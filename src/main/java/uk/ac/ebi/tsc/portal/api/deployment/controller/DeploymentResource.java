package uk.ac.ebi.tsc.portal.api.deployment.controller;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.stereotype.Component;

import uk.ac.ebi.tsc.portal.api.account.controller.AccountRestController;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller.CloudProviderParametersCopyResource;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopy;
import uk.ac.ebi.tsc.portal.api.deployment.repo.Deployment;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Component
public class DeploymentResource extends ResourceSupport {

	private Long id;
    public String reference;
    public String accountUsername;
    public String accountGivenName;
    public String accountEmail;
    public String applicationName;
    public String applicationAccountUsername;
    public String providerId;
    public String accessIp;
    public String configurationName;
    public String configurationAccountUsername;
    public java.sql.Timestamp startedTime;
    public java.sql.Timestamp deployedTime;
    public java.sql.Timestamp  failedTime;
    public java.sql.Timestamp  destroyedTime;
    public java.sql.Timestamp  lastNotificationTime;
    public CloudProviderParametersCopyResource cloudProviderParametersCopy;
    public Collection<DeploymentAttachedVolumeResource> attachedVolumes;
    public Collection<DeploymentAssignedInputResource> assignedInputs;
    public Collection<DeploymentAssignedParameterResource> assignedParameters;
    public Collection<DeploymentGeneratedOutputResource> generatedOutputs;
    public String userSshKey;
    
    public DeploymentResource() {
    }

    public DeploymentResource(Deployment deployment, CloudProviderParamsCopy cloudProviderParametersCopy) {

        this.id = deployment.getId();
        this.reference = deployment.getReference();
        this.accountUsername = deployment.getAccount().getUsername();
        this.accountGivenName = deployment.getAccount().getGivenName();
        this.accountEmail = deployment.getAccount().getEmail();
        this.applicationName = deployment.getDeploymentApplication().getName();
        this.applicationAccountUsername = deployment.getDeploymentApplication().getAccount().getUsername();
        this.providerId = deployment.getProviderId();
        this.accessIp = deployment.getAccessIp();
        this.startedTime = deployment.getStartTime();
        this.deployedTime = deployment.getDeployedTime();
        this.failedTime = deployment.getFailedTime();
        this.destroyedTime = deployment.getDestroyedTime();
        this.lastNotificationTime = deployment.getLastNotificationTime();
        this.cloudProviderParametersCopy = cloudProviderParametersCopy != null ?
        		new CloudProviderParametersCopyResource(cloudProviderParametersCopy): new CloudProviderParametersCopyResource();
        this.userSshKey = deployment.getUserSshKey();
        this.attachedVolumes = deployment.getAttachedVolumes().stream().map(
                DeploymentAttachedVolumeResource::new
        ).collect(Collectors.toList());
        this.assignedInputs = deployment.getAssignedInputs().stream().map(
                DeploymentAssignedInputResource::new
        ).collect(Collectors.toList());
        this.assignedParameters = deployment.getAssignedParameters().stream().map(
                DeploymentAssignedParameterResource::new
        ).collect(Collectors.toList());
        this.generatedOutputs = deployment.getGeneratedOutputs().stream().map(
                DeploymentGeneratedOutputResource::new
        ).collect(Collectors.toList());
        this.configurationName = deployment.getDeploymentConfiguration() != null ? deployment.getDeploymentConfiguration().getName()
        		: null;
        this.configurationAccountUsername = deployment.getDeploymentConfiguration() != null ? deployment.getDeploymentConfiguration().getOwnerAccountUsername()
                : null;
        this.add(
            linkTo(AccountRestController.class)
                .slash(deployment.getAccount().getUsername())
            .withRel("account")
        );
        
        this.add(
            linkTo(DeploymentRestController.class)
                .slash(deployment.getReference())
                .slash("status")
            .withRel("status")
        );
        this.add(
            linkTo(DeploymentRestController.class)
            .withRel("deployments")
        );
        this.add(
            linkTo(DeploymentRestController.class)
                .slash(deployment.getReference()).
            withSelfRel()
    );


    }

    public String getReference() {
        return reference;
    }

    public String getAccountUsername() {
        return accountUsername;
    }

    public String getAccountGivenName() {
        return accountGivenName;
    }

    public String getAccountEmail() {
        return accountEmail;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getAccessIp() {
        return accessIp;
    }

    public Collection<DeploymentAttachedVolumeResource> getAttachedVolumes() {
        return attachedVolumes;
    }

    public Collection<DeploymentAssignedInputResource> getAssignedInputs() {
        return assignedInputs;
    }

    public Collection<DeploymentAssignedParameterResource> getAssignedParameters() {
		return assignedParameters;
	}

	public Collection<DeploymentGeneratedOutputResource> getGeneratedOutputs() {
        return generatedOutputs;
    }
	public void setAssignedInputs(Collection<DeploymentAssignedInputResource> assignedInputs) {
		this.assignedInputs = assignedInputs;
	}

	public String getConfigurationName() {
		return configurationName;
	}

	public void setConfigurationName(String configurationName) {
		this.configurationName = configurationName;
	}

	public String getApplicationAccountUsername() {
		return applicationAccountUsername;
	}

	public void setApplicationAccountUsername(String applicationAccountUsername) {
		this.applicationAccountUsername = applicationAccountUsername;
	}

	public String getConfigurationAccountUsername() {
		return configurationAccountUsername;
	}

    public Timestamp getStartedTime() {
        return startedTime;
    }

    public Timestamp getDeployedTime() {
        return deployedTime;
    }

    public Timestamp getFailedTime() {
        return failedTime;
    }

    public Timestamp getDestroyedTime() {
        return destroyedTime;
    }

    public Timestamp getLastNotificationTime() {
        return lastNotificationTime;
    }

    public void setConfigurationAccountUsername(String configurationAccountUsername) {
		this.configurationAccountUsername = configurationAccountUsername;
	}

	public CloudProviderParametersCopyResource getCloudProviderParametersCopy() {
		return cloudProviderParametersCopy;
	}

	public void setCloudProviderParametersCopy(CloudProviderParametersCopyResource cloudProviderParametersCopy) {
		this.cloudProviderParametersCopy = cloudProviderParametersCopy;
	}

	public String getUserSshKey() {
		return userSshKey;
	}

	public void setUserSshKey(String userSshKey) {
		this.userSshKey = userSshKey;
	}
}
