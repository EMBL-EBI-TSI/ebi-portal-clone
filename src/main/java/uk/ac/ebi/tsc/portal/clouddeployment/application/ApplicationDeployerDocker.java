package uk.ac.ebi.tsc.portal.clouddeployment.application;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.application.repo.Application;
import uk.ac.ebi.tsc.portal.api.application.repo.ApplicationRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopy;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigDeploymentParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.configuration.repo.Configuration;
import uk.ac.ebi.tsc.portal.api.deployment.repo.*;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDeployerException;
import uk.ac.ebi.tsc.portal.clouddeployment.model.StateFromTerraformOutput;
import uk.ac.ebi.tsc.portal.clouddeployment.utils.SSHKeyGenerator;
import uk.ac.ebi.tsc.portal.usage.deployment.service.DeploymentIndexService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@Component
public class ApplicationDeployerDocker extends AbstractApplicationDeployer {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationDeployerDocker.class);

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

    @Value("${ecp.runner.docker.host}")
    private String dockerHost;

    @Value("${ecp.runner.docker.tls.verify}")
    private String dockerTlsVerify;

    @Value("${ecp.runner.docker.cert.path}")
    private String dockerCertPath;

    @Value("${ecp.runner.docker.registry.username}")
    private String dockerRegistryUsername;

    @Value("${ecp.runner.docker.registry.password}")
    private String dockerRegistryPassword;

    @Value("${ecp.runner.docker.registry.url}")
    private String dockerRegistryUrl;

    @Value("${ecp.runner.docker.registry.email}")
    private String dockerRegistryEmail;

    @Value("${ecp.runner.docker.container.origin}")
    private String dockerContainerOrigin;

    @Value("${ecp.runner.docker.container.name}")
    private String dockerContainerName;


    private static DockerClient dockerClient;

    @Autowired
    public ApplicationDeployerDocker(DeploymentRepository deploymentRepository,
                                   DeploymentStatusRepository deploymentStatusRepository,
                                   ApplicationRepository applicationRepository,
                                   DomainService domainService,
                                   CloudProviderParamsCopyRepository cloudProviderParametersRepository,
                                   ConfigDeploymentParamsCopyRepository configDeploymentParamsCopyRepository,
                                   EncryptionService encryptionService,
                                     StopMeSecretService secretService) {
        super(deploymentRepository,
                deploymentStatusRepository,
                applicationRepository,
                domainService,
                cloudProviderParametersRepository,
                configDeploymentParamsCopyRepository,
                encryptionService,
                secretService
        );

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
                       String userSshKey)  throws IOException,
            ApplicationDeployerException, NoSuchAlgorithmException, NoSuchProviderException {

        initEcpRunnerDocker();
        DeploymentIndexService deploymentIndexService = new DeploymentIndexService(
                new RestTemplate(),
                this.elasticSearchUrl + "/" + this.elasticSearchIndex,
                this.elasticSearchUsername,
                this.elasticSearchPassword);

        logger.info("Starting deployment of application using DOCKER, from repo: " + theApplication.repoPath);
        if (inputAssignments!= null) logger.info("  With " + inputAssignments.keySet().size() + " assigned inputs");
        if (parameterAssignments!= null) logger.info("  With " + parameterAssignments.keySet().size() + " assigned parameters");
        if (volumeAttachments!= null) logger.info("  With " + volumeAttachments.keySet().size() + " attached volumes");
        if (configurationParameters!= null) logger.info("  With " + configurationParameters.keySet().size() + " configuration parameters added ");

        // Setup deployment environment
        List<String> environment = new LinkedList<>();
        // set the deployment reference
        environment.add("PORTAL_DEPLOYMENT_REFERENCE=" + reference);
        // Set cloud credentials
        Map<String,String> credentials = new HashMap<>();
        ApplicationDeployerHelper.addGenericProviderCreds(credentials, cloudProviderParametersCopy, logger);
        credentials.entrySet().stream().forEach(entry -> {
            environment.add(entry.getKey() + "=" + entry.getValue());
        });
        // Generate and inject SSH key
        String fileDestination = deploymentsRoot + File.separator + reference + File.separator + reference;
        logger.info("Generated internal key file ", fileDestination);
        SSHKeyGenerator.generateKeys(userEmail, fileDestination);
        environment.add("PRIVATE_KEY" + "=" + fileDestination);
        environment.add("PUBLIC_KEY" + "=" + fileDestination + ".pub");
        environment.add("TF_VAR_private_key_path" + "=" + fileDestination);
        environment.add("TF_VAR_public_key_path" + "=" + fileDestination + ".pub");
        environment.add("portal_private_key_path" + "=" + fileDestination);
        environment.add("portal_public_key_path" + "=" + fileDestination + ".pub");
        environment.add("TF_VAR_portal_private_key_path" + "=" + fileDestination);
        environment.add("TF_VAR_portal_public_key_path" + "=" + fileDestination + ".pub");
        // Set deployment parameters
        parameterAssignments.entrySet().stream().forEach(entry -> {
            environment.add(entry.getKey() + "=" + entry.getValue());
        });
        // Set volume assignments
        volumeAttachments.entrySet().stream().forEach(entry -> {
            environment.add(entry.getKey() + "=" + entry.getValue());
        });
        // Set configuration parameters
        configurationParameters.entrySet().stream().forEach(entry -> {
            environment.add(entry.getKey() + "=" + entry.getValue());
        });
        // Set inputs
        inputAssignments.entrySet().stream().forEach(entry -> {
            environment.add(entry.getKey() + "=" + entry.getValue());
        });
        // Set user SSH-KEY
        if (userSshKey != null){
            logger.info("Adding user's own ssh_key of configuration");
            environment.add("TF_VAR_ssh_key" + "=" + userSshKey);
            environment.add("ssh_key" + "=" + userSshKey);
            environment.add("profile_public_key" + "=" + userSshKey);
            environment.add("TF_VAR_profile_public_key" + "=" + userSshKey);
        } else if (configuration != null) {
            logger.info("Adding ssh_key of configuration");
            environment.add("TF_VAR_ssh_key" + "=" + configuration.getSshKey());
            environment.add("ssh_key" + "=" + configuration.getSshKey());
            environment.add("profile_public_key" + "=" + configuration.getSshKey());
            environment.add("TF_VAR_profile_public_key" + "=" + configuration.getSshKey());
        }

        // Start docker container
        CreateContainerResponse container;

        try {
            container = createContainerForCommand("deploy", reference, environment);
        } catch (Exception e) {
            logger.error("Failed to create container container." + e.getMessage());
            logger.error("Trying to remove first...");
            dockerClient.removeContainerCmd(this.dockerContainerName).exec();
            container = createContainerForCommand("deploy", reference, environment);
        }


        if (container != null) {

            try {
                startContainer(container);

                Deployment theDeployment = deploymentService.findByReference(reference);
                updateDeploymentStatus(deploymentIndexService, theDeployment,
                        DeploymentStatusEnum.STARTING, "Interrupted deployment process",
                        null, null, startTime);
                deploymentService.save(theDeployment); // TODO: right place??

                // TODO: we need to do a lot of stuff on deployment results - where do we do it?
                // - Do we create a thread that start the container?
                // - Can we use the exec(callback) to detect completion and do stuff?
                // - Can we use something different from .awaitCompletion() (e.g. onCompletion or similar)
                //   and do stuff then?
                AttachContainerSimpleCallback callback = new AttachContainerSimpleCallback(reference, "deploy.log");
                dockerClient.attachContainerCmd(container.getId())
                        .withStdErr(true)
                        .withStdOut(true)
                        .withFollowStream(true)
                        .withLogs(true)
                        .withStdOut(true)
                        .exec(callback)
                        .awaitCompletion();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


        }

    }


    public StateFromTerraformOutput state(String repoPath,
                                          String reference,
                                          String cloudProviderPath,
                                          Map<String, String> outputs,
                                          Configuration configuration,
                                          String userSshKey) {

        throw new NotImplementedException();
    }


    public void destroy(String repoPath, String reference,
                        String cloudProviderPath,
                        Collection<DeploymentAssignedInput> inputAssignments,
                        Collection<DeploymentAssignedParameter> parameterAssignments,
                        Collection<DeploymentAttachedVolume> volumeAttachments,
                        DeploymentConfiguration deploymentConfiguration,
                        CloudProviderParamsCopy cloudProviderParametersCopy) {
        throw new NotImplementedException();
    }


    private void startContainer(CreateContainerResponse container) throws InterruptedException {
        dockerClient.startContainerCmd(container.getId()).exec();
    }


    private CreateContainerResponse createContainerForCommand(String command, String deploymentReference, List<String> environment) {

        String containerCommand;
        switch (command.toLowerCase()) {
            case "deploy":
                containerCommand = "application/ostack/deploy.sh";
                break;
            case "destroy":
                containerCommand = "application/ostack/destroy.sh";
                break;
            default:
                containerCommand = null;
        }

        if (containerCommand != null) {
            logger.info("Creating container for '" + containerCommand + "'");

            // Create bind folders (avoid Docker linux to use root permissions)
            String dataDirectoryPath = System.getProperty("user.dir") + File.separator + "ecp-agent" + File.separator + "data";
            createFolderIfNotExists(dataDirectoryPath);
            String deploymentDirectoryPath = System.getProperty("user.dir") + File.separator + "ecp-agent" + File.separator + deploymentReference;
            createFolderIfNotExists(deploymentDirectoryPath);
            String tmpDirectoryPath = System.getProperty("user.dir") + File.separator + "ecp-agent" + File.separator + "tmp";
            createFolderIfNotExists(tmpDirectoryPath);

            return dockerClient.createContainerCmd(dockerContainerOrigin)
                    .withName(dockerContainerName)
                    .withBinds(
                            new Bind(
                                    dataDirectoryPath,
                                    new Volume("/data")),
                            new Bind(
                                    deploymentDirectoryPath,
                                    new Volume(File.separator + deploymentReference)),
                            new Bind(
                                    tmpDirectoryPath,
                                    new Volume("/tmp"))
                    )
                    .withEnv(
                            environment
                    )
                    .withCmd(containerCommand)
                    .exec();
        } else {
            logger.error("Not creating container: command should be 'deploy' or 'destroy'");
            return null;
        }
    }


    private static void createFolderIfNotExists(String directoryPath) {
        Path path = Paths.get(directoryPath);
        //if directory exists?
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                //fail to create directory
                e.printStackTrace();
            }
        }
    }


    private void initEcpRunnerDocker() {
        // Configure docker client
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(this.dockerHost)
                .withDockerTlsVerify(this.dockerTlsVerify)
                .withDockerCertPath(this.dockerCertPath)
                .withRegistryUrl(this.dockerRegistryUrl)
                .withRegistryUsername(this.dockerRegistryUsername)
                .withRegistryPassword(this.dockerRegistryPassword)
                .withRegistryEmail(this.dockerRegistryEmail)
                .build();

        // Get client instance for later use
        dockerClient = DockerClientBuilder.getInstance(config)
                .build();

        // Pull image  and create repository
        try {
            dockerClient.pullImageCmd(this.dockerContainerOrigin)
                    //.withTag("latest")
                    .exec(new PullImageResultCallback()).awaitCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
