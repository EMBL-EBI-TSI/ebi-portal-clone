package uk.ac.ebi.tsc.portal.api.configuration.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ExistingConfigurationNameException extends RuntimeException {

    public ExistingConfigurationNameException(String username, String name) {
        super("Could not create configuration '" + name + "' for user '" + username + "'. Name already exists.");
    }

}