package uk.ac.ebi.tsc.portal.api.configuration.controller;

import org.springframework.hateoas.ResourceSupport;

import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParameter;


/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class ConfigurationDeploymentParameterResource extends ResourceSupport {

    private String key;
    private String value;

    public ConfigurationDeploymentParameterResource() {
    }

    public ConfigurationDeploymentParameterResource(ConfigurationDeploymentParameter configurationDeploymentParameter) {
        this.key = configurationDeploymentParameter.getKey();
        this.value = configurationDeploymentParameter.getValue();
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
