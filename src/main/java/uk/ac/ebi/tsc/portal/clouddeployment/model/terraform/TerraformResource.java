package uk.ac.ebi.tsc.portal.clouddeployment.model.terraform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TerraformResource {

    public String type;

    public TerraformResourcePrimary primary;

}
