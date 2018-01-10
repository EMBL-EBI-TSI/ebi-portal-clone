package uk.ac.ebi.tsc.portal.api.volumesetup.controller;

import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDownloaderException;
import uk.ac.ebi.tsc.portal.api.application.service.ApplicationNotFoundException;

import java.io.IOException;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ControllerAdvice
class VolumeSetupControllerAdvice {

    @ResponseBody
    @ExceptionHandler(ApplicationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    VndErrors applicationNotFoundExceptionHandler(ApplicationNotFoundException ex) {
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(ApplicationDownloaderException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    VndErrors applicationDownloadingExceptionHandler(ApplicationDownloaderException ex) {
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    VndErrors ioExceptionHandler(IOException ex) {
        return new VndErrors("error", ex.getMessage());
    }
}