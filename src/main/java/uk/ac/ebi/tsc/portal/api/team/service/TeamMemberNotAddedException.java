package uk.ac.ebi.tsc.portal.api.team.service;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class TeamMemberNotAddedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TeamMemberNotAddedException(String teamName, String reason){
		
		super("User was not added to team " + teamName + ". Reason: " + reason);
		
	}
}
