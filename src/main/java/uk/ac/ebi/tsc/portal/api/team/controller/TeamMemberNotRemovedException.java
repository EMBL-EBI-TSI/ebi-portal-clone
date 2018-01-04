package uk.ac.ebi.tsc.portal.api.team.controller;

public class TeamMemberNotRemovedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TeamMemberNotRemovedException(String teamName){
		
		super("User was not removed from team " + teamName );
		
	}
}
