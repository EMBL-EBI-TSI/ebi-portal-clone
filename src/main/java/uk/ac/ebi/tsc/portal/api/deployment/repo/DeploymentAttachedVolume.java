package uk.ac.ebi.tsc.portal.api.deployment.repo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Entity
public class DeploymentAttachedVolume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @ManyToOne
    private Deployment deployment;

    private String volumeInstanceReference;

    private String volumeInstanceProviderId;

    DeploymentAttachedVolume() { // jpa only
    }

    public DeploymentAttachedVolume(String name, Deployment deployment, String volumeInstanceReference) {
        this.name = name;
        this.deployment = deployment;
        this.volumeInstanceReference = volumeInstanceReference;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }

    public String getVolumeInstanceReference() {
        return volumeInstanceReference;
    }

    public void setVolumeInstanceReference(String volumeInstanceReference) {
        this.volumeInstanceReference = volumeInstanceReference;
    }

    public String getVolumeInstanceProviderId() {
        return volumeInstanceProviderId;
    }

    public void setVolumeInstanceProviderId(String volumeInstanceProviderId) {
        this.volumeInstanceProviderId = volumeInstanceProviderId;
    }
}
