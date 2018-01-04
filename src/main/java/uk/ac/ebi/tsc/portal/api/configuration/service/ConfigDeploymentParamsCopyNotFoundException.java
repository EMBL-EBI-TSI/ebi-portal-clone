package uk.ac.ebi.tsc.portal.api.configuration.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
/**
 * 
 * @author navis
 *
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ConfigDeploymentParamsCopyNotFoundException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConfigDeploymentParamsCopyNotFoundException(String reference) {
        super("Could not find configuration deployment parameters " + reference + " for the user ");
    }

}
