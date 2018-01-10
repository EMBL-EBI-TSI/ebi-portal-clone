package uk.ac.ebi.tsc.portal.api.volumeinstance.controller;

import org.springframework.hateoas.ResourceSupport;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceStatus;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceStatusEnum;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
class VolumeInstanceStatusResource extends ResourceSupport {

    private VolumeInstanceStatusEnum status;

    public VolumeInstanceStatusResource() {
    }

    public VolumeInstanceStatusResource(VolumeInstanceStatus volumeInstanceStatus) {

        this.status = volumeInstanceStatus.getStatus();

        this.add(
                linkTo(
                        methodOn(
                                VolumeInstanceRestController.class
                        ).getVolumeInstanceByReference(
                                null,
                                volumeInstanceStatus.volumeInstance.getReference()
                        )
                ).withRel("deployment")
        );

    }

    public VolumeInstanceStatusEnum getStatus() {
        return status;
    }

}
