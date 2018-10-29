package uk.ac.ebi.tsc.portal.api.deployment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.tsc.portal.api.deployment.repo.Deployment;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentGeneratedOutput;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentGeneratedOutputRepository;
import javax.transaction.Transactional;
import java.util.Map;
import java.util.Optional;

@Component
public class DeploymentGeneratedOutputService {

    @Autowired
    DeploymentGeneratedOutputRepository deploymentGeneratedOutputRepository;

    public void saveDeploymentGeneratedOutput(DeploymentGeneratedOutput deploymentGeneratedOutput){
        deploymentGeneratedOutputRepository.save(deploymentGeneratedOutput);
    }

    public Optional<DeploymentGeneratedOutput> getDeploymentGeneratedOutput(String reference,String outputName){
        return deploymentGeneratedOutputRepository.findByDeploymentReferenceAndOutputName(reference,outputName);
    }

    @Transactional
    public void saveOrUpdateDeploymentOutputs(Map<String, String> outputMap, Deployment theDeployment, String deploymentReference) {
        for (Map.Entry<String, String> entry : outputMap.entrySet()) {
            Optional<DeploymentGeneratedOutput> deploymentGeneratedOutputOpt = getDeploymentGeneratedOutput(deploymentReference, entry.getKey());
            if (deploymentGeneratedOutputOpt.isPresent()) {
                DeploymentGeneratedOutput deploymentGeneratedOutput = deploymentGeneratedOutputOpt.get();
                deploymentGeneratedOutput.setValue(entry.getValue());
                saveDeploymentGeneratedOutput(deploymentGeneratedOutput);
            } else {
                DeploymentGeneratedOutput deploymentGeneratedOutput = new DeploymentGeneratedOutput(entry.getKey(), entry.getValue(), theDeployment);
                saveDeploymentGeneratedOutput(deploymentGeneratedOutput);
            }
        }
    }

}
