package uk.ac.ebi.tsc.portal.api.team.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TeamNameInvalidInputException extends RuntimeException  {

	public TeamNameInvalidInputException(String message){
		super(message);
	}
	
}
