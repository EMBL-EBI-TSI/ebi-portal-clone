package uk.ac.ebi.tsc.portal.api.application.repo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;


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
