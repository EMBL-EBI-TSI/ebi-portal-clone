package uk.ac.ebi.tsc.portal.security;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.aap.client.repo.TokenService;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;
import uk.ac.ebi.tsc.portal.api.account.service.AccountService;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParamsCopyService;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentConfigurationRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentStatusRepository;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentConfigurationService;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentService;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;
import uk.ac.ebi.tsc.portal.api.team.controller.TeamNotFoundException;
import uk.ac.ebi.tsc.portal.api.team.repo.Team;
import uk.ac.ebi.tsc.portal.api.team.repo.TeamRepository;
import uk.ac.ebi.tsc.portal.api.team.service.TeamService;
import uk.ac.ebi.tsc.portal.clouddeployment.application.ApplicationDeployerBash;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.UUID;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
/**
 * Checks whether a request contains a valid Json Web Token, from a valid user
 */
@Component
public class TokenAuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationService.class);
    private static final String TOKEN_HEADER_KEY = "Authorization";
    private static final String TOKEN_HEADER_VALUE_PREFIX = "Bearer ";

    private final TokenHandler tokenHandler;
    private final AccountService accountService;
    private final TeamService teamService;
    private final DeploymentService deploymentService;
    private final CloudProviderParamsCopyService cloudProviderParamsCopyService;
    private final DeploymentConfigurationService deploymentConfigurationService;
    private final ApplicationDeployerBash applicationDeployerBash;
    private final String ecpAapUsername;
    private final String ecpAapPassword;
    private final TokenService tokenService;


    @Autowired
    public TokenAuthenticationService(TokenHandler tokenHandler,
                                      AccountRepository accountRepository,
                                      DeploymentRepository deploymentRepository,
                                      DeploymentStatusRepository deploymentStatusRepository,
                                      DeploymentConfigurationRepository deploymentConfigurationRepository,
                                      CloudProviderParamsCopyRepository cloudProviderParamsCopyRepository,
                                      TeamRepository teamRepository,
                                      ApplicationDeployerBash applicationDeployerBash,
                                      DomainService domainService,
                                      TokenService tokenService,
                                      EncryptionService encryptionService,
                                      @Value("${ecp.security.salt}") final String salt,
                                      @Value("${ecp.security.password}") final String password,
                                      @Value("${ecp.aap.username}") final String ecpAapUsername,
                                      @Value("${ecp.aap.password}") final String ecpAapPassword) {
        this.ecpAapUsername = ecpAapUsername;
        this.ecpAapPassword = ecpAapPassword;
        this.tokenHandler = tokenHandler;
        this.tokenService = tokenService;
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

    Authentication getAuthentication(HttpServletRequest request) {
        try {
            final String header = request.getHeader(TOKEN_HEADER_KEY);
            if (header == null) {
                logger.trace("No {} header", TOKEN_HEADER_KEY);
                return null;
            }
            if (!header.startsWith(TOKEN_HEADER_VALUE_PREFIX)) {
                logger.trace("No {} prefix", TOKEN_HEADER_VALUE_PREFIX);
                return null;
            }
            final String token = header.substring(TOKEN_HEADER_VALUE_PREFIX.length());
            if (StringUtils.isEmpty(token)) {
                logger.trace("Missing jwt token");
                return null;
            }
            logger.trace("Got token {}", token);


            try { // Try to find user by token name claim
                UserDetails user = tokenHandler.loadUserFromTokenName(token);
                logger.trace("user details by sub {}", user.getUsername());
                // Here we need to update the legacy username by using sub instead
                String name = tokenHandler.parseNameFromToken(token);
                Account theAccount = this.accountService.findByUsername(name);
                String userName = tokenHandler.parseUserNameFromToken(token);
                theAccount.setUsername(userName);
                theAccount.setGivenName(name);
                this.accountService.save(theAccount);
                // Add user to their organisation teams if needed
                if (theAccount.getEmail().endsWith("@ebi.ac.uk")) {
                    // Get ECP AAP account token
                    String ecpAapToken = tokenService.getAAPToken(this.ecpAapUsername, this.ecpAapPassword);

                    try {
                        // Get EBI team
                        Team emblEbiTeam = this.teamService.findByName("EMBL-EBI");
                        // Add member to team
                        teamService.addMemberToTeamNoEmail(ecpAapToken, emblEbiTeam, theAccount);
                    } catch (TeamNotFoundException tnfe) {
                        logger.info("Team EMBL-EBI not found. Can't add user " + theAccount.getEmail());
                    }
                }
                // Return the user authentication
                return new UserAuthentication(tokenHandler.loadUserFromTokenSub(token));
            } catch (UsernameNotFoundException usernameNotFoundException) { // Not found by given name...
                try { // Try to find user by sub name claim
                    UserDetails user = tokenHandler.loadUserFromTokenSub(token);
                    logger.trace("user details by sub {}", user.getUsername());
                    // Add user to their organisation teams if needed
                    Account theAccount = this.accountService.findByUsername(user.getUsername());
                    if (theAccount.getEmail().endsWith("@ebi.ac.uk")) {
                        // Get ECP AAP account token
                        String ecpAapToken = tokenService.getAAPToken(this.ecpAapUsername, this.ecpAapPassword);

                        try {
                            // Get EBI team
                            Team emblEbiTeam = this.teamService.findByName("EMBL-EBI");
                            // Add member to team
                            teamService.addMemberToTeamNoEmail(ecpAapToken, emblEbiTeam, theAccount);
                        } catch (TeamNotFoundException tnfe) {
                            logger.info("Team EMBL-EBI not found. Can't add user " + theAccount.getEmail());
                        }
                    }
                    // Return the user authentication
                    return new UserAuthentication(user);
                } catch (UsernameNotFoundException anotherUsernameNotFoundException) { // User not found at all
                    String userName = tokenHandler.parseUserNameFromToken(token);
                    String email = tokenHandler.parseEmailFromToken(token);
                    String name = tokenHandler.parseNameFromToken(token);
                    logger.info("No account found for user " + userName);
                    logger.info("Creating account for user {}, {}", userName, name);
                    try {
                        Account newAccount = new Account(
                                "acc" + System.currentTimeMillis(),
                                userName,
                                name,
                                UUID.randomUUID().toString(),
                                email,
                                new Date(System.currentTimeMillis()),
                                null,
                                null
                        );
                        this.accountService.save(newAccount);
                        // Add user to their organisation teams if needed
                        if (newAccount.getEmail().endsWith("@ebi.ac.uk")) {
                            // Get ECP AAP account token
                            String ecpAapToken = tokenService.getAAPToken(this.ecpAapUsername, this.ecpAapPassword);

                            try {
                                // Get EBI team
                                Team emblEbiTeam = this.teamService.findByName("EMBL-EBI");
                                // Add member to team
                                teamService.addMemberToTeamNoEmail(ecpAapToken, emblEbiTeam, newAccount);
                            } catch (TeamNotFoundException tnfe) {
                                logger.info("Team EMBL-EBI not found. Can't add user " + newAccount.getEmail());
                            }
                        }
                        // Return the user authentication
                        return new UserAuthentication(tokenHandler.loadUserFromTokenSub(token));
                    } catch (Exception sql) {
                        logger.info("Couldn't add new account for user " + userName + ". Already added?");
                        logger.info(sql.getMessage());
                        return new UserAuthentication(tokenHandler.loadUserFromTokenSub(token));
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.trace("", e);
            return null;
        }
    }

}