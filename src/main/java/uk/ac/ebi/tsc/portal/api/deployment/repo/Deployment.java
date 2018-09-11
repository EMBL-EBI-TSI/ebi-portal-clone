package uk.ac.ebi.tsc.portal.api.deployment.repo;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Entity
public class Deployment{

	@ManyToOne
    private Account account;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique=true)
    private String reference;

    private String providerId;

    private String accessIp;
  
    @ManyToOne(cascade = CascadeType.ALL)
    public DeploymentApplication deploymentApplication;
    
    @NotNull
    @Column(name="cloud_provider_params_reference")
    private String cloudProviderParametersReference;
    
    @OneToOne(cascade = CascadeType.ALL)
    public DeploymentStatus deploymentStatus;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "deployment", cascade = CascadeType.ALL)
    public Collection<DeploymentAttachedVolume> attachedVolumes;

    
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "deployment", cascade = CascadeType.ALL)
    public Collection<DeploymentAssignedInput> assignedInputs;

   
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "deployment", cascade = CascadeType.ALL)
    public Collection<DeploymentAssignedParameter> assignedParameters;
    
   
    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch=FetchType.EAGER, mappedBy = "deployment", cascade = CascadeType.ALL)
    public Collection<DeploymentGeneratedOutput> generatedOutputs;
   
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToOne(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    public DeploymentConfiguration deploymentConfiguration;
 
    private java.sql.Timestamp startTime;
    
    private java.sql.Timestamp  deployedTime;
 
    private java.sql.Timestamp  failedTime;
 
    private java.sql.Timestamp  destroyedTime;

    private java.sql.Timestamp  lastNotificationTime;

    private String userSshKey;
    
    private String domainReference;
    
    Deployment() { 
    }

    public Deployment(String reference, Account account, DeploymentApplication deploymentApplication,
    		String cloudProviderParametersReference, String userSshKey, String domainReference) {
        this.reference = reference;
        this.account = account;
        this.deploymentApplication = deploymentApplication;
        this.deploymentStatus = new DeploymentStatus(this,DeploymentStatusEnum.STARTING);
        this.attachedVolumes = new LinkedList<>();
        this.assignedInputs = new LinkedList<>();
        this.assignedParameters = new LinkedList<>();
        this.generatedOutputs = new LinkedList<>();
        this.cloudProviderParametersReference = cloudProviderParametersReference;
        this.userSshKey = userSshKey;
        this.domainReference = domainReference;
    }

    public Long getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getAccessIp() {
        return accessIp;
		
    }

    public void setAccessIp(String accessIp) {
        this.accessIp = accessIp;
    }

    public Account getAccount() {
        return account;
    }

    public DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }

    public Collection<DeploymentAttachedVolume> getAttachedVolumes() {
        return attachedVolumes;
    }

    public void setAttachedVolumes(Collection<DeploymentAttachedVolume> attachedVolumes) {
        this.attachedVolumes = attachedVolumes;
    }

    public Collection<DeploymentAssignedInput> getAssignedInputs() {
        return assignedInputs;
    }

    public void setAssignedInputs(Collection<DeploymentAssignedInput> assignedInputs) {
        this.assignedInputs = assignedInputs;
    }

	public Collection<DeploymentAssignedParameter> getAssignedParameters() {
		return assignedParameters;
	}

	public Collection<DeploymentGeneratedOutput> getGeneratedOutputs() {
        return generatedOutputs;
    }

    public void setGeneratedOutputs(Collection<DeploymentGeneratedOutput> generatedOutputs) {
        this.generatedOutputs = generatedOutputs;
    }

	public DeploymentConfiguration getDeploymentConfiguration() {
		return deploymentConfiguration;
	}

	public void setDeploymentConfiguration(DeploymentConfiguration deploymentConfiguration) {
		this.deploymentConfiguration = deploymentConfiguration;
	}

	public DeploymentApplication getDeploymentApplication() {
		return deploymentApplication;
	}

	public void setDeploymentApplication(DeploymentApplication deploymentApplication) {
		this.deploymentApplication = deploymentApplication;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public void setDeploymentStatus(DeploymentStatus deploymentStatus) {
		this.deploymentStatus = deploymentStatus;
	}

	public void setAssignedParameters(Collection<DeploymentAssignedParameter> assignedParameters) {
		this.assignedParameters = assignedParameters;
	}

	public java.sql.Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(java.sql.Timestamp startTime) {
		this.startTime = startTime;
	}

	public java.sql.Timestamp getDeployedTime() {
		return deployedTime;
	}

	public void setDeployedTime(java.sql.Timestamp deployedTime) {
		this.deployedTime = deployedTime;
	}

	public java.sql.Timestamp getFailedTime() {
		return failedTime;
	}

	public void setFailedTime(java.sql.Timestamp failedTime) {
		this.failedTime = failedTime;
	}

	public java.sql.Timestamp getDestroyedTime() {
		return destroyedTime;
	}

	public void setDestroyedTime(java.sql.Timestamp destroyedTime) {
		this.destroyedTime = destroyedTime;
	}

	public String getCloudProviderParametersReference() {
		return cloudProviderParametersReference;
	}

	public void setCloudProviderParametersReference(String cloudProviderParametersReference) {
		this.cloudProviderParametersReference = cloudProviderParametersReference;
	}

    public Timestamp getLastNotificationTime() {
        return lastNotificationTime;
    }

    public void setLastNotificationTime(Timestamp lastNotificationTime) {
        this.lastNotificationTime = lastNotificationTime;
    }

    public String getUserSshKey() {
		return userSshKey;
	}

	public void setUserSshKey(String userSshKey) {
		this.userSshKey = userSshKey;
	}

	public String getDomainReference() {
		return domainReference;
	}

	public void setDomainReference(String domainReference) {
		this.domainReference = domainReference;
	}
	
}
