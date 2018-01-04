package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ExistingCloudProviderParametersNameException extends RuntimeException {

    public ExistingCloudProviderParametersNameException(String username, String name) {
        super("Could not create cloud provider parameters '" + name + "' for user '" + username + "'. Name already exists.");
    }

}