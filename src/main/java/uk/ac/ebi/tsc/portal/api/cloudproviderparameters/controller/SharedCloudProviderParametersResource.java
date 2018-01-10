package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller;

import org.springframework.hateoas.ResourceSupport;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class SharedCloudProviderParametersResource extends ResourceSupport{
	
	private String userEmail;
	private String cloudProviderParameterName;
	
	public SharedCloudProviderParametersResource() {
		
	}

	public SharedCloudProviderParametersResource(String userEmail, String cloudProviderParameterName) {
		this.userEmail = userEmail;
		this.cloudProviderParameterName = cloudProviderParameterName;
    }

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getCloudProviderParameterName() {
		return cloudProviderParameterName;
	}

	public void setCloudProviderParameterName(String cloudProviderParameterName) {
		this.cloudProviderParameterName = cloudProviderParameterName;
	}


}
