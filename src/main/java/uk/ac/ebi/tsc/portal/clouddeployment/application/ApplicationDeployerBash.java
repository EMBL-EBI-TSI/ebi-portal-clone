package uk.ac.ebi.tsc.portal.clouddeployment.application;

import static com.github.underscore.U.chain;
import static com.github.underscore.U.concat;
import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.application.repo.Application;
import uk.ac.ebi.tsc.portal.api.application.repo.ApplicationRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopy;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigDeploymentParamCopy;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigDeploymentParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.configuration.repo.Configuration;
import uk.ac.ebi.tsc.portal.api.deployment.repo.Deployment;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentAssignedInput;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentAssignedParameter;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentAttachedVolume;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentConfiguration;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentStatusEnum;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentSecretService;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentService;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDeployerException;
import uk.ac.ebi.tsc.portal.clouddeployment.model.StateFromTerraformOutput;
import uk.ac.ebi.tsc.portal.clouddeployment.model.terraform.TerraformState;
import uk.ac.ebi.tsc.portal.clouddeployment.utils.InputStreamLogger;
import uk.ac.ebi.tsc.portal.clouddeployment.utils.SSHKeyGenerator;
import uk.ac.ebi.tsc.portal.usage.deployment.model.DeploymentDocument;
import uk.ac.ebi.tsc.portal.usage.deployment.model.ParameterDocument;
import uk.ac.ebi.tsc.portal.usage.deployment.service.DeploymentIndexService;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @author Gianni Dalla Torre <giannidallatorre@gmail.com>
 * @author Navis Raj <navis@ebi.ac.uk>
 * @since v0.0.1
 */
@Component
public class ApplicationDeployerBash extends AbstractApplicationDeployer {

	static final String CONTAINER_APP_FOLDER = "/app";

    static final String CONTAINER_DEPLOYMENTS_FOLDER = "/deployments";

	private static final Logger logger = LoggerFactory.getLogger(ApplicationDeployerBash.class);

	private static final String BASH_COMMAND = "bash";

	@Value("${be.deployments.root}")
	String deploymentsRoot;

	@Value("${elasticsearch.url}")
	private String elasticSearchUrl;

	@Value("${elasticsearch.index}")
	private String elasticSearchIndex;

	@Value("${elasticsearch.username}")
	private String elasticSearchUsername;

	@Value("${elasticsearch.password}")
	private String elasticSearchPassword;

    boolean docker = true;


	@Autowired
	public ApplicationDeployerBash(DeploymentService deploymentService,
	                               ApplicationRepository applicationRepository,
								   DomainService domainService,
								   CloudProviderParamsCopyRepository cloudProviderParametersRepository,
								   ConfigDeploymentParamsCopyRepository configDeploymentParamsCopyRepository,
								   EncryptionService encryptionService,
								   DeploymentSecretService secretService) {
	    super(  deploymentService,
	            applicationRepository,
                domainService,
                cloudProviderParametersRepository,
                configDeploymentParamsCopyRepository,
                encryptionService,
				secretService
        );
	}

