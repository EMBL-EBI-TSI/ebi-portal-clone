package uk.ac.ebi.tsc.portal.api.application.controller;

import org.springframework.hateoas.ResourceSupport;

public class SharedApplicationResource extends ResourceSupport{
	
	private String userEmail;
	private String applicationName;
	
	
	public SharedApplicationResource() {}
	
	public SharedApplicationResource(String userEmail, String applicationName) {
		this.userEmail = userEmail;
		this.applicationName = applicationName;
	}



	public String getUserEmail() {
		return userEmail;
	}


	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}


	public String getApplicationName() {
		return applicationName;
	}


	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	
	

	
	
}
