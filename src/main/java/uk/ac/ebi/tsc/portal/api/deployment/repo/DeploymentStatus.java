package uk.ac.ebi.tsc.portal.api.deployment.repo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Entity
public class DeploymentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private DeploymentStatusEnum status;

    @OneToOne
    public Deployment deployment;


    DeploymentStatus() { // jpa only
    }

    public DeploymentStatus(Deployment deployment, DeploymentStatusEnum status) {
        this.deployment = deployment;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public DeploymentStatusEnum getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatusEnum status) {
        this.status = status;
    }

    public Deployment getDeployment() {
        return deployment;
    }

}
