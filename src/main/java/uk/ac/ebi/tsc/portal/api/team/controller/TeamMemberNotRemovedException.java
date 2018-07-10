package uk.ac.ebi.tsc.portal.api.team.controller;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class TeamMemberNotRemovedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TeamMemberNotRemovedException(String teamName, String reason){
		
		super("User was not removed from team " + teamName + ", Reason: " + reason);
		
	}
}
