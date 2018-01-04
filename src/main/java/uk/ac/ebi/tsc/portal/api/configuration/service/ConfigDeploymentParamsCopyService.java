package uk.ac.ebi.tsc.portal.api.configuration.service;


import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigDeploymentParamsCopy;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigDeploymentParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.configuration.repo.Configuration;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParameters;
import uk.ac.ebi.tsc.portal.api.deployment.repo.Deployment;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentService;

public class ConfigDeploymentParamsCopyService {

	private static final Logger logger = LoggerFactory.getLogger(ConfigDeploymentParamsCopyService.class);

	private final ConfigDeploymentParamsCopyRepository configDeploymentParamsCopyRepository;

	@Autowired
	public ConfigDeploymentParamsCopyService(ConfigDeploymentParamsCopyRepository configDeploymentParamsCopyRepository) {
		this.configDeploymentParamsCopyRepository = configDeploymentParamsCopyRepository;
	}

	public ConfigDeploymentParamsCopy findByConfigurationDeploymentParametersReference(String reference) {
		return this.configDeploymentParamsCopyRepository.findByConfigurationDeploymentParametersReference(reference).orElseThrow(
				() -> new ConfigDeploymentParamsCopyNotFoundException(reference));
	}
	
	public ConfigDeploymentParamsCopy findByName(String name) {
		return this.configDeploymentParamsCopyRepository.findByName(name).stream().findFirst().orElseThrow(
				() -> new ConfigDeploymentParamsCopyNotFoundException(name));
	}
	
	public ConfigDeploymentParamsCopy findByNameAndAccountUsername(String name, String username){
		return this.configDeploymentParamsCopyRepository.findByNameAndAccountUsername(name, username)
				.stream().findFirst().orElseThrow(
				() -> new ConfigDeploymentParamsCopyNotFoundException(name));
	}
	
	public ConfigDeploymentParamsCopy save(ConfigDeploymentParamsCopy configDeploymentParamsCopy) {
		return this.configDeploymentParamsCopyRepository.save(configDeploymentParamsCopy);
	}

	public void checkAndDeleteCDPCopy(ConfigDeploymentParamsCopy cdpCopy, DeploymentService deploymentService,
			ConfigurationService configurationService) {
		
		logger.info("Checking if any deployments refer to the config deployment params copy");
		
		Deployment deploymentFound = deploymentService.findAll().stream().
				 filter(deployment -> deployment.getDeploymentConfiguration() != null )
				.filter(deployment -> deployment.getDeploymentConfiguration().getConfigDeploymentParametersReference() != null)
				.filter(deployment -> deployment.getDeploymentConfiguration().getConfigDeploymentParametersReference()
				.equals(cdpCopy.getConfigurationDeploymentParametersReference()))
				.findAny().orElse(null);

		logger.info("Checking if any configurations refer to the cloud credential copy");
		Configuration configurationFound = configurationService.findAll().stream().
				filter(configuration -> 
				configuration.getConfigDeployParamsReference().equals(cdpCopy.getConfigurationDeploymentParametersReference())
						)
				.findAny().orElse(null);

		if(deploymentFound == null && configurationFound == null){
			logger.info("CDP copy is not referenced by any deployment or configuration, so deleting it");
			this.configDeploymentParamsCopyRepository.delete(cdpCopy);
		}
		
	}

	public List<ConfigDeploymentParamsCopy> findAll() {
		return this.configDeploymentParamsCopyRepository.findAll();
	}
}
