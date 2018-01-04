package uk.ac.ebi.tsc.portal.api.configuration.service;


import java.util.List;

import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigDeploymentParamCopy;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigDeploymentParamCopyRepository;

public class ConfigDeploymentParamCopyService {

	private final ConfigDeploymentParamCopyRepository configDeploymentParamCopyRepository;

	public ConfigDeploymentParamCopyService(ConfigDeploymentParamCopyRepository configDeploymentParamCopyRepository) {
	        this.configDeploymentParamCopyRepository = configDeploymentParamCopyRepository;
	}

	public ConfigDeploymentParamCopy save(ConfigDeploymentParamCopy configDeploymentParamCopy) {
		return this.configDeploymentParamCopyRepository.save(configDeploymentParamCopy);
	}
	
	public List<ConfigDeploymentParamCopy> findAll() {
		return this.configDeploymentParamCopyRepository.findAll();
	}
}
