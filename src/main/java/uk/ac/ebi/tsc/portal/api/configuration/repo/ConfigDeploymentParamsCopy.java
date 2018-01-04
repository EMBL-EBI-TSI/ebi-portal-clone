package uk.ac.ebi.tsc.portal.api.configuration.repo;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.team.repo.Team;


@Entity
public class ConfigDeploymentParamsCopy {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public String name;

	@ManyToOne
	private Account account;

	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(mappedBy = "configDeploymentParamsCopy", cascade = CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER)
	private Set<ConfigDeploymentParamCopy> configDeploymentParamCopy = new HashSet<>();
	
    @NotNull
    @Column(name="config_deployment_params_reference")
	private String configurationDeploymentParametersReference;

	ConfigDeploymentParamsCopy() { // jpa only
	}

	public ConfigDeploymentParamsCopy(ConfigurationDeploymentParameters configurationDeploymentParameters ) {
		this.name = configurationDeploymentParameters.getName();
		this.account = configurationDeploymentParameters.getAccount();
		this.configurationDeploymentParametersReference = configurationDeploymentParameters.getReference();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Set<ConfigDeploymentParamCopy> getConfigDeploymentParamCopy() {
		return configDeploymentParamCopy;
	}

	public void setConfigDeploymentParamCopy(Set<ConfigDeploymentParamCopy> configDeploymentParamCopy) {
		this.configDeploymentParamCopy = configDeploymentParamCopy;
	}

	public Account getAccount() {
		return account;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getConfigurationDeploymentParametersReference() {
		return configurationDeploymentParametersReference;
	}

	public void setConfigurationDeploymentParametersReference(String configurationDeploymentParametersReference) {
		this.configurationDeploymentParametersReference = configurationDeploymentParametersReference;
	}

	public void setAccount(Account account) {
		this.account = account;
	}
	
}
