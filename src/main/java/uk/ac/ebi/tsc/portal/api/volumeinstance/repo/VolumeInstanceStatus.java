package uk.ac.ebi.tsc.portal.api.volumeinstance.repo;

import javax.persistence.*;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@Entity
public class VolumeInstanceStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private VolumeInstanceStatusEnum status;

    @OneToOne
    public VolumeInstance volumeInstance;


    VolumeInstanceStatus() { // jpa only
    }

    public VolumeInstanceStatus(VolumeInstance volumeInstance, VolumeInstanceStatusEnum status) {
        this.volumeInstance = volumeInstance;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public VolumeInstanceStatusEnum getStatus() {
        return status;
    }

    public void setStatus(VolumeInstanceStatusEnum status) {
        this.status = status;
    }

    public VolumeInstance getVolumeInstance() {
        return volumeInstance;
    }

}