	@Override
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
			String userSshKey,
			String baseUrl) throws IOException,
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

  		ProcessBuilder processBuilder = new ProcessBuilder();
                                                                          
        Map<String, String> env = new HashMap<>();
        
		logger.info("Creating log file at {}", this.deploymentsRoot+File.separator+reference+File.separator+"output.log");
		File logs = new File(this.deploymentsRoot+File.separator+reference+File.separator+"output.log");
		logs.getParentFile().mkdirs();
		logs.createNewFile();
		processBuilder.redirectOutput(logs);
		processBuilder.redirectErrorStream(true);

        logger.info("Looking for deployment " + reference);
		Deployment theDeployment = deploymentService.findByReference(reference);
        logger.info("Can't find deployment " + reference);

		ApplicationDeployerHelper.addGenericProviderCreds(env, cloudProviderParametersCopy, logger);

		logger.info("  With DEPLOYMENTS_ROOT=" + deploymentsRoot);
		logger.info("  With PORTAL_DEPLOYMENT_REFERENCE=" + reference);
		logger.info("  With PORTAL_APP_REPO_FOLDER=" + theApplication.repoPath);

		env.put("PORTAL_CALLBACK_SECRET", secretService.create(theDeployment));
		env.put("PORTAL_BASE_URL", baseUrl);
		
		env.put("TF_VAR_key_pair", "demo-key"); 


		//generate keys
		String keysFilePath = deploymentsRoot + File.separator + reference + File.separator + reference ;
		logger.info(keysFilePath);
		SSHKeyGenerator.generateKeys(userEmail, keysFilePath);
		
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

		String appFolder = theApplication.repoPath;
        String deploymentsFolder = this.deploymentsRoot;
        
        configureProcessBuilder(processBuilder, cloudProviderPath, env, appFolder, deploymentsFolder, reference, "deploy.sh");
        
		Process p = startProcess(processBuilder);

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
					Deployment theDeployment = deploymentService.findByReference(reference);
					updateDeploymentStatus(deploymentIndexService, theDeployment,
							DeploymentStatusEnum.STARTING, "Interrupted deployment process",
                            null, null, startTime);
					deploymentService.save(theDeployment);
				} catch (InterruptedException e) {
					String errorOutput = ApplicationDeployerHelper.getOutputFromFile(logs, logger);
					logger.error("There is an interruption while deploying application from " + theApplication.repoPath);
					logger.error(errorOutput);
					// kill the process if alive?
					p.destroy();
					// Set the right deployment status
					Deployment theDeployment = deploymentService.findByReference(reference);
					updateDeploymentStatus(deploymentIndexService,
                            deploymentService.findByReference(theDeployment.getReference()),
							DeploymentStatusEnum.STARTING_FAILED, "Interrupted deployment process",
                            null, errorOutput, startTime);
					deploymentService.save(theDeployment);
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

					String errorOutput = ApplicationDeployerHelper.getOutputFromFile(logs, logger);

					logger.error("There is a non-zero exit code while deploying application from " + theApplication.repoPath);
					//					logger.error("Error: " + errorOutput);

					// kill the process if alive?
					p.destroy();
					// Set the right deployment status
					Deployment theDeployment = deploymentService.findByReference(reference);
					updateDeploymentStatus(deploymentIndexService, theDeployment,
							DeploymentStatusEnum.STARTING_FAILED, "Failed deployment process exit code", null, errorOutput, startTime);
					deploymentService.save(theDeployment);
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

					String output = ApplicationDeployerHelper.getOutputFromFile(logs, logger);
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
					updateDeploymentStatus(deploymentIndexService, theDeployment, DeploymentStatusEnum.RUNNING,
                            null, terraformState, null, startTime);
                    updateDeploymentOutputs(deploymentIndexService, applicationService, theDeployment, cloudProviderPath,
                            DeploymentStatusEnum.RUNNING_FAILED, configuration, null);
                    deploymentService.save(theDeployment);
				}
			}
		});
		newThread.start();
	}

    void configureProcessBuilder( ProcessBuilder processBuilder
                                , String cloudProviderPath
                                , Map<String, String> env
                                , String appFolder
                                , String deploymentsFolder
                                , String reference
                                , String script
                                )
    {
        if (this.docker) configureDocker(processBuilder, cloudProviderPath, env, appFolder, deploymentsFolder, reference, script);
                    else configureBash  (processBuilder, cloudProviderPath, env, appFolder, deploymentsFolder, reference, script);
        
    }
	
	void configureDocker( ProcessBuilder processBuilder
                        , String cloudProviderPath
                        , Map<String, String> env
                        , String appFolder
                        , String deploymentsFolder
                        , String reference
                        , String script
                        )
	{
	    // Env
	    setEnvDocker(env, reference);
	    addKeyEnvVars(env, reference);
	    
	    // Command
	    processBuilder.command(dockerCmd(appFolder, deploymentsFolder, cloudProviderPath, script, env));
	}
	
    void configureBash( ProcessBuilder processBuilder
                      , String cloudProviderPath
                      , Map<String, String> env
                      , String appFolder
                      , String deploymentsFolder
                      , String reference
                      , String script
                      )
    {
        // Env
        setEnvBash(env, reference, deploymentsRoot, appFolder);
        addKeyEnvVars(env, reference);
        
        Map<String, String> hostEnv = processBuilder.environment();
        hostEnv.putAll(env);
        
        // Command
        processBuilder.command(BASH_COMMAND, cloudProviderPath + File.separator + script);
        
        // Working dir
        processBuilder.directory(new File(appFolder));
    }

    @SuppressWarnings("unchecked")
    List<String> dockerCmd(String appFolder, String deploymentsFolder, String cloudProviderPath, String script, Map<String, String> env) {
        
        return concat(
            
              asList("docker", "run")
            , volume(appFolder         , CONTAINER_APP_FOLDER)           // appFolder
            , volume(deploymentsFolder , CONTAINER_DEPLOYMENTS_FOLDER)   // deploymentFolder
            , envToOpts(env)
            , asList("-w", CONTAINER_APP_FOLDER)                         // working dir
            , asList( "--entrypoint", ""                                 // disable erik's image entry-point
                    , "erikvdbergh/ecp-agent"                            // erik's image
                    , scriptPath(cloudProviderPath, script)              // script path
                    )
        );
    }
    
    @SuppressWarnings("unchecked")
    List<String> envToOpts(Map<String, String> env) {
        
      return chain(new ArrayList<>(env.entrySet()))
             .map(e -> asList("-e", e.toString()))
             .flatten()
             .value()
             ;
    }

    String scriptPath(String cloudProviderPath, String script) {
        
        return new File(
                     new File(CONTAINER_APP_FOLDER, cloudProviderPath)
                    ,script)
               .toString()
               ;
    }

    void setEnvDocker(Map<String, String> env, String reference) {
        
        env.put("PORTAL_DEPLOYMENTS_ROOT"       , CONTAINER_DEPLOYMENTS_FOLDER);
        env.put("PORTAL_APP_REPO_FOLDER"        , CONTAINER_APP_FOLDER);
        env.put("PORTAL_DEPLOYMENT_REFERENCE"   , reference);
    }
    
    void setEnvBash(Map<String, String> env, String reference, String deploymentsRoot, String repoPath) {
        
        env.put("PORTAL_DEPLOYMENTS_ROOT"       , deploymentsRoot);
        env.put("PORTAL_APP_REPO_FOLDER"        , repoPath);
        env.put("PORTAL_DEPLOYMENT_REFERENCE"   , reference);
    }
    
    void addKeyEnvVars(Map<String, String> env, String reference) {
        
        deploymentsRoot = this.docker ? CONTAINER_DEPLOYMENTS_FOLDER
                                      : this.deploymentsRoot
                                      ;
        
        String fileDestination = deploymentsRoot + File.separator + reference + File.separator + reference;
        
        String privateKeyPath = fileDestination;
        String publicKeyPath = privateKeyPath + ".pub";
        
        env.put("PRIVATE_KEY", privateKeyPath);
        env.put("PUBLIC_KEY", publicKeyPath);
        env.put("TF_VAR_private_key_path", privateKeyPath);
        env.put("TF_VAR_public_key_path", publicKeyPath);
        env.put("portal_private_key_path", privateKeyPath);
        env.put("portal_public_key_path", publicKeyPath);
        env.put("TF_VAR_portal_private_key_path", privateKeyPath);
        env.put("TF_VAR_portal_public_key_path", publicKeyPath);
    }

    Process startProcess(ProcessBuilder processBuilder) throws IOException {
        
        return processBuilder.start();
    }

    List<String> volume(String folder, String mountPoint) {
        
        return folder == null ? asList()
                              : asList("-v", format("%s:%s", folder, mountPoint))
                              ;
    }

	public StateFromTerraformOutput state(String repoPath, 
			String reference,
			String cloudProviderPath, 
			Map<String, String> outputs,
			Configuration configuration,
			String userSshKey) throws IOException, ApplicationDeployerException {

		logger.info("Showing state of reference: " + reference);

		ProcessBuilder processBuilder = new ProcessBuilder();

		Map<String, String> env = new HashMap<>();

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

        configureProcessBuilder(processBuilder, cloudProviderPath, env, repoPath, this.deploymentsRoot, reference, "state.sh");

		Process p = startProcess(processBuilder);

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
			return ApplicationDeployerHelper.terraformStateFromString(output, outputs, logger);
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

		ProcessBuilder processBuilder = new ProcessBuilder();

		logger.info("Creating log file at {}", this.deploymentsRoot+File.separator+reference+File.separator+"destroy.log");
		File logs = new File(this.deploymentsRoot+File.separator+reference+File.separator+"destroy.log");
		logs.getParentFile().mkdirs();
		logs.createNewFile();
		processBuilder.redirectOutput(logs);
		processBuilder.redirectErrorStream(true);

		Map<String, String> env = new HashMap<>();

		ApplicationDeployerHelper.addGenericProviderCreds(env, cloudProviderParametersCopy, logger);
		setEnvDocker(env, reference);

		//generate keys
		addKeyEnvVars(env, reference);

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

		logger.info("Destroying deployment of application at: " + path);
		logger.info("- Provider path at " + cloudProviderPath);
		logger.info("- With Cloud Provider Parameters Copy '" + cloudProviderParametersCopy.getName() + "'");

        processBuilder.command(dockerCmd(repoPath, this.deploymentsRoot, cloudProviderPath, "destroy.sh", env));
		
		Process p = startProcess(processBuilder);

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
					String errorOutput = ApplicationDeployerHelper.getOutputFromFile(logs, logger);
					logger.error(errorOutput);
					// kill the process if alive?
					p.destroy();
					// Set the right deployment status
					Deployment theDeployment = deploymentService.findByReference(reference);
					updateDeploymentStatus(deploymentIndexService, theDeployment,
							DeploymentStatusEnum.DESTROYING_FAILED, "Interrupted destroy process",
							null, errorOutput, null);
					deploymentService.save(theDeployment);
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
					String errorOutput = ApplicationDeployerHelper.getOutputFromFile(logs, logger);
					logger.error(errorOutput);
					// kill the process if alive?
					p.destroy();
					// Set the right deployment status
					Deployment theDeployment = deploymentService.findByReference(reference);
					updateDeploymentStatus(deploymentIndexService, theDeployment,
							DeploymentStatusEnum.DESTROYING_FAILED,
							"Failed destroy process exit code", null, errorOutput, null);
					deploymentService.save(theDeployment);
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
					updateDeploymentStatus(deploymentIndexService, theDeployment,
							DeploymentStatusEnum.DESTROYED, null,
							null, null, null);
					//updateDeploymentOutputs(theDeployment.getReference(), cloudProviderPath, DeploymentStatusEnum.DESTROYING_FAILED, deploymentsRoot);
                    deploymentService.save(theDeployment);
				}
			}
		});
		newThread.start();

	}


}