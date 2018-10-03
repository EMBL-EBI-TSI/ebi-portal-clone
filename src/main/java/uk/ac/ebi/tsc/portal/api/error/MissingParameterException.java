package uk.ac.ebi.tsc.portal.api.error;

import static java.lang.String.format;


@SuppressWarnings("serial")
public class MissingParameterException extends RuntimeException {

    public MissingParameterException(String paramName) {
        
        super(format("Missing parameter: '%s'.", paramName));
    }
}
