package uk.ac.ebi.tsc.portal.api.configuration.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UsageLimitsException extends RuntimeException {

	public UsageLimitsException(String configurationReference, Double hardUsageLimit) {
        super("Configuration " + configurationReference +" hard usage limit of " + hardUsageLimit + " reached");
    }

}