package uk.ac.ebi.tsc.portal.api.application.repo;

import javax.persistence.*;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Entity
public class ApplicationCloudProviderVolume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    public String name;

    @ManyToOne
    public ApplicationCloudProvider applicationCloudProvider;

    ApplicationCloudProviderVolume() { // jpa only
    }

    public ApplicationCloudProviderVolume(String name, ApplicationCloudProvider applicationCloudProvider) {
        this.name = name;
        this.applicationCloudProvider = applicationCloudProvider;
    }

    public String getName() {
        return name;
    }

    public ApplicationCloudProvider getApplicationCloudProvider() {
        return applicationCloudProvider;
    }

}
