package uk.ac.ebi.tsc.portal.api.configuration.repo;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;

import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.team.repo.Team;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Entity
public class ConfigurationDeploymentParameters {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public String name;

	@ManyToOne
	private Account account;

	@OneToMany(mappedBy = "configurationDeploymentParameters", cascade = CascadeType.ALL, orphanRemoval=true)
	private Set<ConfigurationDeploymentParameter> configurationDeploymentParameter = new HashSet<>();
	
	@ManyToMany(mappedBy="configDepParamsBelongingToTeam")
    private Set<Team> sharedWithTeams = new HashSet<>();
	
	private String reference;

	ConfigurationDeploymentParameters() { // jpa only
	}

	public ConfigurationDeploymentParameters(String name, Account account) {
		this.name = name;
		this.account = account;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Set<ConfigurationDeploymentParameter> getConfigurationDeploymentParameter() {
		return configurationDeploymentParameter;
	}
	
	public void setConfigurationDeploymentParameter(
			Set<ConfigurationDeploymentParameter> configurationDeploymentParameter) {
		this.configurationDeploymentParameter = configurationDeploymentParameter;
	}

	public Account getAccount() {
		return account;
	}

	public Set<Team> getSharedWithTeams() {
		return sharedWithTeams;
	}

	public void setSharedWithTeams(Set<Team> sharedWithTeams) {
		this.sharedWithTeams = sharedWithTeams;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	@PreRemove
	private void removeDeploymentParametersFromTeams(){
		for(Team team: sharedWithTeams ){
			team.getConfigDepParamsBelongingToTeam().remove(this);
		}
	}
}
