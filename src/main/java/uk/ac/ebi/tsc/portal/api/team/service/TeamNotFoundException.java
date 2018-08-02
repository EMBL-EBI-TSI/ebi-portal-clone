package uk.ac.ebi.tsc.portal.api.team.service;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class TeamNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TeamNotFoundException(String name){
		
		super("Team " + name + " does not exist" );
		
	}
}
