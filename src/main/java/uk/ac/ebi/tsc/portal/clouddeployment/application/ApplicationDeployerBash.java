package uk.ac.ebi.tsc.portal.clouddeployment.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.application.repo.Application;
import uk.ac.ebi.tsc.portal.api.application.repo.ApplicationOutput;
import uk.ac.ebi.tsc.portal.api.application.repo.ApplicationRepository;
import uk.ac.ebi.tsc.portal.api.application.service.ApplicationService;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopy;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyField;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParamsCopyService;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigDeploymentParamCopy;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigDeploymentParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.configuration.repo.Configuration;
import uk.ac.ebi.tsc.portal.api.configuration.service.ConfigDeploymentParamsCopyService;
import uk.ac.ebi.tsc.portal.api.deployment.repo.*;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentService;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDeployerException;
import uk.ac.ebi.tsc.portal.clouddeployment.model.ErrorFromTerraformOutput;
import uk.ac.ebi.tsc.portal.clouddeployment.model.MachineSpecs;
import uk.ac.ebi.tsc.portal.clouddeployment.model.StateFromTerraformOutput;
import uk.ac.ebi.tsc.portal.clouddeployment.model.terraform.TerraformModule;
import uk.ac.ebi.tsc.portal.clouddeployment.model.terraform.TerraformResource;
import uk.ac.ebi.tsc.portal.clouddeployment.model.terraform.TerraformState;
import uk.ac.ebi.tsc.portal.clouddeployment.utils.InputStreamLogger;
import uk.ac.ebi.tsc.portal.clouddeployment.utils.SSHKeyGenerator;
import uk.ac.ebi.tsc.portal.usage.deployment.model.DeploymentDocument;
import uk.ac.ebi.tsc.portal.usage.deployment.model.ParameterDocument;
import uk.ac.ebi.tsc.portal.usage.deployment.service.DeploymentIndexService;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @author Gianni Dalla Torre <giannidallatorre@gmail.com>
 * @author Navis Raj <navis@ebi.ac.uk>
 * @since v0.0.1
 */
