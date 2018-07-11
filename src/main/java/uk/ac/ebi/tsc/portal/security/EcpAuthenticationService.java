package uk.ac.ebi.tsc.portal.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;
import uk.ac.ebi.tsc.portal.api.account.service.AccountService;
import uk.ac.ebi.tsc.portal.api.account.service.UserNotFoundException;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParamsCopyService;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentConfigurationRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentStatusRepository;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentConfigurationService;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentService;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;
import uk.ac.ebi.tsc.portal.api.team.service.TeamNotFoundException;
import uk.ac.ebi.tsc.portal.api.team.repo.Team;
import uk.ac.ebi.tsc.portal.api.team.repo.TeamRepository;
import uk.ac.ebi.tsc.portal.api.team.service.TeamService;
import uk.ac.ebi.tsc.portal.clouddeployment.application.ApplicationDeployerBash;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.UUID;

/**
 * Extracts user authentication details from Token using AAP domains API
 *
 * @author Jose A Dianes  <jdianes@ebi.ac.uk>
 * @since 09/05/2018.
 */
@Component
public class EcpAuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(EcpAuthenticationService.class);
    private static final String TOKEN_HEADER_KEY = "Authorization";
    private static final String TOKEN_HEADER_VALUE_PREFIX = "Bearer ";

    uk.ac.ebi.tsc.aap.client.security.TokenAuthenticationService tokenAuthenticationService;

    private final AccountService accountService;
    private final TeamService teamService;
    private final DeploymentService deploymentService;
    private final CloudProviderParamsCopyService cloudProviderParamsCopyService;
    private final DeploymentConfigurationService deploymentConfigurationService;
    private final ApplicationDeployerBash applicationDeployerBash;
    private final String ecpAapUsername;
    private final String ecpAapPassword;


    public EcpAuthenticationService(uk.ac.ebi.tsc.aap.client.security.TokenAuthenticationService tokenAuthenticationService,
                                    AccountRepository accountRepository,
                                    DeploymentRepository deploymentRepository,
                                    DeploymentStatusRepository deploymentStatusRepository,
                                    DeploymentConfigurationRepository deploymentConfigurationRepository,
                                    CloudProviderParamsCopyRepository cloudProviderParamsCopyRepository,
                                    TeamRepository teamRepository,
                                    ApplicationDeployerBash applicationDeployerBash,
                                    DomainService domainService,
                                    EncryptionService encryptionService,
                                    @Value("${ecp.security.salt}") final String salt,
                                    @Value("${ecp.security.password}") final String password,
                                    @Value("${ecp.aap.username}") final String ecpAapUsername,
                                    @Value("${ecp.aap.password}") final String ecpAapPassword) {
        this.tokenAuthenticationService = tokenAuthenticationService;
        this.ecpAapUsername = ecpAapUsername;
        this.ecpAapPassword = ecpAapPassword;
        this.accountService = new AccountService(accountRepository);
        this.applicationDeployerBash = applicationDeployerBash;
        this.deploymentService = new DeploymentService(deploymentRepository, deploymentStatusRepository);
        this.cloudProviderParamsCopyService = new CloudProviderParamsCopyService(
                cloudProviderParamsCopyRepository,
                encryptionService,
                salt, password);
        this.deploymentConfigurationService = new DeploymentConfigurationService(deploymentConfigurationRepository);
        this.teamService = new TeamService(
                teamRepository, accountRepository, domainService, this.deploymentService,
                this.cloudProviderParamsCopyService, this.deploymentConfigurationService,
                this.applicationDeployerBash);
    }

    /**
     * Builds an authentication object from the the http request. Relies on the app client implementation + ECP
     * account creation and handling default team membership.
     *
     * @param request
     * @return
     */
    public Authentication getAuthentication(HttpServletRequest request) {
        Authentication authentication = this.tokenAuthenticationService.getAuthentication(request);
        if (authentication == null) {
            return null;
        }

        // Get token - at this point we know the token is there since we passed the super.getAuthentication
        final String header = request.getHeader(TOKEN_HEADER_KEY);
        final String aapToken = header.substring(TOKEN_HEADER_VALUE_PREFIX.length());
        logger.trace("Got token {}", aapToken);

        // Get user details from authentication details
        uk.ac.ebi.tsc.aap.client.model.User user = (uk.ac.ebi.tsc.aap.client.model.User) authentication.getDetails();

        // Retrieve or create ECP account from DB
        Account account = null;
        try { // Try to find user by token name claim (legacy ECP accounts)
            logger.trace("Looking for account by token 'name' claim {}", user.getUserName());
            // get the account
            account = this.accountService.findByUsername(user.getUserName());
            // Update the account username with the token sub claim
            account.setUsername(user.getUsername()); // TODO - check with @ameliec
            account.setGivenName(user.getUserName());
            this.accountService.save(account);
        } catch (UserNotFoundException userNameNotFoundException) {
            try { // Try with sub claim (currently used)
                logger.trace("Looking for account by token 'sub' claim {}", user.getUsername());
                // check if account exists
                account = this.accountService.findByUsername(user.getUsername());
            } catch (UserNotFoundException usernameNotFoundException) {
                logger.info("No account found for user " + user.getUsername() + " ("+ user.getUserName());
                logger.info("Creating account for user {}, {}", user.getUsername(), user.getUserName());
                try {
                    account = new Account(
                            "acc" + System.currentTimeMillis(),
                            user.getUsername(),
                            user.getUserName(),
                            UUID.randomUUID().toString(),
                            user.getEmail(), // TODO - check with @ameliec
                            new Date(System.currentTimeMillis()),
                            null,
                            null
                    );
                    this.accountService.save(account);
                } catch (Exception sql) {
                    logger.info("Couldn't add new account for user "
                            + user.getUsername() + " ("+ user.getUserName() +"). Already added?");
                    logger.info(sql.getMessage());
                    return authentication;
                }
            }
        }

        // We should have retrieved or created an ECP account by now... otherwise
        if (account==null) {
            return null;
        }

        // Manage default team membership
        this.addAccountToDefaultTeamsByEmail(account, aapToken);

        return authentication;
    }

    /**
     * Adds accounts to EBI/EMBL default teams if they are not already there.
     *
     * @param account The ECP Account which membership needs to be managed
     * @param appToken The AAP token associated with the authentication request
     */
    public void addAccountToDefaultTeamsByEmail(Account account, String appToken) {
        if (account.getEmail().endsWith("@ebi.ac.uk")) {
            try {
                // Get EBI team
                Team emblEbiTeam = this.teamService.findByName("EMBL-EBI");
                if (!emblEbiTeam.getAccountsBelongingToTeam().stream().anyMatch(anotherAccount -> anotherAccount.getUsername().equals(account.getUsername()))) {
                    // Add member to team
                    teamService.addMemberToTeamNoEmail(appToken, emblEbiTeam, account);
                }
            } catch (TeamNotFoundException tnfe) {
                logger.info("Team EMBL-EBI not found. Can't add user " + account.getEmail());
            }
        } else if (account.getEmail().endsWith("@embl.de")) {
            try {
                // Get EMBL team
                Team emblTeam = this.teamService.findByName("EMBL");
                if (!emblTeam.getAccountsBelongingToTeam().stream().anyMatch(anotherAccount -> anotherAccount.getUsername().equals(account.getUsername()))) {
                    // Add member to team
                    teamService.addMemberToTeamNoEmail(appToken, emblTeam, account);
                }
            } catch (TeamNotFoundException tnfe) {
                logger.info("Team EMBL not found. Can't add user " + account.getEmail());
            }
        }
    }

}
