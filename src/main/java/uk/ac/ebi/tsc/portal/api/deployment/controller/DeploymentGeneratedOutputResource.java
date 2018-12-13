package uk.ac.ebi.tsc.portal.api.deployment.controller;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.stereotype.Component;

import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentGeneratedOutput;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Component
public class DeploymentGeneratedOutputResource extends ResourceSupport {

    private String outputName;
    private String generatedValue;

    public DeploymentGeneratedOutputResource() {
    }

    public DeploymentGeneratedOutputResource(DeploymentGeneratedOutput deploymentGeneratedOutput) {

        this.outputName = deploymentGeneratedOutput.getOutputName();
        this.generatedValue = deploymentGeneratedOutput.getValue();

        this.add(
            linkTo(DeploymentRestController.class)
                .slash(deploymentGeneratedOutput.getDeployment().getReference())
            .withRel("deployment")
        );

    }

    public String getOutputName() {
        return outputName;
    }

    public String getGeneratedValue() {
        return generatedValue;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    public void setGeneratedValue(String generatedValue) {
        this.generatedValue = generatedValue;
    }
}
