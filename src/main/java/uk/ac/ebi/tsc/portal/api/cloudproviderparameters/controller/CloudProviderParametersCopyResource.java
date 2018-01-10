package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import uk.ac.ebi.tsc.portal.api.account.controller.AccountRestController;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopy;
import uk.ac.ebi.tsc.portal.api.team.repo.Team;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class CloudProviderParametersCopyResource extends ResourceSupport {

    private Long id;
    private String name;
    private String cloudProvider;
    private String accountUsername;
    private String cloudProviderParametersReference;

    public CloudProviderParametersCopyResource() {
    }

    public CloudProviderParametersCopyResource(CloudProviderParamsCopy cloudProviderParametersCopy) {

        this.id = cloudProviderParametersCopy.getId();
        this.name = cloudProviderParametersCopy.getName();
        this.accountUsername = cloudProviderParametersCopy.account.getUsername();
        this.cloudProvider = cloudProviderParametersCopy.getCloudProvider();
        this.cloudProviderParametersReference = cloudProviderParametersCopy.getCloudProviderParametersReference();
        
        this.add(
                ControllerLinkBuilder.linkTo(
                        methodOn(
                                AccountRestController.class
                        ).getAccountByUsername(
                        		cloudProviderParametersCopy.getAccount().getUsername()
                        )
                ).withSelfRel()
        );
    }

    public String getName() {
        return name;
    }

    public String getAccountUsername() {
		return accountUsername;
	}

	public void setAccountUsername(String accountUsername) {
		this.accountUsername = accountUsername;
	}

	public String getCloudProvider() {
        return cloudProvider;
    }

	public String getCloudProviderParametersReference() {
		return cloudProviderParametersReference;
	}

	public void setCloudProviderParametersReference(String cloudProviderParametersReference) {
		this.cloudProviderParametersReference = cloudProviderParametersReference;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCloudProvider(String cloudProvider) {
		this.cloudProvider = cloudProvider;
	}
}
