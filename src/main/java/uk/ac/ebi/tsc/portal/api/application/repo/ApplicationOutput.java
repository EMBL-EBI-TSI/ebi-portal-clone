package uk.ac.ebi.tsc.portal.api.application.repo;

import javax.persistence.*;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@Entity
public class ApplicationOutput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String name;

    @ManyToOne
    public Application application;

    ApplicationOutput() { // jpa only
    }

    public ApplicationOutput(String name, Application application) {
        this.name = name;
        this.application = application;
    }

    public String getName() {
        return name;
    }

    public Application getApplication() {
        return application;
    }

}
