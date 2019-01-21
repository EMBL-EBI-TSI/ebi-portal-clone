package uk.ac.ebi.tsc.portal.api.deployment.bean;

import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentGeneratedOutput;
import uk.ac.ebi.tsc.portal.api.error.ErrorMessage;
import java.util.List;
import java.util.Optional;

public class DeploymentOutputsProcessResult {

    Optional<ErrorMessage> errorMessage;

    List<DeploymentGeneratedOutput> deploymentGeneratedOutputList;

    public Optional<ErrorMessage> getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(Optional<ErrorMessage> errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<DeploymentGeneratedOutput> getDeploymentGeneratedOutputList() {
        return deploymentGeneratedOutputList;
    }

    public void setDeploymentGeneratedOutputList(List<DeploymentGeneratedOutput> deploymentGeneratedOutputList) {
        this.deploymentGeneratedOutputList = deploymentGeneratedOutputList;
    }
}
