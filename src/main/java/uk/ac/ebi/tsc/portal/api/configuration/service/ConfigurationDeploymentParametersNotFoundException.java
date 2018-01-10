package uk.ac.ebi.tsc.portal.api.configuration.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ConfigurationDeploymentParametersNotFoundException extends RuntimeException {

    public ConfigurationDeploymentParametersNotFoundException(String name) {
        super("Could not find configuration deployment parameters " + name + " for the user ");
    }
    
    public ConfigurationDeploymentParametersNotFoundException(Long id) {
        super("Could not find configuration deployment parameters with id " + id + "for the user ");
    }

}
