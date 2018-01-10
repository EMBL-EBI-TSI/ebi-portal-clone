package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CloudProviderParametersNotFoundException extends RuntimeException {

    public CloudProviderParametersNotFoundException(String userId) {
        super("Could not find/no permission for user '" + userId + "' to use the cloud provider.");
    }

    public CloudProviderParametersNotFoundException(String username, String name) {
        super("Could not find cloud provider parameters " + name + " for user '" + username + "'.");
    }

    public CloudProviderParametersNotFoundException(Long cloudCredentialsId) {
        super("Could not cloud provider parameters for ID '" + cloudCredentialsId + "'.");
    }
    
    public CloudProviderParametersNotFoundException(String name, Long cloudCredentialsId) {
        super("Could not cloud provider parameters " + name + "and ID '" + cloudCredentialsId + "'.");
    }

}