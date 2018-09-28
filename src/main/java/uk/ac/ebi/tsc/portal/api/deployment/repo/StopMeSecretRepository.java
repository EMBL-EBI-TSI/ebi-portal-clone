package uk.ac.ebi.tsc.portal.api.deployment.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface StopMeSecretRepository extends JpaRepository<StopMeSecret, Long> {
    
}
