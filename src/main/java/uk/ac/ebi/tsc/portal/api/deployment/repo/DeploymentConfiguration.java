package uk.ac.ebi.tsc.portal.api.deployment.repo;

import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
public class DeploymentConfiguration {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	private Deployment deployment;
	
	@LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "deploymentConfiguration", cascade = CascadeType.ALL)
	private Collection<DeploymentConfigurationParameter> configurationParameters;

	private String name;

	private String ownerAccountUsername;
	
	private String sshKey;
	
	private String configurationReference;
    
    @Column(name="config_deployment_params_reference")
    private String configDeploymentParametersReference;
	
	public DeploymentConfiguration(){}

	public DeploymentConfiguration(String name, String accountUsername, 
			String sshKey, Deployment deployment, String configurationReference,
			String configDeploymentParamsReference){
		this.name = name;
		this.ownerAccountUsername = accountUsername;
		this.deployment = deployment;
		this.sshKey = sshKey;
		this.configurationParameters = new LinkedList<>();
		this.configurationReference = configurationReference;
		this.configDeploymentParametersReference = configDeploymentParamsReference;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Deployment getDeployment() {
		return deployment;
	}

	public void setDeployment(Deployment deployment) {
		this.deployment = deployment;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<DeploymentConfigurationParameter> getConfigurationParameters() {
		return configurationParameters;
	}

	public void setConfigurationParameters(Collection<DeploymentConfigurationParameter> configurationParameters) {
		this.configurationParameters = configurationParameters;
	}

	public String getOwnerAccountUsername() {
		return ownerAccountUsername;
	}

	public void setOwnerAccountUsername(String ownerAccountUsername) {
		this.ownerAccountUsername = ownerAccountUsername;
	}

	public String getSshKey() {
		return sshKey;
	}

	public void setSshKey(String sshKey) {
		this.sshKey = sshKey;
	}

	public String getConfigurationReference() {
		return configurationReference;
	}

	public void setConfigurationReference(String configurationReference) {
		this.configurationReference = configurationReference;
	}

	public String getConfigDeploymentParametersReference() {
		return configDeploymentParametersReference;
	}

	public void setConfigDeploymentParametersReference(String configDeploymentParametersReference) {
		this.configDeploymentParametersReference = configDeploymentParametersReference;
	}
	
}
