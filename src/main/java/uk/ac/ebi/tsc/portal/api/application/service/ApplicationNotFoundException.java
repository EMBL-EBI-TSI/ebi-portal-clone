package uk.ac.ebi.tsc.portal.api.application.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(Long containerId) {
        super("Could not find application '" + containerId + "'.");
    }

    public ApplicationNotFoundException(String repoUri) {
        super("Could not find application with URI '" + repoUri + "'.");
    }
    
    public ApplicationNotFoundException(String username, String applicationName) {
        super("Could not find application '" + applicationName + "' for user \'" + username + "\'.");
    }

}