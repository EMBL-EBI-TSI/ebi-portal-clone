package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import uk.ac.ebi.tsc.portal.api.account.controller.AccountRestController;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.team.repo.Team;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class CloudProviderParametersResource extends ResourceSupport {

    private Long id;
    private String name;
    private String cloudProvider;
    private String accountUsername;
    private String accountGivenName;
    private String accountEmail;
    private Collection<CloudProviderParametersFieldResource> fields;
    private Collection<String> sharedWithAccountEmails;
    private Collection<String> sharedWithTeamNames;

    public CloudProviderParametersResource() {
    }

    public CloudProviderParametersResource(CloudProviderParameters cloudProviderParameters) {

        this.id = cloudProviderParameters.getId();
        this.name = cloudProviderParameters.getName();
        this.accountUsername = cloudProviderParameters.account.getUsername();
        this.accountGivenName = cloudProviderParameters.account.getGivenName();
        this.accountEmail = cloudProviderParameters.account.getEmail();
        this.cloudProvider = cloudProviderParameters.getCloudProvider();
        this.fields = cloudProviderParameters.getFields().stream().map(
                CloudProviderParametersFieldResource::new
        ).collect(Collectors.toList());
        this.sharedWithAccountEmails = cloudProviderParameters.getSharedWith().stream().map(
                Account::getEmail
        ).collect(Collectors.toList());
        this.sharedWithTeamNames = cloudProviderParameters.getSharedWithTeams().stream().map(
                Team::getName
        ).collect(Collectors.toList());

        this.add(
                ControllerLinkBuilder.linkTo(
                        methodOn(
                                AccountRestController.class
                        ).getAccountByUsername(
                                cloudProviderParameters.getAccount().getUsername()
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

    public String getAccountGivenName() {
        return accountGivenName;
    }

    public String getAccountEmail() {
        return accountEmail;
    }

    public String getCloudProvider() {
        return cloudProvider;
    }

    public Collection<CloudProviderParametersFieldResource> getFields() {
        return fields;
    }

    public Collection<String> getSharedWithAccountEmails() {
        return sharedWithAccountEmails;
    }

    public Collection<String> getSharedWithTeamNames() {
        return sharedWithTeamNames;
    }
}
