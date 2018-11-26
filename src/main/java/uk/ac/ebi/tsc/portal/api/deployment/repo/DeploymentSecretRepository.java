package uk.ac.ebi.tsc.portal.api.deployment.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface DeploymentSecretRepository extends JpaRepository<DeploymentSecret, Long> {
    
    Optional<DeploymentSecret> findByDeploymentReferenceAndSecret(String reference, String secret);
}
