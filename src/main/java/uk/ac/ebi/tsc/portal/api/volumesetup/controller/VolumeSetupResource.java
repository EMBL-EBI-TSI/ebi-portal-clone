package uk.ac.ebi.tsc.portal.api.volumesetup.controller;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetup;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
class VolumeSetupResource extends ResourceSupport {

    private Long id;
    private String repoUri;
    private String name;
    private Collection<String> cloudProviders;

    public VolumeSetupResource() {
    }

    public VolumeSetupResource(VolumeSetup volumeSetup) {

        this.id = volumeSetup.getId();
        this.repoUri = volumeSetup.getRepoUri();
        this.name = volumeSetup.getName();
        this.cloudProviders = new LinkedList<>();
        this.cloudProviders.addAll(
                volumeSetup.getCloudProviders().stream().map(provider -> provider.name).collect(Collectors.toList())
        );

        this.add(
                linkTo(
                        VolumeSetupRestController.class
                ).withRel("volumeSetups")
        );
        this.add(
                linkTo(VolumeSetupRestController.class)
                        .slash(volumeSetup.getAccount().getUsername())
                        .withRel("account")
        );
        this.add(
                linkTo(
                        VolumeSetupRestController.class
                ).withRel("applications")
        );
        this.add(
                linkTo(VolumeSetupRestController.class)
                        .slash(volumeSetup.getName()
                        ).withSelfRel()
        );
        this.add(new Link(volumeSetup.getRepoUri(), "volumeSetup-repo-uri"));

    }

    public String getRepoUri() {
        return this.repoUri;
    }

    public String getName() {
        return this.name;
    }

    public Collection<String> getCloudProviders() {
        return cloudProviders;
    }
}
