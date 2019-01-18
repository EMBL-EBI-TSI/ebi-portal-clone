package uk.ac.ebi.tsc.portal.clouddeployment.model.terraform;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Component
public class TerraformModule {

    public Map<String,TerraformResource> resources;
}
