package uk.ac.ebi.tsc.portal.usage.deployment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 **/

@JsonIgnoreProperties(ignoreUnknown = true)
public class Query {

    @JsonProperty
    public String match_all;

}
