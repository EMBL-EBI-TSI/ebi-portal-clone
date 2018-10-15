package uk.ac.ebi.tsc.portal.api.team.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.ac.ebi.tsc.aap.client.model.Domain;
import uk.ac.ebi.tsc.aap.client.model.User;
import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;
import uk.ac.ebi.tsc.portal.api.account.service.AccountService;
import uk.ac.ebi.tsc.portal.api.account.service.UserNotFoundException;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopy;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParamsCopyService;
import uk.ac.ebi.tsc.portal.api.configuration.repo.Configuration;
import uk.ac.ebi.tsc.portal.api.configuration.service.ConfigurationService;
import uk.ac.ebi.tsc.portal.api.deployment.repo.Deployment;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentApplication;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentApplicationCloudProvider;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentStatusEnum;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentConfigurationService;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentService;
import uk.ac.ebi.tsc.portal.api.team.controller.TeamResource;
import uk.ac.ebi.tsc.portal.api.team.repo.Team;
import uk.ac.ebi.tsc.portal.api.team.repo.TeamRepository;
import uk.ac.ebi.tsc.portal.api.utils.SendMail;
import uk.ac.ebi.tsc.portal.clouddeployment.application.ApplicationDeployerDocker;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDeployerException;

import javax.security.auth.login.AccountNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Navis Raj <navis@ebi.ac.uk>
 * @author Jose A. Dianes <jdianes@ebi.ac.uk> (code refactoring)
 */

public class TeamService {

	private final TeamRepository teamRepository;
	private final AccountService accountService;
	private final DomainService domainService;
	private final DeploymentService deploymentService;
	private final CloudProviderParamsCopyService cloudProviderParametersCopyService;
	private final ApplicationDeployerDocker applicationDeployerBash;

	private static final Logger logger = LoggerFactory.getLogger(TeamService.class);

	@Autowired
	public TeamService(TeamRepository teamRepository,
			AccountRepository accountRepository, 
			DomainService domainService,
			DeploymentService deploymentService,
			CloudProviderParamsCopyService cloudProviderParametersCopyService,
			DeploymentConfigurationService deploymentConfigurationService,
			ApplicationDeployerDocker applicationDeployer
			){
		this.teamRepository = teamRepository;
		this.accountService = new AccountService(accountRepository);
		this.domainService = domainService;
		this.deploymentService = deploymentService ;
		this.cloudProviderParametersCopyService = cloudProviderParametersCopyService;
		this.applicationDeployerBash = applicationDeployer;

	}

	public Team findByName(String name){
		return this.teamRepository.findByName(name).orElseThrow(() -> new TeamNotFoundException(name));
	}

	public Collection<Team> findByAccountUsername(String accountUsername){
		return this.teamRepository.findByAccountUsername(accountUsername);
	}

	public Team save(Team team) {
		return this.teamRepository.save(team);
	}

	public void delete(Team team){
		this.teamRepository.delete(team);
	}


	public Team findByNameAndAccountUsername(String teamName, String name) {
		return this.teamRepository.findByNameAndAccountUsername(teamName,name).orElseThrow(() -> new TeamNotFoundException(name));
	}

	public Collection<Team> findAll(){
		return this.teamRepository.findAll();
	}

	public Team findByDomainReference(String domainReference) {
		return this.teamRepository.findByDomainReference(domainReference).orElseThrow(() -> new TeamNotFoundException("with domain reference " + domainReference));
	}

