package uk.ac.ebi.tsc.portal.clouddeployment.exceptions;

import org.springframework.stereotype.Component;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class ApplicationDownloaderException extends Exception {

    public ApplicationDownloaderException(String message) {
        super(message);
    }
}
