package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvalidCloudProviderParametersInputException extends RuntimeException {

    public InvalidCloudProviderParametersInputException(String username, String name) {
        super("Could not create cloud provider parameters '" + name + "' for user '" + username + "'. Invalid input.");
    }
    
    public InvalidCloudProviderParametersInputException(String name) {
        super("Could not create cloud provider parameters '" + name + "Invalid input."
        		+ " Names should start only with an alphabet or number. "
        		+ " Names may end with or contain '.' ,'_', '-' or spaces inbetween them." 
        	);
    }

}