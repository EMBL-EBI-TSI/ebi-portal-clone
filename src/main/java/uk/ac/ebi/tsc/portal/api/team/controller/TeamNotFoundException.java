package uk.ac.ebi.tsc.portal.api.team.controller;

public class TeamNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TeamNotFoundException(String name){
		
		super("Team " + name + " does not exist" );
		
	}
}
