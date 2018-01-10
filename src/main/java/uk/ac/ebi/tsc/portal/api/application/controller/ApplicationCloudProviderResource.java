package uk.ac.ebi.tsc.portal.api.application.controller;

import org.springframework.hateoas.ResourceSupport;
import uk.ac.ebi.tsc.portal.api.application.repo.ApplicationCloudProvider;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
class ApplicationCloudProviderResource extends ResourceSupport {

    private String cloudProvider;
    private Collection<String> inputs;
    private Collection<String> outputs;
    private Collection<String> volumes;

    public ApplicationCloudProviderResource() {
    }

    public ApplicationCloudProviderResource(ApplicationCloudProvider applicationCloudProvider) {

        this.cloudProvider = applicationCloudProvider.getName();
        this.inputs = new LinkedList<>();
        this.inputs.addAll(
                applicationCloudProvider.getInputs().stream().map(input -> input.name).collect(Collectors.toList())
        );
        this.outputs = new LinkedList<>();
        this.outputs.addAll(
                applicationCloudProvider.getOutputs().stream().map(output -> output.name).collect(Collectors.toList())
        );
        this.volumes = new LinkedList<>();
        this.volumes.addAll(
                applicationCloudProvider.getVolumes().stream().map(volume -> volume.name).collect(Collectors.toList())
        );


    }

    public String getCloudProvider() {
        return cloudProvider;
    }

    public Collection<String> getInputs() {
        return inputs;
    }

    public Collection<String> getOutputs() {
        return outputs;
    }

    public Collection<String> getVolumes() {
        return volumes;
    }
}
