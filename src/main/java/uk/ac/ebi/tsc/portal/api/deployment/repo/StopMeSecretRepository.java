package uk.ac.ebi.tsc.portal.api.deployment.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface StopMeSecretRepository extends JpaRepository<StopMeSecret, Long> {
    
    Optional<StopMeSecret> findByDeploymentIdAndSecret(long deploymentId, String secret);
}
