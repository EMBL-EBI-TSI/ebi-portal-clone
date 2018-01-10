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
public class DeploymentGeneratedOutput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String value;

    @ManyToOne
    private Deployment deployment;

    private String outputName;


    DeploymentGeneratedOutput() { // jpa only
    }

    public DeploymentGeneratedOutput(String outputName, String value, Deployment deployment) {
        this.value = value;
        this.deployment = deployment;
        this.outputName = outputName;
    }

    public Long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }
}
