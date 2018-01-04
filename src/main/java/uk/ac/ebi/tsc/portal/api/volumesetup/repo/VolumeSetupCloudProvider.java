package uk.ac.ebi.tsc.portal.api.volumesetup.repo;

import javax.persistence.*;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@Entity
public class VolumeSetupCloudProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    public String name;

    public String path;

    @ManyToOne
    public VolumeSetup volumeSetup;

    VolumeSetupCloudProvider() { // jpa only
    }

    public VolumeSetupCloudProvider(String name, String path, VolumeSetup volumeSetup) {
        this.name = name;
        this.path = path;
        this.volumeSetup = volumeSetup;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public VolumeSetup getVolumeSetup() {
        return volumeSetup;
    }

}
