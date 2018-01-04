package uk.ac.ebi.tsc.portal.api.application.repo;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@Entity
public class ApplicationCloudProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    public String name;

    public String path;

    @ManyToOne
    public Application application;

    @OneToMany(mappedBy = "applicationCloudProvider", cascade = CascadeType.ALL)
    public Collection<ApplicationCloudProviderInput> inputs;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "applicationCloudProvider", cascade = CascadeType.ALL)
    public Collection<ApplicationCloudProviderOutput> outputs;

    @OneToMany(mappedBy = "applicationCloudProvider", cascade = CascadeType.ALL)
    public Collection<ApplicationCloudProviderVolume> volumes;

    ApplicationCloudProvider() { // jpa only
    }

    public ApplicationCloudProvider(String name, String path, Application application) {
        this.name = name;
        this.path = path;
        this.application = application;
        this.inputs = new LinkedList<>();
        this.outputs = new LinkedList<>();
        this.volumes = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Application getApplication() {
        return application;
    }

    public Collection<ApplicationCloudProviderInput> getInputs() {
        return inputs;
    }

    public Collection<ApplicationCloudProviderOutput> getOutputs() {
        return outputs;
    }

    public Collection<ApplicationCloudProviderVolume> getVolumes() {
        return volumes;
    }
}
