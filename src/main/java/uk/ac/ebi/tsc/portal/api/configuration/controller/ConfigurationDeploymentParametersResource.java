package uk.ac.ebi.tsc.portal.api.configuration.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import uk.ac.ebi.tsc.portal.api.account.controller.AccountRestController;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParameters;
import uk.ac.ebi.tsc.portal.api.team.repo.Team;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class ConfigurationDeploymentParametersResource extends ResourceSupport {
	
	private String name;
	private Collection<ConfigurationDeploymentParameterResource> fields;
	private Collection<String> sharedWithTeamNames;
	private String accountUsername;
	private String reference;
	
	public ConfigurationDeploymentParametersResource(){}
	
	public ConfigurationDeploymentParametersResource(ConfigurationDeploymentParameters configurationDeploymentParameters){
		this.name = configurationDeploymentParameters.getName();
		this.accountUsername = configurationDeploymentParameters.getAccount().getUsername();
		this.fields = configurationDeploymentParameters.getConfigurationDeploymentParameter().stream().map(
                ConfigurationDeploymentParameterResource::new
        ).collect(Collectors.toList());
		this.sharedWithTeamNames = configurationDeploymentParameters.getSharedWithTeams().stream().map(
	            Team::getName
	    ).collect(Collectors.toList());
		this.reference = configurationDeploymentParameters.getReference() != null?
				configurationDeploymentParameters.getReference() : null;
		
		/*this.add(
                ControllerLinkBuilder.linkTo(
                        methodOn(
                                AccountRestController.class
                        ).getAccountByUsername(
                        		configurationDeploymentParameters.getAccount().getUsername()
                        )
                ).withSelfRel()
        );*/
	}

    

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<ConfigurationDeploymentParameterResource> getFields() {
		return fields;
	}

	public void setFields(Collection<ConfigurationDeploymentParameterResource> fields) {
		this.fields = fields;
	}

	public Collection<String> getSharedWithTeamNames() {
		return sharedWithTeamNames;
	}

	public String getAccountUsername() {
		return accountUsername;
	}

	public void setAccountUsername(String accountUsername) {
		this.accountUsername = accountUsername;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}
	
	
}
