package uk.ac.ebi.tsc.portal.api.deployment.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class DeploymentNotFoundException extends RuntimeException {

    public DeploymentNotFoundException(String userId, Long deploymentId) {
        super("Could not find deployment with ID '" + deploymentId + "' for user '" + userId + "'.");
    }

    public DeploymentNotFoundException(String reference) {
        super("Could not find deployment with reference '" + reference + "'.");
    }

}