package uk.ac.ebi.tsc.portal.api.volumeinstance.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class VolumeInstanceNotFoundException extends RuntimeException {

    public VolumeInstanceNotFoundException(String userId, Long deploymentId) {
        super("Could not find deployment with ID '" + deploymentId + "' for user '" + userId + "'.");
    }

    public VolumeInstanceNotFoundException(String reference) {
        super("Could not find deployment with reference '" + reference + "'.");
    }

}