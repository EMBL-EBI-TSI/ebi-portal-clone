package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CloudProviderParamsCopyNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CloudProviderParamsCopyNotFoundException(String reference) {
        super("Could not find cloud provider copy with " + reference + " .");
    }

    public CloudProviderParamsCopyNotFoundException(String username, String name) {
        super("Could not find cloud provider parameters copy " + name + " for user '" + username + "'.");
    }

    public CloudProviderParamsCopyNotFoundException(Long cloudCredentialsId) {
        super("Could not cloud provider parameters copy for ID '" + cloudCredentialsId + "'.");
    }
    
    public CloudProviderParamsCopyNotFoundException(String name, Long cloudCredentialsId) {
        super("Could not cloud provider parameters copy " + name + "and ID '" + cloudCredentialsId + "'.");
    }

}