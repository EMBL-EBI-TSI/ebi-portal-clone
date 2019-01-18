package uk.ac.ebi.tsc.portal.clouddeployment.model.terraform;

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
public class TerraformState {

    public String version;

    @JsonProperty("terraform_version")
    public String terraformVersion;

    public String serial;

    public String lineage;

    public TerraformModule[] modules;

}
