package uk.ac.ebi.tsc.portal.api.configuration.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvalidConfigurationDeploymentParametersException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public InvalidConfigurationDeploymentParametersException(String username, String name) {
        super("Could not create deploymentParameters  '" + name + "' for user '" + username + "'. Invalid name.");
    }
    
    public InvalidConfigurationDeploymentParametersException(String name) {
        super("Could not create deploymentParameters '" + name + "Invalid input."
        		+ " Names should start only with an alphabet or number. "
        		+ " Names may end with or contain '.' ,'_', '-' or spaces inbetween them." 
        	);
    }

}