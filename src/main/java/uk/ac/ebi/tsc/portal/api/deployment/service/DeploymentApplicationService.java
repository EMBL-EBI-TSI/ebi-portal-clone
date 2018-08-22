package uk.ac.ebi.tsc.portal.api.deployment.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.tsc.portal.api.application.repo.Application;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentApplication;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentApplicationCloudProvider;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentApplicationCloudProviderInput;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentApplicationCloudProviderOutput;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentApplicationCloudProviderVolume;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentApplicationRepository;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class DeploymentApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentApplicationService.class);

    private final DeploymentApplicationRepository deploymentApplicationRepository;
    
    public DeploymentApplicationService(DeploymentApplicationRepository deploymentApplicationRepository) {
        this.deploymentApplicationRepository = deploymentApplicationRepository;
    }


    public DeploymentApplication save(DeploymentApplication deploymentApplication) {
        return this.deploymentApplicationRepository.save(deploymentApplication);
    }
    
    public List<DeploymentApplication> findByAccountIdAndRepoPath(Long accountId, String repoPath) {
        return this.deploymentApplicationRepository.findByAccountIdAndRepoPath(accountId, repoPath);
    }


	public DeploymentApplication createDeploymentApplication(Application theApplication) {
		logger.info("Setting the cloud provider inputs,outputs and volume of the application to deploymentApplication");
		DeploymentApplication deploymentApplication = new DeploymentApplication(theApplication);
		deploymentApplication.getCloudProviders().addAll(
				theApplication.getCloudProviders().stream().map(
						provider -> {
							DeploymentApplicationCloudProvider newProvider = new DeploymentApplicationCloudProvider(
									provider.name,
									provider.path,
									deploymentApplication);
							if (provider.inputs!=null) {
								newProvider.getInputs().addAll(
										provider.inputs.stream().map(
												input -> new DeploymentApplicationCloudProviderInput(input.name, newProvider)
												).collect(Collectors.toList())
										);
							}
							if (provider.outputs != null) {
								newProvider.getOutputs().addAll(
										provider.outputs.stream().map(
												output -> new DeploymentApplicationCloudProviderOutput(output.name, newProvider)
												).collect(Collectors.toList())
										);
							}
							if (provider.volumes!=null) {
								newProvider.getVolumes().addAll(
										provider.volumes.stream().map(
												volume -> new DeploymentApplicationCloudProviderVolume(volume.name,newProvider)
												).collect(Collectors.toList())
										);
							}
							return newProvider;
						}
						).collect(Collectors.toList())
				);
		return deploymentApplication;
	}

}
