package uk.ac.ebi.tsc.portal.api.application.repo;

import javax.persistence.*;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@Entity
public class ApplicationInput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String name;

    @ManyToOne
    public Application application;

    ApplicationInput() { // jpa only
    }

    public ApplicationInput(String name, Application application) {
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
