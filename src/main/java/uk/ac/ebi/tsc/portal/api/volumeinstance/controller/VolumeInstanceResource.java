package uk.ac.ebi.tsc.portal.api.volumeinstance.controller;

import org.springframework.hateoas.ResourceSupport;
import uk.ac.ebi.tsc.portal.api.account.controller.AccountRestController;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller.CloudProviderParametersResource;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstance;
import uk.ac.ebi.tsc.portal.api.volumesetup.controller.VolumeSetupRestController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
class VolumeInstanceResource extends ResourceSupport {

    private Long id;
    public String reference;
    public String setupName;
    public String providerId;
    public CloudProviderParametersResource cloudProviderParameters;

    public VolumeInstanceResource() {
    }

    VolumeInstanceResource(VolumeInstance volumeInstance) {

        this.id = volumeInstance.getId();
        this.reference = volumeInstance.getReference();
        this.providerId = volumeInstance.getProviderId();
        this.setupName = volumeInstance.getVolumeSetup().getName();
        this.cloudProviderParameters = new CloudProviderParametersResource(volumeInstance.getCloudProviderParameters());

        this.add(
                linkTo(
                        methodOn(
                                AccountRestController.class
                        ).getAccountByUsername(
                                volumeInstance.getAccount().getUsername()
                        )
                ).withRel("account")
        );
        this.add(
                linkTo(VolumeSetupRestController.class)
                        .slash(volumeInstance.getVolumeSetup().getName()
                        ).withRel("volumeSetup")
        );
        this.add(
                linkTo(
                        methodOn(
                                VolumeInstanceRestController.class
                        ).getVolumeInstanceStatusByReference(
                                null,
                                volumeInstance.getReference()
                        )
                ).withRel("status")
        );
        this.add(
                linkTo(
                        VolumeInstanceRestController.class
                ).withRel("volumeInstances")
        );
        this.add(
                linkTo(
                        methodOn(
                                VolumeInstanceRestController.class
                        ).getVolumeInstanceByReference(
                                null,
                                volumeInstance.getReference()
                        )
                ).withSelfRel()
        );


    }

    public String getReference() {
        return reference;
    }

    public String getSetupName() {
        return setupName;
    }

    public String getProviderId() {
        return providerId;
    }

    public CloudProviderParametersResource getCloudProviderParameters() {
        return cloudProviderParameters;
    }

}
