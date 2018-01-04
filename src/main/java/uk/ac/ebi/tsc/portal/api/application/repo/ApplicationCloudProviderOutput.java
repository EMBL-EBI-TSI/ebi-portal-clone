package uk.ac.ebi.tsc.portal.api.application.repo;

import javax.persistence.*;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@Entity
public class ApplicationCloudProviderOutput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String name;

    @ManyToOne
    public ApplicationCloudProvider applicationCloudProvider;

    ApplicationCloudProviderOutput() { // jpa only
    }

    public ApplicationCloudProviderOutput(String name, ApplicationCloudProvider applicationCloudProvider) {
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
