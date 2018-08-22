package uk.ac.ebi.tsc.portal.api.configuration.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvalidConfigurationInputException extends RuntimeException {

    public InvalidConfigurationInputException(String username, String name) {
        super("Could not create configuration '" + name + "' for user '" + username + "'. Invalid input.");
    }
    
    public InvalidConfigurationInputException(String name) {
        super("Could not create configuration '" + name + "Invalid input."
        		+ " Names should start only with an alphabet or number. "
        		+ " Names may end with or contain '.' ,'_', '-' or spaces inbetween them." 
        	);
    }
    
    public InvalidConfigurationInputException(){
    	super("Configuration and its owner's name should be specified");
    }

}