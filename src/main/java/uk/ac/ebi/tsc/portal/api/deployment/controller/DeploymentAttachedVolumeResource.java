package uk.ac.ebi.tsc.portal.api.deployment.controller;

import org.springframework.hateoas.ResourceSupport;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentAttachedVolume;
import uk.ac.ebi.tsc.portal.api.volumeinstance.controller.VolumeInstanceRestController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
class DeploymentAttachedVolumeResource extends ResourceSupport {

    private String name;
    private String volumeInstanceReference;

    public DeploymentAttachedVolumeResource() {
    }

    public DeploymentAttachedVolumeResource(DeploymentAttachedVolume deploymentAttachedVolume) {

        this.name = deploymentAttachedVolume.getName();
        this.volumeInstanceReference = deploymentAttachedVolume.getVolumeInstanceReference();

        this.add(
            linkTo(DeploymentRestController.class)
                .slash(deploymentAttachedVolume.getDeployment().getReference()
            ).withRel("deployment")
        );
        this.add(
            linkTo(
                methodOn(
                    VolumeInstanceRestController.class
                ).getVolumeInstanceByReference(
                    null,
                    deploymentAttachedVolume.getVolumeInstanceReference()
                )
            ).withRel("volume-instance")
        );

    }

    public String getName() {
        return name;
    }

    public String getVolumeInstanceReference() {
        return volumeInstanceReference;
    }
}