	/**
	 * Create a new team and associated AAP domain.
	 *
	 * @param userName
	 * @param teamResource
	 * @param accountService
	 * @param token
	 * @return
	 * @throws UserNotFoundException
	 */
	public Team constructTeam(
			String userName,
			TeamResource teamResource, 
			AccountService accountService,
			String token) throws UserNotFoundException {


		logger.info("Creating team " + teamResource.getName() + " for user " + userName);

		// Create team
		logger.info("Creating new team");
		Team team = new Team();
		team.setName(teamResource.getName());

		team.setAccount(accountService.findByUsername(userName));
		Account ownerAccount = team.getAccount();

		// Add team members if needed
		if (teamResource.getMemberAccountEmails()!=null) {
			Set<Account> memberAccounts = teamResource.getMemberAccountEmails().stream()
					.map(email -> accountService.findByEmail(email)).collect(Collectors.toSet());
			memberAccounts.add(ownerAccount);
			team.setAccountsBelongingToTeam(memberAccounts);
		}

		// Create associated domain
		// Form domain name
		String domainName = "TEAM_"+ teamResource.getName().toUpperCase()+"_PORTAL_" + userName.toUpperCase();
		try {
			Domain domain = domainService.createDomain(domainName, "Domain " + domainName + " created", token);

			logger.info("Created domain " + domain.getDomainName());

			team.setDomainReference(domain.getDomainReference());

			logger.info("In TeamService: Created team, now saving it " + team.getName());

			try {
				this.save(team);
				return team;
			} catch (Exception e) {
				logger.error("Failed to persist team, after creating AAP domain, so deleting domain: " + e.getMessage());
				try {
					domainService.deleteDomain(domain, token);
				} catch(Exception ex){
					logger.error("Failed to delete the already created AAP domain: " + ex.getMessage());
					throw new TeamNotCreatedException(teamResource.getName(), "failed to persist team and delete already created domain");
				}

				throw new TeamNotCreatedException(teamResource.getName(), "failed to persist team");
			}

		} catch (Exception e) {
			logger.error("Failed to create AAP domain " + domainName);
			throw new TeamNotCreatedException(teamResource.getName(), "failed to create domain " + domainName + ". Reason: " + e.getMessage());
		}



	}

	/**
	 * Remove an account by email from a team given its name.
	 *
	 * @param token
	 * @param teamName
	 * @param userEmail
	 * @return
	 * @throws AccountNotFoundException
	 */
	public Team removeMemberFromTeam(
			String token,
			String teamName, 
			String userEmail) throws AccountNotFoundException {

		logger.debug("Removing user from team " + teamName);

		// Get team
		Team team = this.findByName(teamName);
		if (team==null) {
			throw new TeamNotFoundException(teamName);
		}

		// Get account
		Account account = accountService.findByEmail(userEmail);
		if (account==null) {
			throw new AccountNotFoundException();
		}

		// Only proceed if account is member of team
		if (!team.getAccountsBelongingToTeam().contains(account)) {
			throw new TeamMemberNotRemovedException(teamName, "account " +
					account.getReference() + " is not a member of the team");
		}

		logger.debug("Found associated user with reference " + account.getUsername());

		// If there is an associated domain (it should not for legacy teams)
		if (team.getDomainReference() != null) {
			try {
				//remove member from domain
				logger.debug("Removing user from domain " + team.getDomainReference());

				Domain domain = domainService.getDomainByReference(team.getDomainReference(), token);
				if (domain != null) {
					User domainUser = domainService.getAllUsersFromDomain(domain.getDomainReference(), token)
							.stream()
							.filter(u -> u.getEmail().equals(userEmail))
							.findAny()
							.orElse(null);
					// If the account is actually part of the domain, update the domain
					if (domainUser != null) {
						this.domainService.removeUserFromDomain(new User(null,
								account.getEmail(),
								account.getUsername(),
								account.getGivenName(),
								null),
								domain,
								token);
					}
				}
			} catch (HttpClientErrorException e) {
				if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
					logger.debug("Domain does not exist, removing user from team ");
				} else {
					e.printStackTrace();
					logger.error("Error removing user from team and domain "  + e.getMessage());
				}
			}
		}

		// Update account
		account.getMemberOfTeams().remove(team);
		this.accountService.save(account);

