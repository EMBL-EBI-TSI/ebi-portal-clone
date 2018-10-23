package uk.ac.ebi.tsc.portal.api.application.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ApplicationNotSharedException extends RuntimeException {

	public ApplicationNotSharedException(String givenName, String applicationName) {
        super("Application '" + applicationName + "' has not been shared with user \'" + givenName + "\'.");
    }

}