package uk.ac.ebi.tsc.portal.api.configuration.controller;

import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ebi.tsc.portal.api.account.service.UserNotFoundException;
import uk.ac.ebi.tsc.portal.api.configuration.service.ConfigurationNotFoundException;

import java.io.IOException;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ControllerAdvice
class ConfigurationControllerAdvice {

    @ResponseBody
    @ExceptionHandler(ConfigurationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    VndErrors applicationNotFoundExceptionHandler(ConfigurationNotFoundException ex) {
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
    @ExceptionHandler(InvalidConfigurationInputException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors invalidInputExceptionHandler(InvalidConfigurationInputException ex) {
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(ExistingConfigurationNameException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors existingNameExceptionHandler(ExistingConfigurationNameException ex) {
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(CloudProviderParametersNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    VndErrors existingNameExceptionHandler(CloudProviderParametersNotFoundException ex) {
        return new VndErrors("error", ex.getMessage());
    }
}