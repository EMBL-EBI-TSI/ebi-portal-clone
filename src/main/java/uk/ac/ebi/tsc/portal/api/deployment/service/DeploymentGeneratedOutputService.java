package uk.ac.ebi.tsc.portal.api.deployment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentGeneratedOutput;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentGeneratedOutputRepository;
import java.util.Optional;

@Component
public class DeploymentGeneratedOutputService {

    @Autowired
    DeploymentGeneratedOutputRepository deploymentGeneratedOutputRepository;

    public void saveDeploymentGeneratedOutput(DeploymentGeneratedOutput deploymentGeneratedOutput){
        deploymentGeneratedOutputRepository.save(deploymentGeneratedOutput);
    }

    public boolean isExistDeploymentGeneratedOutput(String reference,String outputName){
        Optional<DeploymentGeneratedOutput> depGenOutput = deploymentGeneratedOutputRepository.findByDeploymentReferenceAndOutputName(reference,outputName);
        return depGenOutput.isPresent();
    }
}
