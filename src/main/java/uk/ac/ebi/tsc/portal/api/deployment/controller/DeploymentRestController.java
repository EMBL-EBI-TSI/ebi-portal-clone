
package uk.ac.ebi.tsc.portal.api.deployment.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;
import uk.ac.ebi.tsc.portal.api.account.service.AccountService;
import uk.ac.ebi.tsc.portal.api.application.controller.InvalidApplicationInputException;
import uk.ac.ebi.tsc.portal.api.application.repo.Application;
import uk.ac.ebi.tsc.portal.api.application.repo.ApplicationCloudProvider;
import uk.ac.ebi.tsc.portal.api.application.repo.ApplicationRepository;
import uk.ac.ebi.tsc.portal.api.application.service.ApplicationNotFoundException;
import uk.ac.ebi.tsc.portal.api.application.service.ApplicationNotSharedException;
import uk.ac.ebi.tsc.portal.api.application.service.ApplicationService;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParametersRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopy;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.*;
import uk.ac.ebi.tsc.portal.api.configuration.controller.InvalidConfigurationInputException;
import uk.ac.ebi.tsc.portal.api.configuration.repo.*;
import uk.ac.ebi.tsc.portal.api.configuration.service.*;
import uk.ac.ebi.tsc.portal.api.deployment.repo.*;
import uk.ac.ebi.tsc.portal.api.deployment.service.*;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;
import uk.ac.ebi.tsc.portal.api.error.ErrorMessage;
import uk.ac.ebi.tsc.portal.api.error.MissingParameterException;
import uk.ac.ebi.tsc.portal.api.team.repo.Team;
import uk.ac.ebi.tsc.portal.api.team.repo.TeamRepository;
import uk.ac.ebi.tsc.portal.api.team.service.TeamService;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstance;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceRepository;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceStatusRepository;
import uk.ac.ebi.tsc.portal.api.volumeinstance.service.VolumeInstanceService;
import uk.ac.ebi.tsc.portal.clouddeployment.application.ApplicationDeployerBash;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentSecretService;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDeployerException;
import uk.ac.ebi.tsc.portal.usage.deployment.model.DeploymentDocument;
import uk.ac.ebi.tsc.portal.usage.deployment.service.DeploymentIndexService;
import uk.ac.ebi.tsc.portal.usage.tracker.DeploymentStatusTracker;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@RestController
@RequestMapping(value = "/deployment", produces = {MediaType.APPLICATION_JSON_VALUE})
public class DeploymentRestController {

