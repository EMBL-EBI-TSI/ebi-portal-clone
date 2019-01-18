package uk.ac.ebi.tsc.portal.clouddeployment.model;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Component
public class ImageSource {

    @JsonProperty
    public String url;

    @JsonProperty
    public String name;

}
