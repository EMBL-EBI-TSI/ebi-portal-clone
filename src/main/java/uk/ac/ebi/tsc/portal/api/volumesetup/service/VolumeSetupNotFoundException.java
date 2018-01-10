package uk.ac.ebi.tsc.portal.api.volumesetup.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class VolumeSetupNotFoundException extends RuntimeException {

    public VolumeSetupNotFoundException(Long id) {
        super("Could not find volume setup '" + id + "'.");
    }

    public VolumeSetupNotFoundException(String name) {
        super("Could not find volume setup with name '" + name + "'.");
    }

}