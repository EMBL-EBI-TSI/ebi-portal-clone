package uk.ac.ebi.tsc.portal.api.deployment.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
public interface DeploymentStatusRepository extends JpaRepository<DeploymentStatus, Long> {
    Optional<DeploymentStatus> findByDeploymentId(Long deploymentId);
}
