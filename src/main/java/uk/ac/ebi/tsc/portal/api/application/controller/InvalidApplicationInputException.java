package uk.ac.ebi.tsc.portal.api.application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvalidApplicationInputException extends RuntimeException {
	
	public InvalidApplicationInputException(String username, String name) {
        super("Could not create applkication '" + name + "' for user '" + username + "'. Invalid input.");
    }
	

}
