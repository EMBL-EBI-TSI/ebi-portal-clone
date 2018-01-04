package uk.ac.ebi.tsc.portal.api.deployment.repo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
public class DeploymentConfigurationParameter {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private DeploymentConfiguration deploymentConfiguration;

	private String parameterName;

	@NotNull
	private String parameterValue;


	DeploymentConfigurationParameter() { // jpa only
	}

	public DeploymentConfigurationParameter(String parameterName, String parameterValue, DeploymentConfiguration deploymentConfiguration) {
		this.parameterName = parameterName;
		this.parameterValue = parameterValue;
		this.deploymentConfiguration = deploymentConfiguration;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DeploymentConfiguration getDeploymentConfiguration() {
		return deploymentConfiguration;
	}

	public void setDeploymentConfiguration(DeploymentConfiguration deploymentConfiguration) {
		this.deploymentConfiguration = deploymentConfiguration;
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public String getParameterValue() {
		return parameterValue;
	}

	public void setParameterValue(String parameterValue) {
		this.parameterValue = parameterValue;
	}
}
