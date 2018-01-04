package uk.ac.ebi.tsc.portal.api.team.controller;

public class TeamNotDeletedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TeamNotDeletedException(String teamName){
		
		super("Team " + teamName + " failed to get deleted");
		
	}
}
