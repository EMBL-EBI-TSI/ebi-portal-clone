package uk.ac.ebi.tsc.portal.api.deployment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.ac.ebi.tsc.portal.api.deployment.bean.DeploymentOutputsProcessResult;
import uk.ac.ebi.tsc.portal.api.deployment.controller.DeploymentGeneratedOutputResource;
import uk.ac.ebi.tsc.portal.api.deployment.repo.Deployment;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentGeneratedOutput;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentRepository;
import uk.ac.ebi.tsc.portal.api.error.ErrorMessage;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Felix Xavier <famaladoss@ebi.ac.uk>
 */
@Service
public class DeploymentGeneratedOutputService {
	
    private final DeploymentRepository deploymentRepository;
    List<DeploymentGeneratedOutput> deploymentGeneratedOutputList;
    List<String> payLoadKeyList;
    Deployment theDeployment;
    StringBuilder replacingOutputValues;
    StringBuilder payLoadOutputValues;

    @Autowired
    public DeploymentGeneratedOutputService(DeploymentRepository deploymentRepository) {
        this.deploymentRepository = deploymentRepository;
    }

    public Boolean findModifyDeploymentGeneratedOutput(String outputName, String generatedValue, StringBuilder replacingOutputValues, List<DeploymentGeneratedOutput> deploymentGeneratedOutputList) {
        for (int i = 0; i < deploymentGeneratedOutputList.size(); i++) {
            DeploymentGeneratedOutput deploymentGeneratedOutput = deploymentGeneratedOutputList.get(i);
            if (deploymentGeneratedOutput.getOutputName().equals(outputName)) {
                replacingOutputValues.append(deploymentGeneratedOutput.getValue());
                deploymentGeneratedOutput.setValue(generatedValue);
                deploymentGeneratedOutputList.set(i, deploymentGeneratedOutput);
                return true;
            }
        }
        return false;
    }

    private Optional<ErrorMessage> validateDeploymentGeneratedOutputs(String reference, String secret, Optional<Deployment> optDeployment, List<DeploymentGeneratedOutputResource> payLoadGeneratedOutputList) {

        if (secret == null || secret.isEmpty()) {
            return Optional.of(new ErrorMessage("Missing header : secret", HttpStatus.BAD_REQUEST));
        }
        if (!optDeployment.isPresent() || !optDeployment.get().getDeploymentSecret().getSecret().equals(secret)) {
            throw new DeploymentNotFoundException(reference + " and secret : " + secret);
        }
        List<String> payLoadKeyList = payLoadGeneratedOutputList.stream().map(o -> o.getOutputName()).collect(Collectors.toList());
        if (payLoadKeyList.size() > new HashSet<>(payLoadKeyList).size())
            return Optional.of(new ErrorMessage("outputName should be unique for given List", HttpStatus.CONFLICT));
        return Optional.empty();
    }


    public DeploymentOutputsProcessResult saveOrUpdateDeploymentOutputs(String reference, String secret, List<DeploymentGeneratedOutputResource> payLoadGeneratedOutputList) {
        StringBuilder replacingOutputValues = new StringBuilder();
        StringBuilder payLoadOutputValues = new StringBuilder();
        Optional<Deployment> optDeployment = deploymentRepository.findByReference(reference);
        DeploymentOutputsProcessResult deploymentOutputsProcessResult = new DeploymentOutputsProcessResult();
        Optional<ErrorMessage> errorMessage = validateDeploymentGeneratedOutputs(reference, secret, optDeployment, payLoadGeneratedOutputList);
        if (errorMessage.isPresent())
            deploymentOutputsProcessResult.setErrorMessage(errorMessage);
        else {
            Deployment theDeployment = optDeployment.get();
            List<DeploymentGeneratedOutput> deploymentGeneratedOutputList = new ArrayList<>(theDeployment.getGeneratedOutputs());
            Map<String, String> outputMap = payLoadGeneratedOutputList.stream().collect(
                    Collectors.toMap(DeploymentGeneratedOutputResource::getOutputName, DeploymentGeneratedOutputResource::getGeneratedValue, (e1, e2) -> e1, LinkedHashMap::new));

            for (Map.Entry<String, String> entry : outputMap.entrySet()) {
                Boolean modify = findModifyDeploymentGeneratedOutput(entry.getKey(), entry.getValue(), replacingOutputValues, deploymentGeneratedOutputList);
                if (!modify) {
                    DeploymentGeneratedOutput deploymentGeneratedOutput = new DeploymentGeneratedOutput(entry.getKey(), entry.getValue(), theDeployment);
                    deploymentGeneratedOutputList.add(deploymentGeneratedOutput);
                }
                payLoadOutputValues.append(entry.getValue());
            }
            String existingOutputValues = theDeployment.getGeneratedOutputs().stream().map(o -> o.getValue()).reduce("", String::concat);
            if (existingOutputValues.length() - replacingOutputValues.length() + payLoadOutputValues.length() > 1000000)
                deploymentOutputsProcessResult.setErrorMessage(Optional.of(new ErrorMessage("Key/Value pair should not exceed 1MB for a deployment", HttpStatus.BAD_REQUEST)));
            else {
                theDeployment.setGeneratedOutputs(deploymentGeneratedOutputList);
                deploymentRepository.save(theDeployment);
                deploymentOutputsProcessResult.setDeploymentGeneratedOutputList(deploymentGeneratedOutputList);
                deploymentOutputsProcessResult.setErrorMessage(Optional.empty());
            }
        }
        return deploymentOutputsProcessResult;
    }

}
