package uk.ac.ebi.tsc.portal.api.deployment.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.stereotype.Component;

import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentAssignedParameter;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class DeploymentAssignedParameterResource extends ResourceSupport {

    private String parameterName;
    private String parameterValue;

    public DeploymentAssignedParameterResource() {
    }

    public DeploymentAssignedParameterResource(DeploymentAssignedParameter deploymentAssignedParameter) {

        this.parameterName = deploymentAssignedParameter.getParameterName();
        this.parameterValue = deploymentAssignedParameter.getParameterValue();

        this.add(
            linkTo(DeploymentRestController.class)
                .slash(deploymentAssignedParameter.getDeployment().getReference()
            ).withRel("deployment")
        );

    }

    public String getParameterName() {
        return parameterName;
    }

    public String getParameterValue() {
        return parameterValue;
    }
}