@Component
public class ApplicationDeployerBash {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationDeployerBash.class);

	private static final String BASH_COMMAND = "bash";
	private static final String OS_FLOATING_IP = "floating_ip";

	private static final String ERROR_MESSAGE = "error(s) occurred";
	private static final String IMAGE_NAME_ERROR_MESSAGE = "Error resolving image name";
	private static final String TIMEOUT_ERROR_MESSAGE = "Error waiting for instance";
	private static final String QUOTA_EXCEEDED_ERROR_MESSAGE = "Quota exceeded for ";

	private final DeploymentService deploymentService;
	private final CloudProviderParamsCopyService cloudProviderParametersCopyService;
	private final ApplicationService applicationService;
	private final ConfigDeploymentParamsCopyService configDeploymentParamsCopyService;

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

	@Autowired
	public ApplicationDeployerBash(DeploymentRepository deploymentRepository,
			DeploymentStatusRepository deploymentStatusRepository,
			ApplicationRepository applicationRepository,
			DomainService domainService,
			CloudProviderParamsCopyRepository cloudProviderParametersRepository,
			ConfigDeploymentParamsCopyRepository configDeploymentParamsCopyRepository,
			EncryptionService encryptionService,
			@Value("${ecp.security.salt}") final String salt, 
			@Value("${ecp.security.password}") final String password) {
		this.deploymentService = new DeploymentService(deploymentRepository, deploymentStatusRepository);
		this.applicationService = new ApplicationService(applicationRepository, domainService);
		this.cloudProviderParametersCopyService = new CloudProviderParamsCopyService(cloudProviderParametersRepository, encryptionService,
				salt, password);
		this.configDeploymentParamsCopyService = new ConfigDeploymentParamsCopyService(configDeploymentParamsCopyRepository);

	}

	public void deploy(String userEmail,
			Application theApplication,
			String reference,
			String cloudProviderPath,
			Map<String, String> inputAssignments,
			Map<String, String> parameterAssignments,
			Map<String, String> volumeAttachments,
			Map<String, String> configurationParameters,
			CloudProviderParamsCopy cloudProviderParametersCopy,
			Configuration configuration,
			java.sql.Timestamp startTime, 
			String userSshKey) throws IOException,
	ApplicationDeployerException, NoSuchAlgorithmException, NoSuchProviderException {

		DeploymentIndexService deploymentIndexService = new DeploymentIndexService(
				new RestTemplate(),
				this.elasticSearchUrl + "/" + this.elasticSearchIndex,
				this.elasticSearchUsername,
				this.elasticSearchPassword);


		logger.info("Starting deployment of application using bash from repo: " + theApplication.repoPath);

		if (inputAssignments!= null) logger.info("  With " + inputAssignments.keySet().size() + " assigned inputs");
		if (parameterAssignments!= null) logger.info("  With " + parameterAssignments.keySet().size() + " assigned parameters");
		if (volumeAttachments!= null) logger.info("  With " + volumeAttachments.keySet().size() + " attached volumes");
		if (configurationParameters!= null) logger.info("  With " + configurationParameters.keySet().size() + " configuration parameters added ");

		ProcessBuilder processBuilder = new ProcessBuilder(BASH_COMMAND, cloudProviderPath + File.separator + "deploy.sh");

		logger.info("Creating log file at {}", this.deploymentsRoot+File.separator+reference+File.separator+"output.log");
		File logs = new File(this.deploymentsRoot+File.separator+reference+File.separator+"output.log");
		logs.getParentFile().mkdirs();
		logs.createNewFile();
		processBuilder.redirectOutput(logs);
		processBuilder.redirectErrorStream(true);

		Map<String, String> env = processBuilder.environment();
		addGenericProviderCreds(env, cloudProviderParametersCopy);

		logger.info("  With DEPLOYMENTS_ROOT=" + deploymentsRoot);
		logger.info("  With PORTAL_DEPLOYMENT_REFERENCE=" + reference);
		logger.info("  With PORTAL_APP_REPO_FOLDER=" + theApplication.repoPath);

		env.put("PORTAL_DEPLOYMENTS_ROOT", deploymentsRoot);
		env.put("PORTAL_DEPLOYMENT_REFERENCE", reference);
		env.put("PORTAL_APP_REPO_FOLDER", theApplication.repoPath);
		env.put("TF_VAR_key_pair", "demo-key"); 


		//generate keys
		String fileDestination = deploymentsRoot + File.separator + reference + File.separator + reference ;
		logger.info(fileDestination);
		SSHKeyGenerator.generateKeys(userEmail, fileDestination);
		env.put("PRIVATE_KEY", fileDestination);
		env.put("PUBLIC_KEY", fileDestination + ".pub");
		env.put("TF_VAR_private_key_path", fileDestination);
		env.put("TF_VAR_public_key_path", fileDestination + ".pub");
		env.put("portal_private_key_path", fileDestination);
		env.put("portal_public_key_path", fileDestination + ".pub");
		env.put("TF_VAR_portal_private_key_path", fileDestination);
		env.put("TF_VAR_portal_public_key_path", fileDestination + ".pub");

		// pass parameter assignments
		Collection<ParameterDocument> deploymentParamDocs = new LinkedList<>();
		if (parameterAssignments!=null) {
			for (String parameterName : parameterAssignments.keySet()) {
				if (!parameterName.toLowerCase().contains("password")) logger.info("Passing deployment parameter assignment " + parameterName + " assigned to " + parameterAssignments.get(parameterName));
				if (parameterName.toLowerCase().contains("password")) logger.info("Passing deployment parameter assignment " + parameterName);
				env.put("TF_VAR_" + parameterName, parameterAssignments.get(parameterName));
				deploymentParamDocs.add(new ParameterDocument(parameterName, parameterAssignments.get(parameterName)));
			}
		}

		// pass volume assignments
		if (volumeAttachments!=null) {
			for (String volumeName : volumeAttachments.keySet()) {
				logger.info("Passing volume attachment " + volumeName + " to volume instance " + volumeAttachments.get(volumeName));
				env.put("TF_VAR_" + volumeName, volumeAttachments.get(volumeName));
			}
		}

		// pass configurations
		if (configurationParameters!=null) {
			for (String configurationParameterName : configurationParameters.keySet()) {
				if (!configurationParameterName.toLowerCase().contains("password")) logger.info("Adding configuration parameter " + configurationParameterName + " with value " + configurationParameters.get(configurationParameterName));
				if (configurationParameterName.toLowerCase().contains("password")) logger.info("Adding configuration parameter " + configurationParameterName);
				env.put("TF_VAR_" + configurationParameterName, configurationParameters.get(configurationParameterName));
			}
		}

		// pass input assignments
		Collection<ParameterDocument> inputParamDocs = new LinkedList<>();
		if (inputAssignments!=null) {
			for (String inputName : inputAssignments.keySet()) {
				if (!inputName.toLowerCase().contains("password")) logger.info("Passing input assignment " + inputName + " assigned to " + inputAssignments.get(inputName));
				if (inputName.toLowerCase().contains("password")) logger.info("Passing input assignment " + inputName);
				env.put("TF_VAR_" + inputName, inputAssignments.get(inputName));
				inputParamDocs.add(new ParameterDocument(inputName, inputAssignments.get(inputName)));
			}
		}

		//pass sshkey
		if(configuration!=null)  {
			if(userSshKey != null){
				logger.info("Adding user's own ssh_key of configuration");
				env.put("TF_VAR_ssh_key", userSshKey);
				env.put("ssh_key" , userSshKey);
				env.put("profile_public_key" , userSshKey);
				env.put("TF_VAR_profile_public_key" , userSshKey);
			}else{
				logger.info("Adding ssh_key of configuration");
				env.put("TF_VAR_ssh_key" , configuration.getSshKey());
				env.put("ssh_key" , configuration.getSshKey());
				env.put("profile_public_key" , configuration.getSshKey());
				env.put("TF_VAR_profile_public_key" , configuration.getSshKey());
			}
		}

		processBuilder.directory(new File(theApplication.repoPath));

		Process p = processBuilder.start();

		logger.info("Starting deployment index service"); // Index deployment as started
		DeploymentDocument theDeploymentDocument = new DeploymentDocument(
				userEmail,
				reference,
				theApplication.getName(),
				theApplication.getContact(),
				theApplication.getVersion(),
				cloudProviderParametersCopy.getCloudProvider(),
				cloudProviderParametersCopy.getName());
		theDeploymentDocument.setStatus(DeploymentStatusEnum.STARTING.toString());
		theDeploymentDocument.setStartedTime(new Date(System.currentTimeMillis()));
		theDeploymentDocument.setDeploymentInputs(inputParamDocs);
		theDeploymentDocument.setDeploymentParameters(deploymentParamDocs);
		try {
			deploymentIndexService.save(theDeploymentDocument);
		} catch (RestClientException rce) {
			logger.error("DeploymentIndex service not available. Cause: ");
			rce.printStackTrace();
		}

		logger.info("Starting the deployment process");
		Thread newThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line = null;
					logger.info("In application deployer bash:deploy  process input stream");
					while( (line= reader.readLine()) != null){
						logger.info(line);
					}
					logger.info("Exit from process input stream");
					p.waitFor();
					updateDeploymentStatus(deploymentIndexService, reference,
							DeploymentStatusEnum.STARTING, "Interrupted deployment process", null, null, startTime);
				} catch (InterruptedException e) {
					String errorOutput = getOutputFromFile(logs);
					logger.error("There is an interruption while deploying application from " + theApplication.repoPath);
					logger.error(errorOutput);
					// kill the process if alive?
					p.destroy();
					// Set the right deployment status
					Deployment theDeployment = deploymentService.findByReference(reference);
					updateDeploymentStatus(deploymentIndexService, theDeployment.getReference(),
							DeploymentStatusEnum.STARTING_FAILED, "Interrupted deployment process", null, errorOutput, startTime);
					// Throw application deployer exception
					try {
						throw new ApplicationDeployerException(errorOutput);
					} catch (Exception e1) {
						logger.error("There is an Exception while dealing with interruption with application from " + theApplication.repoPath);
						e1.printStackTrace();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error("In ApplicationDeployerBash:deploy error reading input stream");
				}

				if (p.exitValue() != 0) {

					String errorOutput = getOutputFromFile(logs);

					logger.error("There is a non-zero exit code while deploying application from " + theApplication.repoPath);
					//					logger.error("Error: " + errorOutput);

					// kill the process if alive?
					p.destroy();
					// Set the right deployment status
					Deployment theDeployment = deploymentService.findByReference(reference);
					updateDeploymentStatus(deploymentIndexService, theDeployment.getReference(),
							DeploymentStatusEnum.STARTING_FAILED, "Failed deployment process exit code", null, errorOutput, startTime);
					// Throw application deployer exception
					try {
						throw new ApplicationDeployerException(errorOutput);
					} catch (Exception e1) {
						logger.error("There is an Exception while dealing with non-zero code from application from " + theApplication.repoPath);
						logger.error("Error:");
						logger.error(errorOutput);
						e1.printStackTrace();
					}
				} else {
					logger.info("Successfully deployed application from " + theApplication.repoPath);
					Deployment theDeployment = deploymentService.findByReference(reference);

					String output = getOutputFromFile(logs);
					logger.debug(output);
					// Read terraform state file
					ObjectMapper mapper = new ObjectMapper();
					TerraformState terraformState;
					try {
						terraformState = mapper.readValue(
								new File(deploymentsRoot + File.separator + reference + File.separator + "terraform.tfstate"),
								TerraformState.class);
					} catch (IOException e) {
						logger.error("Can't read terraform state file for deployment " + reference);
						terraformState = new TerraformState();
						e.printStackTrace();
					}
					updateDeploymentStatus(deploymentIndexService, theDeployment.getReference(), DeploymentStatusEnum.RUNNING, null, terraformState, null, startTime);
					updateDeploymentOutputs(deploymentIndexService, theDeployment.getReference(), cloudProviderPath, DeploymentStatusEnum.RUNNING_FAILED, configuration, null);
				}
			}
		});
		newThread.start();
	}

	public StateFromTerraformOutput state(String repoPath, 
			String reference,
			String cloudProviderPath, 
			Map<String, String> outputs,
			Configuration configuration,
			String userSshKey) throws IOException, ApplicationDeployerException {

		logger.info("Showing state of reference: " + reference);

		ProcessBuilder processBuilder = new ProcessBuilder(BASH_COMMAND, cloudProviderPath + File.separator + "state.sh");

		Map<String, String> env = processBuilder.environment();
		env.put("PORTAL_DEPLOYMENTS_ROOT", deploymentsRoot);
		env.put("PORTAL_DEPLOYMENT_REFERENCE", reference);

		//pass configurations
		if (configuration!=null) {
			logger.info("Passing configuration parameters for " + configuration.getName() + " added to deployment" );
			Set<ConfigDeploymentParamCopy> configurationCopyParameters = this.configDeploymentParamsCopyService.findByConfigurationDeploymentParametersReference(configuration.getConfigDeployParamsReference()).getConfigDeploymentParamCopy();
			configurationCopyParameters.forEach((cdp) -> {
				logger.info("Passing configuration parameter " + cdp.getKey() + " with value " + cdp.getValue() );
				env.put("TF_VAR_" + cdp.getKey(), cdp.getValue());
			});

			if(userSshKey != null){
				logger.info("Adding user's own ssh_key of configuration");
				env.put("TF_VAR_ssh_key", userSshKey);
				env.put("ssh_key" , userSshKey);
				env.put("profile_public_key" , userSshKey);
				env.put("TF_VAR_profile_public_key" , userSshKey);
			}else{
				logger.info("Adding ssh_key of configuration");
				env.put("TF_VAR_ssh_key" , configuration.getSshKey());
				env.put("ssh_key" , configuration.getSshKey());
				env.put("profile_public_key" , configuration.getSshKey());
				env.put("TF_VAR_profile_public_key" , configuration.getSshKey());
			}	
		}

		//generate keys
		String fileDestination = deploymentsRoot + File.separator + reference + File.separator + reference ;
		logger.info(fileDestination);
		//list of environment variables, come up with a standardized approach
		env.put("PRIVATE_KEY", fileDestination);
		env.put("PUBLIC_KEY", fileDestination + ".pub");
		env.put("TF_VAR_private_key_path", fileDestination);
		env.put("TF_VAR_public_key_path", fileDestination + ".pub");
		env.put("portal_private_key_path", fileDestination);
		env.put("portal_public_key_path", fileDestination + ".pub");
		env.put("TF_VAR_portal_private_key_path", fileDestination);
		env.put("TF_VAR_portal_public_key_path", fileDestination + ".pub");


		processBuilder.directory(new File(repoPath));

		Process p = processBuilder.start();

		try {
			p.waitFor();
		} catch (InterruptedException e) {
			logger.error("There is an error showing application " + reference);
			String errorOutput = InputStreamLogger.logInputStream(p.getErrorStream());
			logger.error(errorOutput);
			throw new ApplicationDeployerException(errorOutput);
		}

		if (p.exitValue() != 0) {
			logger.error("There is an error showing application " + reference);
			String errorOutput = InputStreamLogger.logInputStream(p.getErrorStream());
			logger.error(errorOutput);
			throw new ApplicationDeployerException(errorOutput);
		} else {
			String output = InputStreamLogger.logInputStream(p.getInputStream());
			logger.debug(output);
			return terraformStateFromString(output, outputs);
		}

	}

	public void destroy(String repoPath, String reference,
			String cloudProviderPath,
			Collection<DeploymentAssignedInput> inputAssignments,
			Collection<DeploymentAssignedParameter> parameterAssignments,
			Collection<DeploymentAttachedVolume> volumeAttachments,
			DeploymentConfiguration deploymentConfiguration, 
			CloudProviderParamsCopy cloudProviderParametersCopy) throws IOException,
	ApplicationDeployerException {

		DeploymentIndexService deploymentIndexService = new DeploymentIndexService(
				new RestTemplate(),
				this.elasticSearchUrl + "/" + this.elasticSearchIndex,
				this.elasticSearchUsername,
				this.elasticSearchPassword);

		logger.info("Destroying deployment of application: " + reference);
		if (inputAssignments!= null) logger.info("  With " + inputAssignments.size() + " assigned inputs");
		if (parameterAssignments!= null) logger.info("  With " + parameterAssignments.size() + " assigned parameters");
		if (volumeAttachments!= null) logger.info("  With " + volumeAttachments.size() + " attached volumes");

		String path =  deploymentsRoot + File.separator + reference;

		ProcessBuilder processBuilder = new ProcessBuilder(BASH_COMMAND, cloudProviderPath + File.separator + "destroy.sh");

		logger.info("Creating log file at {}", this.deploymentsRoot+File.separator+reference+File.separator+"destroy.log");
		File logs = new File(this.deploymentsRoot+File.separator+reference+File.separator+"destroy.log");
		logs.getParentFile().mkdirs();
		logs.createNewFile();
		processBuilder.redirectOutput(logs);
		processBuilder.redirectErrorStream(true);

		Map<String, String> env = processBuilder.environment();



		addGenericProviderCreds(env, cloudProviderParametersCopy);
		env.put("PORTAL_DEPLOYMENTS_ROOT", deploymentsRoot);
		env.put("PORTAL_DEPLOYMENT_REFERENCE", reference);
		env.put("PORTAL_APP_REPO_FOLDER", repoPath);

		//generate keys
		String fileDestination = deploymentsRoot + File.separator + reference + File.separator + reference ;
		logger.info(fileDestination);
		env.put("PRIVATE_KEY", fileDestination);
		env.put("PUBLIC_KEY", fileDestination + ".pub");
		env.put("TF_VAR_private_key_path", fileDestination);
		env.put("TF_VAR_public_key_path", fileDestination + ".pub");
		env.put("portal_private_key_path", fileDestination);
		env.put("portal_public_key_path", fileDestination + ".pub");
		env.put("TF_VAR_portal_private_key_path", fileDestination);
		env.put("TF_VAR_portal_public_key_path", fileDestination + ".pub");

		// pass input assignments
		if (inputAssignments!=null) {
			for (DeploymentAssignedInput input : inputAssignments) {
				logger.info("Passing input assignment " + input.getValue() + " assigned to " + input.getInputName());
				env.put("TF_VAR_" + input.getInputName(), input.getValue());
			}
		}

		// pass parameter assignments
		if (parameterAssignments!=null) {
			for (DeploymentAssignedParameter parameter : parameterAssignments) {
				logger.info("Passing parameter assignment " + parameter.getParameterValue() + " assigned to " + parameter.getParameterValue());
				env.put("TF_VAR_" + parameter.getParameterName(), parameter.getParameterValue());
			}
		}

		// pass volume assignments
		if (volumeAttachments!=null) {
			for (DeploymentAttachedVolume volume : volumeAttachments)  {
				logger.info("Passing volume attachment " + volume.getVolumeInstanceProviderId() + " to volume instance " + volume.getVolumeInstanceReference());
				env.put("TF_VAR_" + volume.getVolumeInstanceReference(), volume.getVolumeInstanceProviderId());
			}
		}

		//pass configurations
		if (deploymentConfiguration!=null) {
			logger.info("Passing  deploymnet configuration parameters for " + deploymentConfiguration.getName() + " added to deployment" );

			deploymentConfiguration.getConfigurationParameters().forEach((cdp) -> {
				logger.info("Passing deployment configuration parameter " + cdp.getParameterName()+ " with value " + cdp.getParameterValue() );
				env.put("TF_VAR_" + cdp.getParameterName(), cdp.getParameterValue());
			});

			logger.info("Adding ssh_key of configuration");
			env.put("TF_VAR_ssh_key" , deploymentConfiguration.getSshKey());
			env.put("ssh_key" , deploymentConfiguration.getSshKey());
			env.put("profile_public_key" , deploymentConfiguration.getSshKey());
			env.put("TF_VAR_profile_public_key" , deploymentConfiguration.getSshKey());
		}

		processBuilder.directory(new File(repoPath));

		logger.info("Destroying deployment of application at: " + path);
		logger.info("- Provider path at " + cloudProviderPath);
		logger.info("- With Cloud Provider Parameters Copy '" + cloudProviderParametersCopy.getName() + "'");

		Process p = processBuilder.start();

		Thread newThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line = null;
					logger.info("In application deployer bash:destroy process input stream");
					while( (line= reader.readLine()) != null){
						logger.info(line);
					}
					logger.info("Exit from process input stream");
					p.waitFor();
				} catch (InterruptedException e) {
					logger.error("There is an interruption while destroying application from " + repoPath);
					String errorOutput = getOutputFromFile(logs);
					logger.error(errorOutput);
					// kill the process if alive?
					p.destroy();
					// Set the right deployment status
					Deployment theDeployment = deploymentService.findByReference(reference);
					updateDeploymentStatus(deploymentIndexService, theDeployment.getReference(),
							DeploymentStatusEnum.DESTROYING_FAILED, "Interrupted destroy process",
							null, errorOutput, null);
					// Throw application deployer exception
					try {
						throw new ApplicationDeployerException(errorOutput);
					} catch (Exception e1) {
						logger.error("There is an Exception while dealing with interruption with application from " + repoPath);
						e1.printStackTrace();
					}
				} catch (IOException e) {
					logger.error("In ApplicationDeployerBash:destroy error reading input stream");
				}

				if (p.exitValue() != 0) {
					logger.error("There is a non-zero exit code while destroying application from " + repoPath);
					String errorOutput = getOutputFromFile(logs);
					logger.error(errorOutput);
					// kill the process if alive?
					p.destroy();
					// Set the right deployment status
					Deployment theDeployment = deploymentService.findByReference(reference);
					updateDeploymentStatus(deploymentIndexService, theDeployment.getReference(),
							DeploymentStatusEnum.DESTROYING_FAILED,
							"Failed destroy process exit code", null, errorOutput, null);
					// Throw application deployer exception
					try {
						throw new ApplicationDeployerException(errorOutput);
					} catch (Exception e1) {
						logger.error("There is an Exception while dealing with non-zero code from application from " + repoPath);
						e1.printStackTrace();
					}
				} else {
					logger.info("Successfully destroyed application from " + repoPath);
					try {
						logger.info(InputStreamLogger.logInputStream(p.getInputStream()));
					} catch (IOException e) {
						e.printStackTrace();
					}
					Deployment theDeployment = deploymentService.findByReference(reference);
					updateDeploymentStatus(deploymentIndexService, theDeployment.getReference(),
							DeploymentStatusEnum.DESTROYED, null,
							null, null, null);
					//updateDeploymentOutputs(theDeployment.getReference(), cloudProviderPath, DeploymentStatusEnum.DESTROYING_FAILED, deploymentsRoot);
				}
			}
		});
		newThread.start();

	}

	protected String getOutputFromFile(File file) {
		logger.info("Retrieving output for process from " + file.getAbsolutePath());

		String output = null;
		try {
			output = new Scanner(file).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			logger.error("Can't read output from file " + file.getAbsolutePath());
			e.printStackTrace();
		}

		return output;
	}


	private void addGenericProviderCreds(Map<String, String> env, CloudProviderParamsCopy cloudProviderCredentialsCopy) {
		logger.info("Setting Cloud Provider Parameters Copy for " + cloudProviderCredentialsCopy.getName());

		for (CloudProviderParamsCopyField cloudProviderParametersCopyField : cloudProviderCredentialsCopy.getFields()) {
			logger.info("Setting " + cloudProviderParametersCopyField.getKey());
			env.put(cloudProviderParametersCopyField.getKey(), cloudProviderParametersCopyField.getValue());
		}
	}

	private StateFromTerraformOutput terraformStateFromString(String output, Map<String, String> outputs) throws IOException {

		logger.info("The whole terraform state is: " + output);

		StateFromTerraformOutput stateFromTerraformOutput = new StateFromTerraformOutput();

		String[] lines = output.split(System.getProperty("line.separator"));

		// Read Id, it should be the first ID (TODO improve this later)
		stateFromTerraformOutput.setId(lines[1].replaceAll(" ","").split("=")[1]);
		// look for the IP
		Pattern osFloatingIpPattern = Pattern.compile(OS_FLOATING_IP);
		boolean ipFound = false;
		int i=1;
		while (!ipFound && i<lines.length) {
			String line = lines[i].replaceAll(" ","");
			Matcher osFloatingIpMatcher = osFloatingIpPattern.matcher(line);
			if (osFloatingIpMatcher.lookingAt()) {
				// Read IP
				String ip = line.split("=")[1].replaceAll("\n", "").replaceAll("\r", "");
				logger.debug("There is a match. IP = " + ip);
				stateFromTerraformOutput.setAccessIp(ip);
				ipFound = true;
			}
			i++;
		}
		// parse for any other outputs
		if (outputs != null) {
			getOutputs(lines, outputs);
		}

		return stateFromTerraformOutput;
	}

	private ErrorFromTerraformOutput errorFromTerraformOutput(String terraformOutput) {
		ErrorFromTerraformOutput res = new ErrorFromTerraformOutput();
		res.setErrorMessage("unknown");

		// look for specific error message

		if (terraformOutput.contains(TIMEOUT_ERROR_MESSAGE)) {
			logger.info("Timeout error");
			res.setErrorMessage("timeout");
			return res;
		}

		if (terraformOutput.contains(QUOTA_EXCEEDED_ERROR_MESSAGE)) {
			logger.info("Quota exceeded error");
			res.setErrorMessage("quota exceeded");
			return res;
		}

		if (terraformOutput.contains(IMAGE_NAME_ERROR_MESSAGE)) {
			logger.info("Error resolving image name");
			res.setErrorMessage("unresolvable image name");
			return res;
		}

		return res;
	}

	private void getOutputs(String[] lines, Map<String, String> outputs) {
		for (int i = 0; i<lines.length; i++) {
			String line = lines[i].replaceAll(" ","");
			String[] lineSplit = line.split("=");
			if ( outputs.containsKey(lineSplit[0]) ) {
				outputs.put(lineSplit[0], lineSplit[1].replaceAll("\n", "").replaceAll("\r", "").replaceAll("\\[0m","").trim());
				logger.debug("There is a match. " + lineSplit[0] + " = " + outputs.get(lineSplit[0]));
			}
		}
	}

	private void updateDeploymentStatus(DeploymentIndexService deploymentIndexService, String deploymentReference,
			DeploymentStatusEnum status, String cause, TerraformState terraformState, String terraformOutput,
			java.sql.Timestamp startTime) {
		Deployment theDeployment = this.deploymentService.findByReference(deploymentReference);
		// update status
		theDeployment.getDeploymentStatus().setStatus(status);
		// persist changes
		this.deploymentService.save(theDeployment);

		CloudProviderParamsCopy cloudProviderParametersCopy = this.cloudProviderParametersCopyService.
				findByCloudProviderParametersReference(theDeployment.getCloudProviderParametersReference());


		// Index deployment
		if (deploymentIndexService != null) {
			DeploymentDocument theDeploymentDocument = deploymentIndexService.findById(deploymentReference);
			if (theDeploymentDocument == null) {
				theDeploymentDocument = new DeploymentDocument(
						theDeployment.getAccount().getEmail(),
						theDeployment.getReference(),
						theDeployment.getDeploymentApplication().getName(),
						theDeployment.getDeploymentApplication().getContact(),
						theDeployment.getDeploymentApplication().getVersion(),
						theDeployment.getDeploymentApplication().getName(),
						cloudProviderParametersCopy.cloudProvider);
			}
			// set resources if provided
			if (terraformState!=null) {
				updateResourceConsumptionFromTerraformState(terraformState, theDeploymentDocument);
			}
			// set status
			theDeploymentDocument.setStatus(status.toString());
			switch (status) {
			case STARTING:
				theDeploymentDocument.setStartedTime(new Date(System.currentTimeMillis()));
				//start time
				theDeployment.setStartTime(startTime);
				break;
			case RUNNING:
				//deployed time
				theDeploymentDocument.setDeployedTime(new Date(System.currentTimeMillis()));
				theDeployment.setDeployedTime(new java.sql.Timestamp(System.currentTimeMillis()));
				break;
			case DESTROYED:
				theDeploymentDocument.setDestroyedTime(new Date(System.currentTimeMillis()));
				theDeployment.setDestroyedTime(new java.sql.Timestamp(System.currentTimeMillis()));
				if (theDeploymentDocument.getDeployedTime()!=null) {
					theDeploymentDocument.setTotalRunningTime(
							theDeploymentDocument.getDestroyedTime().getTime() - theDeploymentDocument.getDeployedTime().getTime()
							);
				}
				break;
			case STARTING_FAILED:
			case RUNNING_FAILED:
			case DESTROYING_FAILED:
				//start time, would be null for detroying_failed
				if(startTime != null){
					theDeployment.setStartTime(startTime);
				}
				theDeploymentDocument.setFailedTime(new Date(System.currentTimeMillis()));
				theDeployment.setFailedTime(new java.sql.Timestamp(System.currentTimeMillis()));
				theDeploymentDocument.setErrorCause(cause);
				ErrorFromTerraformOutput error = errorFromTerraformOutput(terraformOutput);
				theDeploymentDocument.setErrorCause(error.getErrorMessage());
				break;
			}
			try {
				deploymentIndexService.save(theDeploymentDocument);
			} catch (RestClientException rce) {
				logger.error("DeploymentIndex service not available. Cause: ");
				rce.printStackTrace();
			}
			logger.info("Saving deployment status times");
			this.deploymentService.save(theDeployment);
		}

	}


	private void updateDeploymentOutputs(DeploymentIndexService deploymentIndexService, String deploymentReference, String cloudProviderPath, DeploymentStatusEnum failStatus, Configuration configuration, String terraformOutput) {
		Deployment theDeployment = this.deploymentService.findByReference(deploymentReference);
		Application theApplication = this.applicationService.findByAccountUsernameAndName(
				theDeployment.getDeploymentApplication().getAccount().getUsername(),
				theDeployment.getDeploymentApplication().getName());

		// get deployment state
		Map<String,String> deploymentOutputs = theApplication.getOutputs().stream()
				.collect(Collectors.toMap(ApplicationOutput::getName, s -> ""));
		StateFromTerraformOutput stateFromTerraformOutput = null;
		try {
			stateFromTerraformOutput = this.state(
					theApplication.getRepoPath(),
					theDeployment.getReference(),
					cloudProviderPath,
					deploymentOutputs,
					configuration,
					theDeployment.getUserSshKey()
					);


			// update the deployment
			theDeployment.setAccessIp(stateFromTerraformOutput.getAccessIp());
			theDeployment.setProviderId(stateFromTerraformOutput.getId()!=null ? stateFromTerraformOutput.getId():"");

			// set outputs
			theApplication.getOutputs().forEach(output ->  {
				logger.debug( "Adding output " + output.getName() + " = " + deploymentOutputs.get(output.getName()) );
				//                        logger.info( "  with length " + deploymentOutputs.get(output.getName()).length());
				DeploymentGeneratedOutput newOutput = new DeploymentGeneratedOutput(
						output.getName(),
						deploymentOutputs.get(output.getName()),
						theDeployment
						);
				//                        logger.info( "  with new length " + newOutput.getValue().length());
				theDeployment.getGeneratedOutputs().add(newOutput);
			}
					);

		} catch (IOException | ApplicationDeployerException e) {
			e.printStackTrace();
			// update status
			this.updateDeploymentStatus(deploymentIndexService, deploymentReference,
					failStatus,"Failed to obtain outputs from state file", null, terraformOutput, null);
		}
		// persist changes
		this.deploymentService.save(theDeployment);
	}

	private long updateResourceConsumptionFromTerraformState(TerraformState terraformState, DeploymentDocument deploymentDocument) {
		long res = 0;
		if (terraformState.modules!=null && terraformState.modules.length>0) {
			Iterator<TerraformModule> itModules = Arrays.stream(terraformState.modules).iterator();
			while (itModules.hasNext()) {
				TerraformModule terraformModule = itModules.next();
				Iterator<Map.Entry<String, TerraformResource>> it = terraformModule.resources.entrySet().iterator();
				while (it.hasNext()) {
					TerraformResource terraformResource = it.next().getValue();
					if (terraformResource.type.equals("openstack_compute_instance_v2")) {
						deploymentDocument.setInstanceCount(deploymentDocument.getInstanceCount()+1);
						deploymentDocument.setTotalVcpus(
								deploymentDocument.getTotalVcpus()
								+ MachineSpecs.fromFlavourName(terraformResource.primary.attributes.get("flavor_name")).getvCPUSs()
								);
						deploymentDocument.setTotalRamGb(
								deploymentDocument.getTotalRamGb()
								+ MachineSpecs.fromFlavourName(terraformResource.primary.attributes.get("flavor_name")).getRamGb()
								);
						deploymentDocument.setTotalDiskGb(
								deploymentDocument.getTotalDiskGb()
								+ MachineSpecs.fromFlavourName(terraformResource.primary.attributes.get("flavor_name")).getDiskSpaceGb()
								);
					} else if (terraformResource.type.equals("google_compute_instance")) {
						deploymentDocument.setInstanceCount(deploymentDocument.getInstanceCount()+1);
						deploymentDocument.setTotalVcpus(
								deploymentDocument.getTotalVcpus()
								+ MachineSpecs.fromFlavourName(terraformResource.primary.attributes.get("machine_type")).getvCPUSs()
								);
						deploymentDocument.setTotalRamGb(
								deploymentDocument.getTotalRamGb()
								+ MachineSpecs.fromFlavourName(terraformResource.primary.attributes.get("machine_type")).getRamGb()
								);
						deploymentDocument.setTotalDiskGb(
								deploymentDocument.getTotalDiskGb()
								+ MachineSpecs.fromFlavourName(terraformResource.primary.attributes.get("machine_type")).getDiskSpaceGb()
								);
					} else if (terraformResource.type.equals("aws_instance")) {
						deploymentDocument.setInstanceCount(deploymentDocument.getInstanceCount()+1);
						deploymentDocument.setTotalVcpus(
								deploymentDocument.getTotalVcpus()
								+ MachineSpecs.fromFlavourName(terraformResource.primary.attributes.get("instance_type")).getvCPUSs()
								);
						deploymentDocument.setTotalRamGb(
								deploymentDocument.getTotalRamGb()
								+ MachineSpecs.fromFlavourName(terraformResource.primary.attributes.get("instance_type")).getRamGb()
								);
						deploymentDocument.setTotalDiskGb(
								deploymentDocument.getTotalDiskGb()
								+ MachineSpecs.fromFlavourName(terraformResource.primary.attributes.get("instance_type")).getDiskSpaceGb()
								);
					} else if (terraformResource.type.equals("azurerm_virtual_machine")) {
						deploymentDocument.setInstanceCount(deploymentDocument.getInstanceCount()+1);
						deploymentDocument.setTotalVcpus(
								deploymentDocument.getTotalVcpus()
								+ MachineSpecs.fromFlavourName(terraformResource.primary.attributes.get("vm_size")).getvCPUSs()
								);
						deploymentDocument.setTotalRamGb(
								deploymentDocument.getTotalRamGb()
								+ MachineSpecs.fromFlavourName(terraformResource.primary.attributes.get("vm_size")).getRamGb()
								);
						deploymentDocument.setTotalDiskGb(
								deploymentDocument.getTotalDiskGb()
								+ MachineSpecs.fromFlavourName(terraformResource.primary.attributes.get("vm_size")).getDiskSpaceGb()
								);
					}
				}
			}
		}
		return res;
	}
}