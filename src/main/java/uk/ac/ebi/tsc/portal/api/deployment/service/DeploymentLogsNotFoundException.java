package uk.ac.ebi.tsc.portal.api.deployment.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class DeploymentLogsNotFoundException extends RuntimeException {

    public DeploymentLogsNotFoundException(String deploymentReference) {
        super("Could not find deployment logs for deployment '" + deploymentReference + "'.");
    }

}