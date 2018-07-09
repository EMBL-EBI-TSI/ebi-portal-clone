package uk.ac.ebi.tsc.portal.api.team.service;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class TeamNotCreatedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TeamNotCreatedException(String teamName, String reason){
		
		super("Failed to create team " + teamName + ". Reason: " + reason);
		
	}
}
