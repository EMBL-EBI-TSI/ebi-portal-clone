package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller;

import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ebi.tsc.portal.api.account.service.UserNotFoundException;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParametersNotFoundException;

import java.io.IOException;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@ControllerAdvice
class CloudProviderParametersControllerAdvice {

    @ResponseBody
    @ExceptionHandler(CloudProviderParametersNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    VndErrors applicationNotFoundExceptionHandler(CloudProviderParametersNotFoundException ex) {
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    VndErrors userNotFoundExceptionHandler(UserNotFoundException ex) {
        return new VndErrors("error", ex.getMessage());
    }


    @ResponseBody
    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    VndErrors ioExceptionHandler(IOException ex) {
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(InvalidCloudProviderParametersInputException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors invalidInputExceptionHandler(InvalidCloudProviderParametersInputException ex) {
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(ExistingCloudProviderParametersNameException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors existingNameExceptionHandler(ExistingCloudProviderParametersNameException ex) {
        return new VndErrors("error", ex.getMessage());
    }

}