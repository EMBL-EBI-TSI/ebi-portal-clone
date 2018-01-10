package uk.ac.ebi.tsc.portal.api.deployment.repo;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.application.repo.Application;

import javax.persistence.*;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Entity
public class DeploymentApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Account account;

    public String repoUri;

    @Column(unique=true)
    public String repoPath;

    public String name;

    public String about;

    public String contact;

    public String version;

	@LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "deploymentApplication", cascade = CascadeType.ALL)
    public Collection<DeploymentApplicationCloudProvider> cloudProviders;
    
    DeploymentApplication() { 
    }

    public DeploymentApplication(Application application) {
        this.repoUri = application.getRepoUri();
        this.repoPath = application.getRepoPath();
        this.name = application.getName();
        this.account = application.getAccount();
        this.about = application.getAbout();
        this.contact = application.getContact();
        this.version = application.getVersion();
        this.cloudProviders = new LinkedList<>();
    }
    
    public DeploymentApplication(DeploymentApplication application) {
        this.repoUri = application.getRepoUri();
        this.repoPath = application.getRepoPath();
        this.name = application.getName();
        this.account = application.getAccount();
        this.about = application.getAbout();
        this.contact = application.getContact();
        this.version = application.getVersion();
        this.cloudProviders = new LinkedList<>();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public String getRepoUri() {
		return repoUri;
	}

	public void setRepoUri(String repoUri) {
		this.repoUri = repoUri;
	}

	public String getRepoPath() {
		return repoPath;
	}

	public void setRepoPath(String repoPath) {
		this.repoPath = repoPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Collection<DeploymentApplicationCloudProvider> getCloudProviders() {
		return cloudProviders;
	}

	public void setCloudProviders(Collection<DeploymentApplicationCloudProvider> cloudProviders) {
		this.cloudProviders = cloudProviders;
	}
    
}

