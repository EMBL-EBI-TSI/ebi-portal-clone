package uk.ac.ebi.tsc.portal.api.volumesetup.repo;

import uk.ac.ebi.tsc.portal.api.account.repo.Account;

import javax.persistence.*;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@Entity
public class VolumeSetup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Account account;

    public String repoUri;

    @Column(unique=true)
    public String repoPath;

    public String name;

    @OneToMany(mappedBy = "volumeSetup", cascade = CascadeType.ALL)
    public Collection<VolumeSetupCloudProvider> cloudProviders;

    VolumeSetup() { // jpa only
    }

    public VolumeSetup(String repoUri, String repoPath, String name, Account account) {
        this.repoUri = repoUri;
        this.repoPath = repoPath;
        this.name = name;
        this.account = account;
        this.cloudProviders = new LinkedList<>();
    }


    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public String getRepoUri() {
        return repoUri;
    }

    public String getRepoPath() {
        return repoPath;
    }

    public String getName() {
        return name;
    }

    public Collection<VolumeSetupCloudProvider> getCloudProviders() {
        return cloudProviders;
    }
}
