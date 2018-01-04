package uk.ac.ebi.tsc.portal.api.volumeinstance.repo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetup;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@Entity
public class VolumeInstance {

    @JsonIgnore
    @ManyToOne
    private Account account;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique=true)
    private String reference;

    private String providerId;

    @ManyToOne
    public CloudProviderParameters cloudProviderParameters;

    @ManyToOne
    public VolumeSetup volumeSetup;

    @OneToOne(cascade = CascadeType.ALL)
    public VolumeInstanceStatus volumeInstanceStatus;

    VolumeInstance() { // jpa only
    }

    public VolumeInstance(String reference, Account account, VolumeSetup volumeSetup, CloudProviderParameters cloudProviderParameters) {
        this.reference = reference;
        this.account = account;
        this.volumeSetup = volumeSetup;
        this.cloudProviderParameters = cloudProviderParameters;
        this.volumeInstanceStatus = new VolumeInstanceStatus(this, VolumeInstanceStatusEnum.STARTING);
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

    public Account getAccount() {
        return account;
    }

    public VolumeSetup getVolumeSetup() {
        return volumeSetup;
    }

    public VolumeInstanceStatus getVolumeInstanceStatus() {
        return volumeInstanceStatus;
    }

    public void setVolumeInstanceStatus(VolumeInstanceStatus volumeInstanceStatus) {
        this.volumeInstanceStatus = volumeInstanceStatus;
    }

    public CloudProviderParameters getCloudProviderParameters() {
        return cloudProviderParameters;
    }

    public void setCloudProviderParameters(CloudProviderParameters cloudProviderParameters) {
        this.cloudProviderParameters = cloudProviderParameters;
    }
}
