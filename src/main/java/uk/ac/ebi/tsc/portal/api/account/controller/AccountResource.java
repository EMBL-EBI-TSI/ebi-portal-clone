package uk.ac.ebi.tsc.portal.api.account.controller;

import org.springframework.hateoas.ResourceSupport;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.deployment.controller.DeploymentRestController;

import java.sql.Date;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
class AccountResource extends ResourceSupport {

    private final Long id;
    private final String reference;
    private final String userName;
    private final String givenName;
    private final String email;
    private final Date firstJoinedDate;
    private final String organisation;
    private final String avatarImageUrl;

    AccountResource(Account account) {

        this.id = account.getId();
        this.reference = account.getReference();
        this.userName = account.getUsername();
        this.givenName = account.getGivenName();
        this.email = account.getEmail();
        this.firstJoinedDate = account.getFirstJoinedDate();
        this.organisation = account.getOrganisation();
        this.avatarImageUrl = account.getAvatarImageUrl();

        this.add(
                linkTo(
                        methodOn(
                                AccountRestController.class
                        ).getAccountByUsername(
                                account.getUsername()
                        )
                ).withSelfRel()
        );
        this.add(
                linkTo(
                        DeploymentRestController.class,
                        account.getUsername()
                ).withRel("deployments")
        );

    }

    public String getReference() {
        return reference;
    }

    public String getUserName() {
        return userName;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getEmail() {
        return email;
    }

    public String getOrganisation() {
        return organisation;
    }

    public Date getFirstJoinedDate() {
        return firstJoinedDate;
    }

    public String getAvatarImageUrl() {
        return avatarImageUrl;
    }
}
