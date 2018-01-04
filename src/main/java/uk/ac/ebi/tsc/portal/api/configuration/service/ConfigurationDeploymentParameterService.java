package uk.ac.ebi.tsc.portal.api.configuration.service;


import java.util.Collection;

import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParameter;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParameterRepository;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParameters;

public class ConfigurationDeploymentParameterService {

	private final ConfigurationDeploymentParameterRepository configurationDeploymentParameterRepository;

	public ConfigurationDeploymentParameterService(ConfigurationDeploymentParameterRepository configurationDeploymentParameterRepository) {
	        this.configurationDeploymentParameterRepository = configurationDeploymentParameterRepository;
	}

	public ConfigurationDeploymentParameter save(ConfigurationDeploymentParameter deploymentParameter) {
		return this.configurationDeploymentParameterRepository.save(deploymentParameter);
	}
	
	public Collection<ConfigurationDeploymentParameter> findAll() {
		return this.configurationDeploymentParameterRepository.findAll();
	}
}
