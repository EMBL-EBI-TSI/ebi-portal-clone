package uk.ac.ebi.tsc.portal.api.error;

import org.springframework.http.HttpStatus;


public class ErrorMessage {
    private String error;
    private HttpStatus status;

    public ErrorMessage(String error, HttpStatus status) {
        this.error = error;
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
