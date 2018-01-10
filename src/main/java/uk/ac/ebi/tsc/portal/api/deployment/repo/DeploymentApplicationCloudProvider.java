package uk.ac.ebi.tsc.portal.api.deployment.repo;

import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Entity
@Table(name="dep_app_cloud_provider")
public class DeploymentApplicationCloudProvider {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique=true)
	public String name;

	public String path;

	@LazyCollection(LazyCollectionOption.FALSE)
	@ManyToOne(cascade = CascadeType.ALL)
	public DeploymentApplication deploymentApplication;

	@OneToMany(mappedBy = "depAppCloudProvider", cascade = CascadeType.ALL)
	@Column(name="dep_app_cloud_provider_input")
	public Collection<DeploymentApplicationCloudProviderInput> inputs;

	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(mappedBy = "depAppCloudProvider", cascade = CascadeType.ALL)
	@Column(name="dep_app_cloud_provider_output")
	public Collection<DeploymentApplicationCloudProviderOutput> outputs;

	@OneToMany(mappedBy = "depAppCloudProvider", cascade = CascadeType.ALL)
	@Column(name="dep_app_cloud_provider_volume")
	public Collection<DeploymentApplicationCloudProviderVolume> volumes;

	DeploymentApplicationCloudProvider() { 
	}

	public DeploymentApplicationCloudProvider(String name, String path, DeploymentApplication deploymentApplication) {
		this.name = name;
		this.path = path;
		this.deploymentApplication = deploymentApplication;
		this.inputs = new LinkedList<>();
		this.outputs = new LinkedList<>();
		this.volumes = new LinkedList<>();
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public DeploymentApplication getDeploymentApplication() {
		return deploymentApplication;
	}

	public Collection<DeploymentApplicationCloudProviderInput> getInputs() {
		return inputs;
	}

	public Collection<DeploymentApplicationCloudProviderOutput> getOutputs() {
		return outputs;
	}

	public Collection<DeploymentApplicationCloudProviderVolume> getVolumes() {
		return volumes;
	}

	public void setInputs(Collection<DeploymentApplicationCloudProviderInput> inputs) {
		this.inputs = inputs;
	}

	public void setOutputs(Collection<DeploymentApplicationCloudProviderOutput> outputs) {
		this.outputs = outputs;
	}

	public void setVolumes(Collection<DeploymentApplicationCloudProviderVolume> volumes) {
		this.volumes = volumes;
	}
}
