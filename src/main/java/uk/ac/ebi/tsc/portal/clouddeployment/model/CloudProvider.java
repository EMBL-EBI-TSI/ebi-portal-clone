package uk.ac.ebi.tsc.portal.clouddeployment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudProvider {

    @JsonProperty
    public String cloudProvider;

    @JsonProperty
    public String path;

    @JsonProperty
    public Collection<String> volumes;

    @JsonProperty
    public Collection<String> inputs;

    @JsonProperty
    public Collection<String> outputs;

}
