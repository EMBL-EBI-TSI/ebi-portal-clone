package uk.ac.ebi.tsc.portal.api.deployment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.tsc.portal.api.deployment.repo.Deployment;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentConfiguration;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentConfigurationRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentStatus;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentStatusRepository;

import java.util.Collection;
import java.util.List;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class DeploymentConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentConfigurationService.class);

    private final DeploymentConfigurationRepository deploymentConfigurationRepository;

    public DeploymentConfigurationService(DeploymentConfigurationRepository deploymentConfigurationRepository) {
    	this.deploymentConfigurationRepository = deploymentConfigurationRepository;
    }

    public DeploymentConfiguration findByDeployment(Deployment deployment) {
        return deploymentConfigurationRepository.findByDeployment(deployment)
        		.orElseThrow(() -> new DeploymentConfigurationNotFoundException(deployment.getReference()));
    }

    public DeploymentConfiguration findByName(String name) {
        return this.deploymentConfigurationRepository.findByName(name).orElseThrow(
                () -> new DeploymentConfigurationNotFoundException(name));
    }
    
    public List<DeploymentConfiguration> findAll(){
    	return this.deploymentConfigurationRepository.findAll();
    }
   
}
