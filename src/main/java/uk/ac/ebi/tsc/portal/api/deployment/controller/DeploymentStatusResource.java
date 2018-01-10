package uk.ac.ebi.tsc.portal.api.deployment.controller;

import org.springframework.hateoas.ResourceSupport;
import uk.ac.ebi.tsc.portal.api.deployment.repo.Deployment;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentStatus;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentStatusEnum;
import uk.ac.ebi.tsc.portal.usage.deployment.model.DeploymentDocument;

import java.sql.Timestamp;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
class DeploymentStatusResource extends ResourceSupport {

    private final DeploymentStatusEnum status;
    public java.sql.Timestamp startedTime;
    public java.sql.Timestamp deployedTime;
    public java.sql.Timestamp  failedTime;
    public java.sql.Timestamp  destroyedTime;
    public long totalRunningTime;
    public long instanceCount;
    public double totalDiskGb;
    public double totalRamGb;
    public double totalVcpus;
    public String errorCause;

    public DeploymentStatusResource(DeploymentStatus deploymentStatus, Deployment deployment,
                                    DeploymentDocument deploymentDocument) {

        this.status = deploymentStatus.getStatus();
        this.startedTime = deployment.getStartTime();
        this.deployedTime = deployment.getDeployedTime();
        this.failedTime = deployment.getFailedTime();
        this.destroyedTime = deployment.getDestroyedTime();
        if (deploymentDocument != null) {
            this.totalRunningTime = deploymentDocument.getTotalRunningTime();
            this.instanceCount = deploymentDocument.getInstanceCount();
            this.totalDiskGb = deploymentDocument.getTotalDiskGb();
            this.totalRamGb = deploymentDocument.getTotalRamGb();
            this.totalVcpus = deploymentDocument.getTotalVcpus();
            this.errorCause = deploymentDocument.getErrorCause();
        }

        this.add(
            linkTo(DeploymentRestController.class)
                .slash(deploymentStatus.deployment.getReference()
            ).withRel("deployment")
        );

    }

    public DeploymentStatusEnum getStatus() {
        return status;
    }

    public Timestamp getStartedTime() {
        return startedTime;
    }

    public Timestamp getDeployedTime() {
        return deployedTime;
    }

    public Timestamp getFailedTime() {
        return failedTime;
    }

    public Timestamp getDestroyedTime() {
        return destroyedTime;
    }

    public long getTotalRunningTime() {
        return totalRunningTime;
    }

    public long getInstanceCount() {
        return instanceCount;
    }

    public double getTotalDiskGb() {
        return totalDiskGb;
    }

    public double getTotalRamGb() {
        return totalRamGb;
    }

    public double getTotalVcpus() {
        return totalVcpus;
    }

    public String getErrorCause() {
        return errorCause;
    }
}
