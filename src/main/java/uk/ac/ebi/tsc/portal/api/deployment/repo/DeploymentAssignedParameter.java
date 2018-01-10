package uk.ac.ebi.tsc.portal.api.deployment.repo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Entity
public class DeploymentAssignedParameter {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	private String parameterValue;

	@ManyToOne
	private Deployment deployment;

	private String parameterName;


	DeploymentAssignedParameter() { // jpa only
	}

	public DeploymentAssignedParameter(String parameterName, String parameterValue, Deployment deployment) {
		this.parameterValue = parameterValue;
		this.deployment = deployment;
		this.parameterName = parameterName;
	}

	public Long getId() {
		return id;
	}

	public String getParameterValue() {
		return parameterValue;
	}

	public Deployment getDeployment() {
		return deployment;
	}

	public void setDeployment(Deployment deployment) {
		this.deployment = deployment;
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

}
