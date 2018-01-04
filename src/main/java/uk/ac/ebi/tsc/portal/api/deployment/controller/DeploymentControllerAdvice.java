package uk.ac.ebi.tsc.portal.api.deployment.controller;

import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDeployerException;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParametersNotFoundException;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentNotFoundException;

import java.io.IOException;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@ControllerAdvice
class DeploymentControllerAdvice {

    @ResponseBody
    @ExceptionHandler(DeploymentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    VndErrors deploymentNotFoundExceptionHandler(DeploymentNotFoundException ex) {
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(ApplicationDeployerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    VndErrors applicationDownloadingExceptionHandler(ApplicationDeployerException ex) {
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(CloudProviderParametersNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    VndErrors cloudCredentialsNotFoundExceptionHandler(CloudProviderParametersNotFoundException ex) {
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    VndErrors ioExceptionHandler(IOException ex) {
        return new VndErrors("error", ex.getMessage());
    }
}