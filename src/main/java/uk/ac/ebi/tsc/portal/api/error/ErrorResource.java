package uk.ac.ebi.tsc.portal.api.error;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.stereotype.Component;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Component
public class ErrorResource extends ResourceSupport {

    private String error;

    public ErrorResource() {
    }

    public ErrorResource(String error) {

        this.error = error;

    }

    public String getError() {
        return this.error;
    }

}
