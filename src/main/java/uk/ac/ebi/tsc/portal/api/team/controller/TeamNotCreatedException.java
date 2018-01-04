package uk.ac.ebi.tsc.portal.api.team.controller;

public class TeamNotCreatedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TeamNotCreatedException(String teamName){
		
		super("Team " + teamName + " failed to get created");
		
	}
}
