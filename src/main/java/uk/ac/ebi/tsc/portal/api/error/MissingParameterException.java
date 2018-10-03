package uk.ac.ebi.tsc.portal.api.error;

import static java.lang.String.format;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class MissingParameterException extends RuntimeException {

    public MissingParameterException(String paramName) {
        
        super(format("Missing parameter: '%s'.", paramName));
    }
}
