package uk.ac.ebi.tsc.portal.api.account.repo;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 **/

import uk.ac.ebi.tsc.portal.api.application.repo.Application;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParameters;
import uk.ac.ebi.tsc.portal.api.deployment.repo.Deployment;
import uk.ac.ebi.tsc.portal.api.team.repo.Team;
import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetup;


/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Entity
@Order(Ordered.HIGHEST_PRECEDENCE)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String reference;

    public String username;

    public String givenName;

    public String password;

    public String email;

    public Date firstJoinedDate;

    public String organisation;

    public String avatarImageUrl;

    @OneToMany(mappedBy = "account")
    private Set<Deployment> deployments = new HashSet<>();

    @OneToMany(mappedBy = "account")
    private Set<Application> applications = new HashSet<>();

    @OneToMany(mappedBy = "account")
    private Set<VolumeSetup> volumeSetups = new HashSet<>();

    @OneToMany(mappedBy = "account")
    private Set<CloudProviderParameters> cloudCredentials = new HashSet<>();

    @OneToMany(mappedBy = "account")
    private Set<CloudProviderParameters> profiles = new HashSet<>();
    
    @OneToMany(mappedBy ="account")
    private Set<Team> ownedTeams = new HashSet<>();
    
    @ManyToMany(mappedBy="accountsBelongingToTeam")
    private Set<Team> memberOfTeams = new HashSet<>();
    
    @OneToMany(mappedBy = "account")
    private Set<ConfigurationDeploymentParameters> deploymentParameters = new HashSet<>();
    
    public Account(String reference, String username, String givenName, String password, String email,
                   Date firstJoinedDate, String organisation, String avatarImageUrl) {
        this.reference = reference;
        this.username = username;
        this.givenName = givenName;
        this.password = password;
        this.email = email;
        this.firstJoinedDate = firstJoinedDate;
        this.organisation = organisation;
        this.avatarImageUrl = avatarImageUrl;
    }

    Account() { // jpa only
    }

    public Set<Deployment> getDeployments() {
        return deployments;
    }

    public Set<Application> getApplications() {
        return applications;
    }

    public Set<VolumeSetup> getVolumeSetups() {
        return volumeSetups;
    }

    public Set<CloudProviderParameters> getCloudCredentials() {
        return cloudCredentials;
    }

    public Set<CloudProviderParameters> getProfiles() {
        return profiles;
    }

    public Long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getEmail() {
        return email;
    }

    public String getOrganisation() {
        return organisation;
    }

    public String getReference() {
        return reference;
    }

    public Date getFirstJoinedDate() {
        return firstJoinedDate;
    }

    public String getAvatarImageUrl() {
        return avatarImageUrl;
    }

	public Set<Team> getOwnedTeams() {
		return ownedTeams;
	}

	public void setOwnedTeams(Set<Team> ownedTeams) {
		this.ownedTeams = ownedTeams;
	}

	public Set<Team> getMemberOfTeams() {
		return memberOfTeams;
	}

	public void setMemberOfTeams(Set<Team> memberOfTeams) {
		this.memberOfTeams = memberOfTeams;
	}

	public Set<ConfigurationDeploymentParameters> getDeploymentParameters() {
		return deploymentParameters;
	}

	public void setDeploymentParameters(Set<ConfigurationDeploymentParameters> deploymentParameters) {
		this.deploymentParameters = deploymentParameters;
	}
}