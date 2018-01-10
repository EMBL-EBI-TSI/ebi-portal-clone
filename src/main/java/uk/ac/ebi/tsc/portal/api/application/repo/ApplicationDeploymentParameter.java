package uk.ac.ebi.tsc.portal.api.application.repo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Entity
public class ApplicationDeploymentParameter {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String name;

    @ManyToOne
    public Application application;

    ApplicationDeploymentParameter() { 
    }

    public ApplicationDeploymentParameter(String name, Application application) {
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
