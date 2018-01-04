package uk.ac.ebi.tsc.portal.api.team.controller;

public class TeamMemberNotAddedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TeamMemberNotAddedException(String teamName){
		
		super("User was not added to team " + teamName );
		
	}
}
