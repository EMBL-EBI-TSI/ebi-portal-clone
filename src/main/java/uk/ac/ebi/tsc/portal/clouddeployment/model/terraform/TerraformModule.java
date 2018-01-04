package uk.ac.ebi.tsc.portal.clouddeployment.model.terraform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class TerraformModule {

    public Map<String,TerraformResource> resources;
}
