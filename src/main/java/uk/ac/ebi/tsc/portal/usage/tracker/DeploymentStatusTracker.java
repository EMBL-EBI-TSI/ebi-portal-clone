package uk.ac.ebi.tsc.portal.usage.tracker;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParametersRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParametersService;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParamsCopyService;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParametersRepository;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationRepository;
import uk.ac.ebi.tsc.portal.api.configuration.service.ConfigurationDeploymentParametersService;
import uk.ac.ebi.tsc.portal.api.configuration.service.ConfigurationService;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentConfigurationRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentStatusRepository;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentConfigurationService;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentService;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;
import uk.ac.ebi.tsc.portal.clouddeployment.application.ApplicationDeployerBash;
import uk.ac.ebi.tsc.portal.usage.deployment.service.DeploymentIndexService;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Component
public class DeploymentStatusTracker {

	private static final Logger logger = LoggerFactory.getLogger(DeploymentStatusTracker.class);

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

	private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

	private DeploymentService deploymentService;
	private ConfigurationService configurationService;
	private DeploymentConfigurationService deploymentConfigurationService;
	private CloudProviderParametersService cloudProviderParametersService;
	private CloudProviderParamsCopyService cloudProviderParamsCopyService;
	private ConfigurationDeploymentParametersService configurationDeploymentParametersService;
	private ApplicationDeployerBash applicationDeployerBash;

	private final CloudProviderParamsCopyRepository cloudProviderParametersCopyRepository;

	private final EncryptionService encryptionService;

	private final String salt, password;

	@Autowired
	public DeploymentStatusTracker(DeploymentRepository deploymentRepository,
			DeploymentStatusRepository deploymentStatusRepository,
			CloudProviderParamsCopyRepository cloudProviderParametersCopyRepository,
			ConfigurationRepository configurationRepository,
			DomainService domainService,
			CloudProviderParametersRepository cloudProviderParametersRepository,
			ConfigurationDeploymentParametersRepository configurationDeploymentParametersRepository,
			DeploymentConfigurationRepository deploymentConfigurationRepository,
			ApplicationDeployerBash applicationDeployerBash,
			EncryptionService encryptionService,
			@Value("${ecp.security.salt}") final String salt, 
			@Value("${ecp.security.password}") final String password) {
		this.salt = salt;
		this.password = password;
		this.cloudProviderParametersCopyRepository = cloudProviderParametersCopyRepository;
		this.deploymentService = new DeploymentService(deploymentRepository, deploymentStatusRepository);
		this.cloudProviderParamsCopyService = new CloudProviderParamsCopyService(cloudProviderParametersCopyRepository, encryptionService,
				salt, password);
		this.cloudProviderParametersService = new CloudProviderParametersService(cloudProviderParametersRepository, domainService, cloudProviderParamsCopyService, encryptionService,
				salt, password);
		this.configurationDeploymentParametersService = new ConfigurationDeploymentParametersService(configurationDeploymentParametersRepository, domainService);
		this.configurationService = new ConfigurationService(
				configurationRepository,
				domainService,
				cloudProviderParametersService,
				configurationDeploymentParametersService,
				cloudProviderParamsCopyService,
				deploymentService);
		this.deploymentConfigurationService = new DeploymentConfigurationService(deploymentConfigurationRepository);
		this.applicationDeployerBash = applicationDeployerBash;
		scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(4);
		this.deploymentService = new DeploymentService(deploymentRepository, deploymentStatusRepository);
		scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);
		this.encryptionService = encryptionService;
	}

	public void start(long initialDelay, long periodInSeconds) {
		logger.info("Starting Deployment Status Tracker... (1/4)");
		DeploymentIndexService deploymentIndexService = new DeploymentIndexService(
				new RestTemplate(),
				this.elasticSearchUrl + "/" + this.elasticSearchIndex,
				this.elasticSearchUsername,
				this.elasticSearchPassword);
		logger.info("DeploymentIndexService initiated... (2/4)");
		scheduledThreadPoolExecutor.scheduleAtFixedRate(
				new DeploymentStatusUpdate(deploymentIndexService, deploymentService, 
						cloudProviderParametersCopyRepository, encryptionService, salt, password),
				initialDelay,
				periodInSeconds,
				TimeUnit.SECONDS
				);
		logger.info("DeploymentStatusUpdate thread scheduled... (3/4)");
		scheduledThreadPoolExecutor.scheduleAtFixedRate(
				new ConfigurationUsageMonitor(
						deploymentIndexService,
						deploymentService,
						configurationService,
						cloudProviderParamsCopyService,
						deploymentConfigurationService,
						applicationDeployerBash),
				initialDelay,
				periodInSeconds,
				TimeUnit.SECONDS
				);
		logger.info("ConfigurationUsageMonitor thread scheduled... (4/4)");
	}
}
