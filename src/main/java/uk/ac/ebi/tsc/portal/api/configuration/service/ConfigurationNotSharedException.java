package uk.ac.ebi.tsc.portal.api.configuration.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ConfigurationNotSharedException extends RuntimeException {

	public ConfigurationNotSharedException(String givenName, String configurationName) {
        super("Configuration '" + configurationName + "' has not been shared with user \'" + givenName + "\'.");
    }

}