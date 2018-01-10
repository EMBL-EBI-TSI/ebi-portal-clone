package uk.ac.ebi.tsc.portal.api.deployment.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotDeploymentOwnerException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public NotDeploymentOwnerException(String deploymentReference) {
        super("To stop deployment " + deploymentReference + " you have to be the one whole deployed it ");
    }

}