package uk.ac.ebi.tsc.portal.api.deployment.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeploymentGeneratedOutputRepository extends JpaRepository<DeploymentGeneratedOutput,Long> {
    Optional<DeploymentGeneratedOutput> findByDeploymentReferenceAndOutputName(String reference, String key);
}