		// Update team
		team.getAccountsBelongingToTeam().remove(account);
		return this.save(team);
	}

	/**
	 * Add accounts to a team, given a token. Send notification emails.
	 *
	 * @param token
	 * @param teamName
	 * @param newMemberEmails
	 * @param baseURL
	 * @return
	 * @throws AccountNotFoundException
	 * @throws UserNotFoundException
	 */
	public Team addMemberToTeam(
			String token,
			String teamName,
			Collection<String> newMemberEmails,
			String baseURL) throws AccountNotFoundException, UserNotFoundException {

		logger.info("Adding member to team " + teamName);

		// Get team
		Team team = this.findByName(teamName);
		if (team==null) {
			throw new TeamNotFoundException(teamName);
		}

		// get the future member accounts
		logger.info("Checking if user is already a member");
		Set<String> memberAccountEmails = team.getAccountsBelongingToTeam().stream().map(Account::getEmail).collect(Collectors.toSet());
		Collection<String> yetToBeMemberEmails = newMemberEmails.stream().filter(
				a -> !memberAccountEmails.contains(a)).collect(Collectors.toSet());
		// throw exception if not all accounts can be added
		if (newMemberEmails.size()!=yetToBeMemberEmails.size()) {
			throw new TeamMemberNotAddedException(teamName, "some accounts already belong to the team, nothing added");
		}
		// get the accounts
		Collection<Account> yetToBeMemberAccounts = yetToBeMemberEmails.stream().map(
				email -> accountService.findByEmail(email)).collect(Collectors.toSet());
		// throw exception if not all accounts can be found
		if (newMemberEmails.size()!=yetToBeMemberAccounts.size()) {
			throw new TeamMemberNotAddedException(teamName, "some accounts cannot be found, nothing added");
		}

		// add accounts to team and persist
		yetToBeMemberAccounts.stream().forEach(
				account -> {
					// Update domain if needed
					if (team.getDomainReference() != null) {
						logger.info("Team has associated domain " + team.getDomainReference() + ". Updating...");
						logger.info("Getting domain");
						Domain domain = domainService.getDomainByReference(
								team.getDomainReference(),
								token);
						if (domain != null) {
							domainService.addUserToDomain(
									domain,
									new User(null, account.getEmail(), account.getUsername(), account.getGivenName() , null),
									token
									);

						} else {
							throw new TeamMemberNotAddedException(teamName, "cannot find associated domain " + team.getDomainReference());
						}
					}

					//update account
					account.getMemberOfTeams().add(team);
					account = this.accountService.save(account);

					// update team
					team.getAccountsBelongingToTeam().add(account);
					this.save(team);

					// Send email notification
					String loginURL = baseURL + "login";
					String teamURL = baseURL + "team" + "/" + team.getName() ;
					String message = "User " + team.getAccount().getGivenName() +
							" has added you to the team " + "'"+team.getName()+"'" + ".\n\n"
							+ "Please click on the link below to login if you haven't already \n"
							+ loginURL.replaceAll(" ", "%20") + "\n\nor click on " + teamURL.replaceAll(" ", "%20")  + "\n"
							+ "to view the team.";
					String toNotifyEmail = account.getEmail();

					try {
						SendMail.send(new ArrayList<String>(){{
							add(toNotifyEmail);
						}}, "Request granted: Added to team " + team.getName(), message );
					} catch (IOException e) {
						logger.error("Couldn't send notification email");
						e.printStackTrace();
					}
				}

				);

		return team;
	}

	/**
	 * Add an account to a team given a token. Send no notification.
	 * TODO: use token to get domains
	 *
	 * @param token
	 * @param teamName
	 * @param account
	 * @return
	 */
	public Team addMemberToTeamByAccountNoNotification(
			String token,
			String teamName,
			Account account) {

		logger.info("Adding " + account.getReference() + " (" + account.getEmail() + ") to team " + teamName);

		// Get team
		Team team = this.findByName(teamName);
		if (team==null) {
			throw new TeamNotFoundException(teamName);
		}

		// Update domain if needed
		if (team.getDomainReference() != null) {
			logger.info("Team has associated domain " + team.getDomainReference() + ". Updating...");
			logger.info("Getting domain");
			Domain domain = domainService.getDomainByReference(
					team.getDomainReference(),
					token);
			if (domain != null) {
				domainService.addUserToDomain(
						domain,
						new User(null, account.getEmail(), account.getUsername(), account.getGivenName() , null),
						token
						);

			} else {
				throw new TeamMemberNotAddedException(teamName, "cannot find associated domain " + team.getDomainReference());
			}
		}

		//update account
		account.getMemberOfTeams().add(team);
		account = this.accountService.save(account);

		// update team
		team.getAccountsBelongingToTeam().add(account);
		this.save(team);

		return team;
	}

	/**
	 * Deletes a team after stopping all the associated deployments.
	 *
	 * @param team the team to delete
	 */
	public void deleteTeam(
			Team team, 
			String token, 
			DeploymentService deploymentService,
			CloudProviderParamsCopyService cloudProviderParametersCopyService, 
			ConfigurationService configurationService) {

		logger.info("Deleting team " + team.getName());

		if (team.getDomainReference() != null) {
			logger.info("- with domain reference " + team.getDomainReference());
			try {
				logger.info("Deleting team which has domain reference, getting domain");
				Domain domain = domainService.getDomainByReference(team.getDomainReference(), token);
				if (domain != null) {
					logger.info("Deleting domain");
					domain.setDomainReference(domain.getDomainReference());
					domainService.deleteDomain(domain, token);
					destroyTeamDeployments(team);
				}

			} catch (HttpClientErrorException e) {
				if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
					logger.error("Domain was not found" + e.getMessage());
				} else {
					logger.error("HTTP client exception: " + e.getMessage());
					throw new TeamNotDeletedException(team.getName(), "AAP client problem - " + e.getMessage());
				}
			} catch (Exception e) {
				logger.error("Unknown exception: " + e.getMessage());
				throw new TeamNotDeletedException(team.getName(), e.getMessage());
			}
		}

		this.delete(team);
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void destroyTeamDeployments(
			Team team) throws IOException{

		logger.info("Stopping deployments of team members (not team owner's)");

		List<String> toNotify = new ArrayList<>();

		//Get all the running deployments belonging to that team
		Collection<Deployment> memberDeployments = 
				deploymentService.findByDomainReference(team.getDomainReference())
				.stream()
				.filter(d -> !d.getAccount().equals(team.getAccount()))//except for team owner account
				.filter(d -> d.getDeploymentStatus().getStatus().equals(DeploymentStatusEnum.STARTING) ||
						d.getDeploymentStatus().getStatus().equals(DeploymentStatusEnum.RUNNING))
				.collect(Collectors.toSet());

		memberDeployments.stream().forEach(deployment -> {
			try{
				stopDeployment(deployment);
				toNotify.add(deployment.getAccount().getEmail());
			}catch (Exception e) {
				logger.error(
						"Failed to stop deployment " + deployment.getReference()
						+ " when team '" + team.getName() + "' was deleted. ");
				e.printStackTrace();
			}
		});

		if(!toNotify.isEmpty()){
			logger.info("There are users who are to be notified, regarding deployments destruction");
			String message = "Your deployments were destroyed. \n"
					+ "This was because team '" + team.getName() + "' was deleted." ;
			SendMail.send(toNotify, "Deployments destroyed", message );

		}
	}

	/**
	 * Stop all deployments associated with an account given by email.
	 * TODO: this method has the same problem as all the previous stop ones - it needs to separate what has been
	 * deployed with something shared withing this team from everything else.
	 *
	 * @param team
	 * @param userEmail
	 */
	public void stopDeploymentsByMemberUserEmail(Team team, String userEmail) {

		logger.info("Stopping all deployments associated with team " + team.getName() + " for user " + userEmail);

		// Get the account
		Account account  = accountService.findByEmail(userEmail);

		// Get the running or starting deployments
		Collection<Deployment> deployments = deploymentService.findByAccountUsername(account.getUsername()).stream()
				.filter(deployment -> deployment.getDeploymentStatus().getStatus().equals(DeploymentStatusEnum.RUNNING) ||
						deployment.getDeploymentStatus().getStatus().equals(DeploymentStatusEnum.STARTING))
				.filter(deployment -> deployment.getDomainReference().equals(team.getDomainReference()))
				.collect(Collectors.toList());

		// To keep track who to notify
		List<String> toNotify = new ArrayList<>();
		toNotify.add(account.getEmail());

		deployments.forEach(deployment -> {
			try{
				this.stopDeployment(deployment);
			} catch (Exception e) {
				logger.error("Failed to stop deployment " + deployment.getReference()
				+ ", using team's shared configuration "
				+ deployment.getDeploymentConfiguration().getConfigurationReference());
				e.printStackTrace();
			}
		});


		if(!toNotify.isEmpty()){
			logger.info("User " + account.getGivenName() + " is to be notified, regarding deployments destruction");
			String message = "Your deployments were destroyed. \n"
					+ "This was because you were removed from the team '" + team.getName() + "'." ;
			try {
				SendMail.send(toNotify, "Deployments destroyed", message );
			} catch (IOException e) {
				logger.error("Failed to send, deployments destroyed notification to " + account.getGivenName() + ".");
			}
		}
	}

	/**
	 * Stop all deployments associated with a team shared cloud credential.
	 * TODO: this method has the same problem as all the previous stop ones - it needs to separate what has been
	 * deployed with something shared withing this team from everything else.
	 * @param team
	 * @param cloudParameters
	 */
	public void stopDeploymentsUsingGivenTeamSharedCloudProvider(
			Team team,
			CloudProviderParameters cloudParameters) {

		logger.info("Stopping deployments of team members (not owner's) using shared cloud credentials " + cloudParameters.getName());

		List<String> toNotify = new ArrayList<>();

		//Get all the running deployments belonging to that team
		Collection<Deployment> memberDeployments = 
				deploymentService.findByDomainReference(team.getDomainReference())
				.stream()
				.filter(d -> !d.getAccount().equals(team.getAccount()))//not the team owner account
				.filter(d -> (d.getDeploymentStatus().getStatus().equals(DeploymentStatusEnum.STARTING)) 
						|| (d.getDeploymentStatus().getStatus().equals(DeploymentStatusEnum.RUNNING)) )
				.collect(Collectors.toSet());

		//for the deployment, there should be a cppcopy
		if(cloudParameters != null){
			CloudProviderParamsCopy cppCopy =
					this.cloudProviderParametersCopyService.findByCloudProviderParametersReference(
							cloudParameters.getReference()
							);
			memberDeployments = memberDeployments.stream()
					.filter(d -> d.getCloudProviderParametersReference().equals(cloudParameters.getReference()))
					.filter(d -> d.getCloudProviderParametersReference().equals(cppCopy.getCloudProviderParametersReference()))
					.collect(Collectors.toSet());
		}

		memberDeployments.stream().forEach(deployment -> {
			try{
				stopDeployment(deployment);
				toNotify.add(deployment.getAccount().getEmail());
			}catch (Exception e) {
				logger.error(
						"Failed to stop deployment " + deployment.getReference()
						+ cloudParameters != null ? " using cloud provider parameters '" + cloudParameters.getName() + "' for user " + deployment.getAccount().getGivenName() : "");
				e.printStackTrace();
			}
		});

		try{
			if(!toNotify.isEmpty()){
				logger.info("There are users who are to be notified, regarding deployments destruction");
				String message = "Your deployments were destroyed. \n"
						+ "This was because  cloud provider parameters '" + cloudParameters.getName() + "' was unshared with you ";
				SendMail.send(toNotify, "Deployments destroyed", message );
			}
		}catch(IOException e){
			logger.error("Failed to send, deployments destroyed notification to the members of the team " + team.getName());
		}
	}

	/**
	 * Stop all deployments associated with a team shared configuration.
	 * TODO: this method has the same problem as all the previous stop ones - it needs to separate what has been
	 * deployed with something shared withing this team from everything else.
	 * @param team
	 * @param configuration
	 */
	public void stopDeploymentsUsingGivenTeamSharedConfiguration(
			Team team,
			Configuration configuration) {

		logger.info("Stopping deployments of team members (not owner's) using shared configuration " + configuration.getName());

		List<String> toNotify = new ArrayList<>();

		//Get all the running deployments belonging to that team
		Collection<Deployment> memberDeployments = 
				deploymentService.findByDomainReference(team.getDomainReference())
				.stream()
				.filter(d -> !d.getAccount().equals(team.getAccount()))
				.filter(d -> (d.getDeploymentStatus().getStatus().equals(DeploymentStatusEnum.STARTING)) || 
						(d.getDeploymentStatus().getStatus().equals(DeploymentStatusEnum.RUNNING)) )
				.filter(d -> d.getDeploymentConfiguration().getConfigurationReference().equals(configuration.getReference()))
				.collect(Collectors.toSet());

		memberDeployments.stream().forEach(deployment -> {
			try{
				stopDeployment(deployment);
				toNotify.add(deployment.getAccount().getEmail());
			}catch (Exception e) {
				logger.error(
						"Failed to stop deployment " + deployment.getReference()
						+" using configuration '" + configuration.getName() + "' for user " + deployment.getAccount().getGivenName());
				e.printStackTrace();
			}
		});

		try{
			if(!toNotify.isEmpty()){
				logger.info("There are users who are to be notified, regarding deployments destruction");
				String message = "Your deployments were destroyed. \n"
						+ "This was because configuration '" + configuration.getName() + "' was unshared with you ";
				SendMail.send(toNotify, "Deployments destroyed", message );
			}
		}catch(IOException e){
			logger.error("Failed to send, deployments destroyed notification to the members of the team '" + team.getName() +"'");
		}
	}

	public void addMemberOnRequest(
			String baseURL,
			TeamResource teamResource) throws IOException {

		Team team = this.findByName(teamResource.getName());

		Set<String> memberAccountEmails = team.getAccountsBelongingToTeam().stream().map(Account::getEmail).collect(Collectors.toSet());

		Collection<String> yetToBeMemberEmails = teamResource.getMemberAccountEmails().stream().filter(
				a -> !memberAccountEmails.contains(a)).collect(Collectors.toSet());
		Account yetToBeMember = yetToBeMemberEmails.stream().map(
				email -> accountService.findByEmail(email)).collect(Collectors.toList()).get(0);

		List<String> toNotify = new ArrayList();
		toNotify.add(team.getAccount().getEmail());

		String loginURL = baseURL + "login";
		String teamURL = baseURL + "team" + "/" + team.getName() ;

		String message = "User " + yetToBeMember.givenName + 
				" would like to join team " + "'"+team.getName()+"'" + ".\n\n"
				+ "Please click on the link below to login if you haven't already \n" 
				+ loginURL.replaceAll(" ", "%20") + "\n\nor click on " + teamURL.replaceAll(" ", "%20")
				+ "\n"
				+ "and use the email " + yetToBeMember.getEmail() + " to add member.\n\n";

		SendMail.send(toNotify, "Request to join team " + team.getName(), message );

	}

	private void stopDeployment(Deployment deployment) throws IOException, ApplicationDeployerException {

		logger.info("Stopping deployment '" + deployment.getReference() + "'");

		// get credentials decrypted through the service layer
		CloudProviderParamsCopy theCloudProviderParametersCopy;
		theCloudProviderParametersCopy = this.cloudProviderParametersCopyService.findByCloudProviderParametersReference(deployment.getCloudProviderParametersReference());

		// Update status
		deployment.getDeploymentStatus().setStatus(DeploymentStatusEnum.DESTROYING);
		this.deploymentService.save(deployment);

		// Proceed to destroy
		this.applicationDeployerBash.destroy(
				deployment.getDeploymentApplication().getRepoPath(),
				deployment.getReference(),
				this.getCloudProviderPathFromDeploymentApplication(
						deployment.getDeploymentApplication(), theCloudProviderParametersCopy.getCloudProvider()),
				deployment.getAssignedInputs(),
				deployment.getAssignedParameters(),
				deployment.getAttachedVolumes(),
				deployment.getDeploymentConfiguration(),
				theCloudProviderParametersCopy
				);

	}

	private static String getCloudProviderPathFromDeploymentApplication(DeploymentApplication deploymentApplication, String cloudProvider) {

		logger.debug("Getting the path of the cloud provider from deploymentApplication");
		Iterator<DeploymentApplicationCloudProvider> it = deploymentApplication.getCloudProviders().iterator();

		while ( it.hasNext() ) {
			DeploymentApplicationCloudProvider cp = it.next();
			if (cloudProvider.equals(cp.getName())) {
				return cp.getPath();
			}
		}

		return null;
	}

}