	private static final Logger logger = LoggerFactory.getLogger(DeploymentRestController.class);
	private static final Pattern IS_IP_ADDRESS = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}");
	private static final long UPDATE_TRACKER_PERIOD = 30;

	@Value("${be.applications.root}")
	private String applicationsRoot;

	@Value("${be.deployments.root}")
	private String deploymentsRoot;

	@Value("${elasticsearch.url}")
	private String elasticSearchUrl;

	@Value("${elasticsearch.index}")
	private String elasticSearchIndex;

	@Value("${elasticsearch.username}")
	private String elasticSearchUsername;

	@Value("${elasticsearch.password}")
	private String elasticSearchPassword;

	private final DeploymentService deploymentService;

	private final DeploymentGeneratedOutputService deploymentGeneratedOutputService;

	private final AccountService accountService;

	private final ApplicationService applicationService;

	private final VolumeInstanceService volumeInstanceService;

	private final CloudProviderParametersService cloudProviderParametersService;

	private final ConfigurationService configurationService;

	private final ConfigurationDeploymentParametersService deploymentParametersService;

	private final DeploymentConfigurationService deploymentConfigurationService;

	private final TeamService teamService;

	private final ApplicationDeployerBash applicationDeployerBash;

	private final DeploymentStatusTracker deploymentStatusTracker;

	private final DeploymentApplicationService deploymentApplicationService;

	private final CloudProviderParamsCopyService cloudProviderParametersCopyService;

	private final ConfigDeploymentParamsCopyService configDeploymentParamsCopyService;

	private DeploymentSecretService deploymentSecretService;

	@Autowired
	DeploymentRestController(DeploymentRepository deploymentRepository,
							 DeploymentStatusRepository deploymentStatusRepository,
							 AccountRepository accountRepository,
							 ApplicationRepository applicationRepository,
							 VolumeInstanceRepository volumeInstanceRepository,
							 VolumeInstanceStatusRepository volumeInstanceStatusRepository,
							 CloudProviderParametersRepository cloudProviderParametersRepository,
							 ConfigurationRepository configurationRepository,
							 TeamRepository teamRepository,
							 ApplicationDeployerBash applicationDeployerBash,

							 DeploymentStatusTracker deploymentStatusTracker,

							 ConfigurationDeploymentParametersRepository deploymentParametersRepository,
							 DomainService domainService,
							 DeploymentConfigurationRepository deploymentConfigurationRepository,
							 DeploymentApplicationRepository deploymentApplicationRepository,

							 CloudProviderParamsCopyRepository cloudProviderParametersCopyRepository,
							 ConfigDeploymentParamsCopyRepository configDeploymentParamsCopyRepository,
							 EncryptionService encryptionService,
							 DeploymentSecretService deploymentSecretService,
							 DeploymentGeneratedOutputService deploymentGeneratedOutputService,
							 @Value("${ecp.security.salt}") final String salt,
							 @Value("${ecp.security.password}") final String password
			) {
		this.cloudProviderParametersCopyService = new CloudProviderParamsCopyService(cloudProviderParametersCopyRepository, encryptionService);
		this.deploymentService = new DeploymentService(deploymentRepository, deploymentStatusRepository);
		this.accountService = new AccountService(accountRepository);
		this.applicationService = new ApplicationService(applicationRepository, domainService);
		this.volumeInstanceService = new VolumeInstanceService(volumeInstanceRepository,
				volumeInstanceStatusRepository);
		this.cloudProviderParametersService = new CloudProviderParametersService(cloudProviderParametersRepository, domainService, 
				cloudProviderParametersCopyService, encryptionService);
		this.applicationDeployerBash = applicationDeployerBash;
		this.deploymentStatusTracker = deploymentStatusTracker;
		this.deploymentStatusTracker.start(0, UPDATE_TRACKER_PERIOD);
		this.deploymentParametersService = new ConfigurationDeploymentParametersService(deploymentParametersRepository, domainService);
		this.configurationService = new ConfigurationService(configurationRepository, domainService,
				cloudProviderParametersService, deploymentParametersService,
				cloudProviderParametersCopyService, deploymentService
				);
		this.deploymentConfigurationService = new DeploymentConfigurationService(deploymentConfigurationRepository);
		this.deploymentApplicationService = new DeploymentApplicationService(deploymentApplicationRepository);
		this.configDeploymentParamsCopyService = new ConfigDeploymentParamsCopyService(configDeploymentParamsCopyRepository);
		this.teamService = new TeamService(teamRepository, accountRepository, domainService,
				deploymentService, cloudProviderParametersCopyService, deploymentConfigurationService, applicationDeployerBash);
		this.deploymentSecretService = deploymentSecretService;
		this.deploymentGeneratedOutputService = deploymentGeneratedOutputService;
	}

	/* useful to inject values without involving spring - i.e. tests */
	void setProperties(Properties properties) {
		this.applicationsRoot = properties.getProperty("be.applications.root");
		this.deploymentsRoot = properties.getProperty("be.deployments.root");
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> addDeployment(HttpServletRequest request, Principal principal, @RequestBody DeploymentResource input)
			throws IOException, NoSuchPaddingException, InvalidKeyException,
			NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException, ApplicationDeployerException {

		logger.info("Adding new " + input.applicationName +
				" deployment by user " + principal.getName() +
				" config " + input.configurationName);

		//check if inputs to find an application is specified
		if((input.getApplicationName() == null) || (input.getApplicationAccountUsername() == null)){
			throw new InvalidApplicationInputException();
		}

		//check if inputs to find a configuration is specified
		if((input.getConfigurationName() == null) || (input.getConfigurationAccountUsername() == null)){
			throw new InvalidConfigurationInputException();
		}
		
		// Get the requester and owner accounts
		Account account = this.accountService.findByUsername(principal.getName());
		logger.info("Principal username " + principal.getName());
		logger.info("Account user name " + account.getUsername());
		logger.debug("Found requesting account {}", account.getGivenName());
		Account applicationOwnerAccount = this.accountService.findByUsername(input.getApplicationAccountUsername());
		logger.debug("Found application owner account {}", applicationOwnerAccount.getGivenName());
		Account configurationOwnerAccount = this.accountService.findByUsername(input.getConfigurationAccountUsername());
		logger.debug("Found configuration owner account {}", configurationOwnerAccount.getGivenName());
		
		// Get the application
		logger.info("Looking for application " + input.getApplicationName() + " for user " + account.getGivenName());
		Application theApplication;

		try{
			//if it is the account owner himself

			logger.info("Checking if the account user is the owner of the application");
			theApplication = this.applicationService.findByAccountUsernameAndName(
					account.getUsername(), input.getApplicationName());
		}catch(ApplicationNotFoundException e){
			//find if the application is shared with user
			logger.info("Account user is not application owner, checking if it has been shared with him" );
			theApplication = this.applicationService.findByAccountUsernameAndName(
					applicationOwnerAccount.getUsername(), input.getApplicationName());
			if(!applicationService.isApplicationSharedWithAccount(account, theApplication)){
				throw new ApplicationNotSharedException(account.getGivenName(), theApplication.getName());
			}
			logger.debug("Application " + theApplication.getName() + " has been shared with " + account.getGivenName());
		}


		// Get the configuration
		logger.info("Looking for configuration " + input.getConfigurationName() + " for user " + account.getGivenName());
		Configuration configuration = null;

		//if it is the account owner himself
		if(input.getDomainReference() == null){
			try{
				logger.info("Checking if the account user is the owner of the configuration");
				configuration = this.configurationService.findByNameAndAccountUsername(
						input.getConfigurationName(), account.getUsername());
			}catch(ConfigurationNotFoundException e){
				throw new ConfigurationNotFoundException(account.getGivenName(), input.getConfigurationName());
			}
		}else{
			try{
				logger.info("Checking if the configuration " +
						input.getConfigurationName() + " has been shared with user by its owner " +
						configurationOwnerAccount.getUsername() + " in team " + input.getDomainReference());
				configuration = this.configurationService.findByNameAndAccountUsername(
						input.getConfigurationName(), configurationOwnerAccount.getUsername());
				logger.debug("Looking for team " + input.getDomainReference());
				Team team = teamService.findByDomainReference(input.getDomainReference());
				if(!configurationService.isConfigurationSharedWithAccount(team, account, configuration)){
					throw new ConfigurationNotSharedException(account.getGivenName(), configuration.getName());
				}
			}catch(ConfigurationNotFoundException e){
				throw new ConfigurationNotFoundException(configurationOwnerAccount.getGivenName(), input.getConfigurationName());
			}
		}
		// Find the cloud provider parameters
		CloudProviderParameters selectedCloudProviderParameters;
		if(configuration != null) {
			Account credentialOwnerAccount = this.accountService.findByUsername(input.getConfigurationAccountUsername());
			try{
				//both configuration and cloud owner are same
				logger.info("Looking for CONFIGURATION cloud provider params '{}' by username '{}'", configuration.cloudProviderParametersName, credentialOwnerAccount.getUsername());
				selectedCloudProviderParameters = this.cloudProviderParametersService.findByNameAndAccountUsername(
						configuration.cloudProviderParametersName, credentialOwnerAccount.getUsername());
			}catch(CloudProviderParametersNotFoundException e){
				//configuration and cloud owner are different
				logger.info("Looking for CONFIGURATION cloud provider params(shared) '{}' by username '{}'", configuration.cloudProviderParametersName, credentialOwnerAccount.getUsername());
				try{
					selectedCloudProviderParameters = this.cloudProviderParametersService.findByReference(
							configuration.getCloudProviderParametersReference());
					if(!cloudProviderParametersService.isCloudProviderParametersSharedWithAccount(account, selectedCloudProviderParameters)){
						throw new CloudProviderParametersNotSharedException(account.getGivenName(), selectedCloudProviderParameters.getName());
					}
					logger.debug("Cloud provider parameters" + selectedCloudProviderParameters.getName() + " has been shared with " + account.getGivenName());
				}catch(CloudProviderParametersNotFoundException ex){
					throw new CloudProviderParametersNotFoundException(credentialOwnerAccount.getUsername(), configuration.cloudProviderParametersName);
				}
			}
		} else { // TODO: At some point we shouldn't allow to pass specific cloud provider parameters and use just those in a configuration
			CloudProviderParameters cpp = this.cloudProviderParametersService.findByReference(input.getCloudProviderParametersCopy().getCloudProviderParametersReference());
			Account credentialOwnerAccount = this.accountService.findByUsername(cpp.getAccount().getUsername());

			logger.info("Looking for EXPLICIT (legacy)cloud provider params '{}' by username '{}'", cpp.getName(), credentialOwnerAccount.getUsername());
			selectedCloudProviderParameters = this.cloudProviderParametersService.findByNameAndAccountUsername(
					cpp.getName(), credentialOwnerAccount.getUsername());
		}

		DeploymentIndexService deploymentIndexService = new DeploymentIndexService(
				new RestTemplate(),
				this.elasticSearchUrl + "/" + this.elasticSearchIndex,
				this.elasticSearchUsername,
				this.elasticSearchPassword);

		if(input.getAssignedInputs() != null){
			for (DeploymentAssignedInputResource assignedInput: input.getAssignedInputs()) {
				logger.info("    Assigned input " + assignedInput.getInputName() + " = " + assignedInput.getAssignedValue());
			}
		}
		if(input.getAssignedParameters() != null){
			for (DeploymentAssignedParameterResource assignedParameter: input.getAssignedParameters()) {
				logger.debug("    Assigned parameter " + assignedParameter.getParameterName() + " = " + assignedParameter.getParameterValue());
			}
		}
		if(input.getAttachedVolumes() != null){
			for (DeploymentAttachedVolumeResource attachedVol: input.getAttachedVolumes()) {
				logger.debug("    Attached volume " + attachedVol.getName() + " = " + attachedVol.getVolumeInstanceReference());
			}
		}
		if(input.getConfigurationName() != null && (!input.getConfigurationName().isEmpty())){
			logger.debug("Added configuration " + input.getConfigurationName());
		}
		if(input.getUserSshKey() != null){
			logger.debug("User has added his own ssh-key " + input.getUserSshKey());
		}

		//get configuration deployment parameters
		Map<String, String> deploymentParameterKV = new HashMap<String, String>();
		if(configuration != null) {
			// check hard usage limit before proceeding any further
			if (configuration.getHardUsageLimit()!= null && this.configurationService.getTotalConsumption(configuration, deploymentIndexService)>=configuration.getHardUsageLimit()) {
				throw new UsageLimitsException(configuration.getName(), configuration.getAccount().getEmail(), configuration.getHardUsageLimit());
			}
			String configurationDeploymentParametersName = this.configDeploymentParamsCopyService.findByConfigurationDeploymentParametersReference(configuration.getConfigDeployParamsReference()).getName();
			try{
				ConfigDeploymentParamsCopy configDeploymentParamsCopy = this.configDeploymentParamsCopyService.findByName(configurationDeploymentParametersName);
				configDeploymentParamsCopy.getConfigDeploymentParamCopy().forEach( parameter ->{
					deploymentParameterKV.put(parameter.getKey(), parameter.getValue());
				});
			}catch(ConfigDeploymentParamsCopyNotFoundException e){
				throw new ConfigDeploymentParamsCopyNotFoundException(configurationDeploymentParametersName, account.getGivenName());
			}
		}

		CloudProviderParamsCopy cloudProviderParametersCopy =
				this.cloudProviderParametersCopyService.findByCloudProviderParametersReference(selectedCloudProviderParameters.getReference());

		Date startTime = new Date(System.currentTimeMillis());
		logger.info("Deployment start time " + startTime);

		// Trigger application deployment
		String theReference = "TSI" + System.currentTimeMillis();

		//create a copy of the application as deployment application
		logger.info("Creating deploymentApplication, from the application");
		DeploymentApplication deploymentApplication = this.deploymentApplicationService.createDeploymentApplication(theApplication);
		this.deploymentApplicationService.save(deploymentApplication);

		// Create and persist deployment object (part of this, like the IP and Id
		// will go into a separate endpoint, when having the async API running)
		final Deployment deployment = new Deployment(
				theReference,
				account,
				deploymentApplication,
				selectedCloudProviderParameters.getReference(),
				input.getUserSshKey(),
				input.getDomainReference()
				);
		Deployment resDeployment = this.deploymentService.save(deployment);

		//Deploy
		this.applicationDeployerBash.deploy(
				account.getEmail(),
				theApplication,
				theReference,
				getCloudProviderPathFromApplication(theApplication, selectedCloudProviderParameters.getCloudProvider()),
				input.getAssignedInputs()!=null ?
						input.getAssignedInputs().stream().collect(Collectors.toMap(s -> s.getInputName(), s-> s.getAssignedValue()))
						: null,
				//the following based on precedence discussion might change, so placeholder here
				deploymentParameterKV!=null ? deploymentParameterKV :null,
				input.getAttachedVolumes()!=null? toProviderIdHashMap(input.getAttachedVolumes()) : null,
				deploymentParameterKV!=null ? deploymentParameterKV :null,
				cloudProviderParametersCopy,
				configuration,
				new java.sql.Timestamp(startTime.getTime()),
				input.getUserSshKey(),
				baseURL(request)
		);

		// set input assignments
		if (input.getAssignedInputs()!=null) {
			for (DeploymentAssignedInputResource assignment : input.getAssignedInputs()) {
				logger.debug("Setting application input  assignment for " + assignment.getInputName()+ " to value " + assignment.getAssignedValue());
				// create the assignment
				DeploymentAssignedInput newAssignment = new DeploymentAssignedInput(
						assignment.getInputName(),
						assignment.getAssignedValue(),
						deployment
						);
				// add it to the volume
				deployment.getAssignedInputs().add(newAssignment);
			}
		}

		if(configuration != null){
			logger.debug("Adding deployment configuration " + configuration.getName());
			DeploymentConfiguration addedConfiguration = new DeploymentConfiguration(
					input.getConfigurationName(),
					input.getConfigurationAccountUsername(),
					configuration.getSshKey(),
					deployment,
					configuration.getReference(),
					configuration.getConfigDeployParamsReference()
					);
			// add it to the deployment
			deployment.setDeploymentConfiguration(addedConfiguration);

			if (deploymentParameterKV !=null) {
				deploymentParameterKV.forEach((k,v) -> {
					logger.debug("Setting deployment parameter assignment for " + k + " to value " + v);
					DeploymentConfigurationParameter newAssignment = 
							new DeploymentConfigurationParameter(k,v,addedConfiguration);
					logger.debug("Setting configuration parameter " + newAssignment.getParameterName() + " to " + newAssignment.getParameterValue() );
					addedConfiguration.getConfigurationParameters().add(newAssignment);
				});
			}

			//set deployment parameters
			if (deploymentParameterKV !=null) {
				deploymentParameterKV.forEach((k,v) -> {
					logger.debug("Setting deployment parameter assignment for " + k + " to value " + v);
					// create the assignment
					DeploymentAssignedParameter newAssignment = new DeploymentAssignedParameter(k,v, deployment);
					// add it to the volume
					deployment.getAssignedParameters().add(newAssignment);
				});
			}
		}

		deployment.setStartTime(new Timestamp(startTime.getTime()));
		resDeployment = this.deploymentService.save(deployment);

		// set deployment to attachments
		if (input.getAttachedVolumes()!=null) {
			for (DeploymentAttachedVolumeResource attachment : input.getAttachedVolumes()) {
				logger.debug("Setting deployment " + resDeployment.getReference() + " to volume " + attachment.getVolumeInstanceReference());
				// a sanity check here for volume instance existence
				VolumeInstance vInstance = this.volumeInstanceService.findByReference(attachment.getVolumeInstanceReference());
				// create the attachment
				DeploymentAttachedVolume newAttachment = new DeploymentAttachedVolume(
						attachment.getName(),
						resDeployment,
						attachment.getVolumeInstanceReference());
				newAttachment.setVolumeInstanceProviderId(vInstance.getProviderId());
				// add it to the volume
				resDeployment.getAttachedVolumes().add(newAttachment);
			}
		}
		resDeployment = this.deploymentService.save(resDeployment);
		// Prepare response
		HttpHeaders httpHeaders = new HttpHeaders();

		DeploymentResource deploymentResource = new DeploymentResource(resDeployment, cloudProviderParametersCopy);
		Link forOneDeployment = deploymentResource.getLink("self");
		httpHeaders.setLocation(URI.create(forOneDeployment.getHref()));

		return new ResponseEntity<>(deploymentResource, httpHeaders, HttpStatus.CREATED);
	}

	String baseURL(HttpServletRequest request) throws MalformedURLException {
	    
	    String requestUrl = request.getRequestURL().toString(); // includes the server path
	    
	    // Let's remove it
	    URL url = new URL(requestUrl);
	    
        return String.format("%s://%s%s" , url.getProtocol()
	                                     , url.getHost()
	                                     , getPortStr(url)
	                                     );
    }

    String getPortStr(URL url) {
        
        int port = url.getPort();
	    
	    return port == -1 ? ""
                          : format(":%d", port)
  	                      ;
    }

	private String getCloudProviderPathFromApplication(Application application, String cloudProvider) {
		Iterator<ApplicationCloudProvider> it = application.getCloudProviders().iterator();

		while ( it.hasNext() ) {
			ApplicationCloudProvider cp = it.next();
			if (cloudProvider.equals(cp.getName())) {
				return cp.getPath();
			}
		}

		return null;
	}

	public static String getCloudProviderPathFromDeploymentApplication(DeploymentApplication deploymentApplication, String cloudProvider) {

		logger.info("Getting the path of the cloud provider from deploymentApplication");
		Iterator<DeploymentApplicationCloudProvider> it = deploymentApplication.getCloudProviders().iterator();

		while ( it.hasNext() ) {
			DeploymentApplicationCloudProvider cp = it.next();
			if (cloudProvider.equals(cp.getName())) {
				return cp.getPath();
			}
		}

		return null;
	}

	@RequestMapping(method = RequestMethod.GET)
	public Resources<DeploymentResource> getAllDeploymentsByUserId(Principal principal) throws IOException, ApplicationDeployerException {
		String userId = principal.getName();

		logger.info("User '" + userId + "' deployment list requested");

		this.accountService.findByUsername(userId);

		Collection<Deployment> userDeployments = this.deploymentService.findByAccountUsername(userId);

		userDeployments.stream().forEach( d -> {
			logger.debug("There are " + d.getGeneratedOutputs().size() + " generated outputs for deployment " + d.getReference());
		});

		List<DeploymentResource> deploymentResourceList = new ArrayList<>();

		userDeployments.forEach(deployment -> {
			deploymentResourceList.add(new DeploymentResource(deployment, null));
		});

		return new Resources<>(deploymentResourceList);
	}

	@RequestMapping(value = "/{deploymentReference}", method = RequestMethod.GET)
	DeploymentResource getDeploymentByReference(Principal principal, @PathVariable("deploymentReference") String reference) throws IOException, ApplicationDeployerException {
		String userId = principal.getName();

		logger.info("Deployment " + reference + " for user " + userId + " requested");
		this.accountService.findByUsername(userId);

		Deployment theDeployment = this.deploymentService.findByReference(reference);
		if(theDeployment.getCloudProviderParametersReference() != null){
			try{
				CloudProviderParamsCopy cppCopy = this.cloudProviderParametersCopyService.findByCloudProviderParametersReference(theDeployment.getCloudProviderParametersReference());
				return new DeploymentResource(theDeployment, cppCopy);
			}catch(CloudProviderParamsCopyNotFoundException e){
				e.printStackTrace();
				logger.error("Could not find cpp copy with reference " + theDeployment.getCloudProviderParametersReference());
				return new DeploymentResource(theDeployment, null);
			}
		}else{
			logger.error("Could not find cpp copy with reference " + theDeployment.getCloudProviderParametersReference());
			return new DeploymentResource(theDeployment, null);
		}


	}

	@RequestMapping(value = "/{deploymentReference}/status", method = RequestMethod.GET)
	public DeploymentStatusResource getDeploymentStatusByReference(Principal principal, @PathVariable("deploymentReference") String reference) throws IOException, ApplicationDeployerException {

		String userId = principal.getName();
		this.accountService.findByUsername(userId);

		Deployment theDeployment = this.deploymentService.findByReference(reference);

		DeploymentIndexService deploymentIndexService = new DeploymentIndexService(
				new RestTemplate(),
				this.elasticSearchUrl + "/" + this.elasticSearchIndex,
				this.elasticSearchUsername,
				this.elasticSearchPassword);
		DeploymentDocument theDeploymentDocument = deploymentIndexService.findById(theDeployment.getReference());

		return new DeploymentStatusResource(
				this.deploymentService.findStatusByDeploymentId(theDeployment.getId()),
				theDeployment,
				theDeploymentDocument);
	}

	@RequestMapping(value = "/{deploymentReference}/outputs", method = RequestMethod.GET)
	public Resources<DeploymentGeneratedOutputResource> getDeploymentOutputsByReference(Principal principal, @PathVariable("deploymentReference") String reference) throws IOException, ApplicationDeployerException {

		String userId = principal.getName();
		this.accountService.findByUsername(userId);

		Deployment theDeployment = this.deploymentService.findByReference(reference);

		return new Resources<>(
				theDeployment.getGeneratedOutputs().stream().map(DeploymentGeneratedOutputResource::new).collect(Collectors.toList())
		);
	}

	@RequestMapping(value = "/{deploymentReference}/outputs", method = RequestMethod.PUT)
	public ResponseEntity<?> addDeploymentOutputs(@PathVariable("deploymentReference") String reference, @RequestHeader("Deployment-Secret") String secret,
												  @RequestBody List<DeploymentGeneratedOutputResource> payLoadGeneratedOutputList) {

		Optional<ErrorMessage> errorMessage = deploymentGeneratedOutputService.saveOrUpdateDeploymentOutputs(reference, secret, payLoadGeneratedOutputList);
		if (errorMessage.isPresent())
			return new ResponseEntity<>(errorMessage.get().getError(), null, errorMessage.get().getStatus());
		return new ResponseEntity<>(null, null, HttpStatus.NO_CONTENT);
	}

	@RequestMapping(value = "/{deploymentReference}/logs", method = RequestMethod.GET)
	public String getDeploymentLogsByReference(Principal principal, @PathVariable("deploymentReference") String reference) throws IOException, ApplicationDeployerException {

		String userId = principal.getName();
		this.accountService.findByUsername(userId);

		Deployment theDeployment = this.deploymentService.findByReference(reference);

		StringBuilder logBuilder = new StringBuilder();
		if (new File(this.deploymentsRoot+File.separator+theDeployment.getReference()+File.separator+"output.log").exists()) {
			Files.readAllLines(
					FileSystems.getDefault().getPath(this.deploymentsRoot + File.separator + theDeployment.getReference() + File.separator + "output.log"),
					StandardCharsets.UTF_8
					).forEach(line -> logBuilder.append(line + System.lineSeparator()));

			return logBuilder.toString();
		} else {
			return "There are no logs for this deployment";
		}
	}

	@RequestMapping(value = "/{deploymentReference}/destroylogs", method = RequestMethod.GET)
	public String getDestroyDeploymentLogsByReference(Principal principal, @PathVariable("deploymentReference") String reference) throws IOException, ApplicationDeployerException {

		String userId = principal.getName();
		this.accountService.findByUsername(userId);

		Deployment theDeployment = this.deploymentService.findByReference(reference);

		StringBuilder logBuilder = new StringBuilder();
		if (new File(this.deploymentsRoot+File.separator+theDeployment.getReference()+File.separator+"destroy.log").exists()) {
			Files.readAllLines(
					FileSystems.getDefault().getPath(this.deploymentsRoot + File.separator + theDeployment.getReference() + File.separator + "destroy.log"),
					StandardCharsets.UTF_8
					).forEach(line -> logBuilder.append(line + System.lineSeparator()));

			return logBuilder.toString();
		} else {
			return "There are no destroy logs for this deployment";
		}
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
	VndErrors handleException(MissingParameterException e) {

		return new VndErrors("error", e.getMessage());
	}

	@RequestMapping(value = "/{deploymentReference}/stopme", method = RequestMethod.PUT)
	public void stopMe( @PathVariable("deploymentReference") String                     deploymentReference
			, @RequestBody                         HashMap<String, String>    body
			)
					throws IOException, ApplicationDeployerException, NoSuchPaddingException, InvalidKeyException,
					NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
					InvalidAlgorithmParameterException, InvalidKeySpecException 
	{
		String secret = body.get("secret");

		if (secret == null || secret.isEmpty()) {

			throw new MissingParameterException("secret");
		}

		if (!deploymentSecretService.exists(deploymentReference, secret)) {

			throw new DeploymentNotFoundException(deploymentReference);
		}

		stop(deploymentReference);
	}

	@RequestMapping(value = "/{deploymentReference}/stop", method = RequestMethod.PUT)
	public ResponseEntity<?> stopByReference(@PathVariable("deploymentReference") String reference)
			throws IOException, ApplicationDeployerException, NoSuchPaddingException, InvalidKeyException,
			NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException, InvalidKeySpecException 
	{
		stop(reference);

		return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.OK);
	}

	void stop(String reference)
			throws IOException, ApplicationDeployerException, NoSuchPaddingException, InvalidKeyException,
			NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException, InvalidKeySpecException 
	{
		logger.info("Stopping deployment '" + reference + "'");

		Deployment theDeployment = this.deploymentService.findByReference(reference);

		// get credentials decrypted through the service layer
		CloudProviderParamsCopy theCloudProviderParametersCopy;
		theCloudProviderParametersCopy = this.cloudProviderParametersCopyService.findByCloudProviderParametersReference(theDeployment.getCloudProviderParametersReference());

		DeploymentConfiguration deploymentConfiguration = null;
		if(theDeployment.getDeploymentConfiguration() != null){
			deploymentConfiguration = this.deploymentConfigurationService.findByDeployment(theDeployment);
		}

		// Update status
		theDeployment.getDeploymentStatus().setStatus(DeploymentStatusEnum.DESTROYING);
		this.deploymentService.save(theDeployment);

		// Proceed to destroy
		this.applicationDeployerBash.destroy(
				theDeployment.getDeploymentApplication().getRepoPath(),
				theDeployment.getReference(),
				getCloudProviderPathFromDeploymentApplication(
						theDeployment.getDeploymentApplication(), theCloudProviderParametersCopy.getCloudProvider()),
				theDeployment.getAssignedInputs(),
				theDeployment.getAssignedParameters(),
				theDeployment.getAttachedVolumes(),
				deploymentConfiguration,
				theCloudProviderParametersCopy
				);
	}

	@RequestMapping(value = "/{deploymentReference}", method = RequestMethod.DELETE)
	public ResponseEntity<?> removeDeploymentByReference(
			Principal principal,
			@PathVariable("deploymentReference") String reference) {

		logger.info("Deleting deployment '" + reference + "'");

		Deployment theDeployment = this.deploymentService.findByReference(reference);

		String cppReference = theDeployment.getCloudProviderParametersReference();

		String cdpsReference  = theDeployment.getDeploymentConfiguration() != null ? theDeployment.getDeploymentConfiguration().getConfigDeploymentParametersReference() :null;

		createDeploymentApplicationRecords(theDeployment);

		this.deploymentService.delete(theDeployment.getId());

		//delete cpp copy if it is not referenced by deployment or configuration
		logger.info("Check and delete copies of cloud provider and deployment configuration parameter,\n"
				+ "if there are no longer used");
		this.configurationService.deleteCPPAndCDPCopies(cppReference, cdpsReference, deploymentService, configDeploymentParamsCopyService);

		// Prepare response
		HttpHeaders httpHeaders = new HttpHeaders();

		return new ResponseEntity<>(null, httpHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/done/{deploymentReference:.+}", method = RequestMethod.POST)
	// need the regexp above otherwise the content from the last . is missing...
	// http://stackoverflow.com/questions/16332092/spring-mvc-pathvariable-with-dot-is-getting-truncated
	public ResponseEntity<?> readyToTearDown(
			@RequestHeader(value="api-key",required = false) String secret,
			@PathVariable("deploymentReference") String referenceOrIp)
					throws IOException, ApplicationDeployerException,
					NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
					IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {

		logger.info("Deployment '" + referenceOrIp + "' ready to be torn down");

		// First check if this request comes from someone we kinda trust
		if(! "dGhlcG9ydGFsZGV2ZWxvcGVkYnl0c2lpc2F3ZXNvbWU=".equals(secret) ) {
			// be invisible if we don't trust
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}

		String reference = findReference(referenceOrIp);
		Deployment theDeployment = this.deploymentService.findByReference(reference);
		String cppReference = theDeployment.getCloudProviderParametersReference();
		String cdpsReference = theDeployment.getDeploymentConfiguration().getConfigDeploymentParametersReference();
		this.deploymentService.delete(theDeployment.getId());
		logger.info("Check and delete copies of cloud provider and deployment configuration parameter,\n"
				+ "if there are no longer used");
		this.configurationService.deleteCPPAndCDPCopies(cppReference, cdpsReference, deploymentService, configDeploymentParamsCopyService);

		return new ResponseEntity<>("we heard you", HttpStatus.OK);
	}

	private String findReference(String referenceOrIp) {
		if(looksLikeAnIpAddress(referenceOrIp)) {
			Deployment theDeployment = this.deploymentService.findByAccessIp(referenceOrIp);
			return theDeployment.getReference();
		}
		return referenceOrIp;
	}

	private boolean looksLikeAnIpAddress(String something) {
		return IS_IP_ADDRESS.matcher(something).matches();
	}


	private Map<String, String> toProviderIdHashMap(Collection<DeploymentAttachedVolumeResource> attachedVolumes) {
		Map<String, String> res = new HashMap<>();
		for (DeploymentAttachedVolumeResource attachment: attachedVolumes) {
			res.put(
					attachment.getName(),
					this.volumeInstanceService.findByReference(attachment.getVolumeInstanceReference()).getProviderId()
					);
		}
		return res;
	}

	private void createDeploymentApplicationRecords(Deployment deployment){

		logger.info("Checking if old deployments need additional data");

		DeploymentApplication deploymentApplication = deployment.getDeploymentApplication();

		List<Deployment> deployments = this.deploymentService.findByDeploymentApplicationId(deploymentApplication.getId());

		if(deployments.size() > 1){
			List<Deployment> toHaveNewData = deployments.subList(2, deployments.size());

			toHaveNewData.forEach(row -> {
				//now for each row you create new deployment application
				logger.info("Create new deployment application " + deploymentApplication.getName());
				DeploymentApplication newDepApp = new DeploymentApplication(deploymentApplication);


				Collection<DeploymentApplicationCloudProvider> cloudProviders = deploymentApplication.getCloudProviders();	
				List<DeploymentApplicationCloudProvider> toAddCP = new ArrayList<>();
				cloudProviders.forEach(cp -> {
					//create new cloud provider
					logger.info("Create new deployment application cloud provider " + cp.getName());
					DeploymentApplicationCloudProvider newCP = new DeploymentApplicationCloudProvider(cp.getName(),cp.getPath(),
							newDepApp);

					Collection<DeploymentApplicationCloudProviderInput> inputs = cp.getInputs();
					List<DeploymentApplicationCloudProviderInput> toAddCPInputs = new ArrayList<>();
					inputs.forEach(input -> {
						//create new inputs
						logger.info("Create new deployment application cloud provider input" + input.getName());
						DeploymentApplicationCloudProviderInput newInput = new DeploymentApplicationCloudProviderInput(input.getName(), newCP);
						toAddCPInputs.add(newInput);
					});
					newCP.setInputs(toAddCPInputs);

					Collection<DeploymentApplicationCloudProviderOutput> outputs = cp.getOutputs();
					List<DeploymentApplicationCloudProviderOutput> toAddCPOutputs = new ArrayList<>();
					outputs.forEach(output -> {
						//create new outputs
						logger.info("Create new deployment application cloud provider output " + output.getName());
						DeploymentApplicationCloudProviderOutput newOutput = new DeploymentApplicationCloudProviderOutput(output.getName(), newCP);
						toAddCPOutputs.add(newOutput);
					});
					newCP.setOutputs(toAddCPOutputs);

					Collection<DeploymentApplicationCloudProviderVolume> volumes = cp.getVolumes();
					List<DeploymentApplicationCloudProviderVolume> toAddCPVolumes = new ArrayList<>();
					//create new volume
					volumes.forEach(volume -> {
						//create new outputs
						logger.info("Create new deployment application cloud provider voulume " + volume.getName());
						DeploymentApplicationCloudProviderVolume newVolume = new DeploymentApplicationCloudProviderVolume(volume.getName(), newCP);
						toAddCPVolumes.add(newVolume);
					});
					newCP.setVolumes(toAddCPVolumes);
					toAddCP.add(newCP);
				});
				newDepApp.setCloudProviders(toAddCP);
				row.setDeploymentApplication(newDepApp);
				this.deploymentService.save(row);
			});
		}else{
			logger.info("No additional data creation was necessary");
		}
	}

}
