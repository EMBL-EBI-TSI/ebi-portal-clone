package uk.ac.ebi.tsc.portal.api.volumeinstance.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class VolumeInstanceStatusNotFoundException extends RuntimeException {

    public VolumeInstanceStatusNotFoundException(Long deploymentId) {
        super("Could not find deployment status for deployment '" + deploymentId + "'.");
    }

}