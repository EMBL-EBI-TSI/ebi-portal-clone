package uk.ac.ebi.tsc.portal.api.volumeinstance.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public interface VolumeInstanceRepository extends JpaRepository<VolumeInstance, Long> {
    Collection<VolumeInstance> findByAccountUsername(String username);
    Optional<VolumeInstance> findByAccountUsernameAndId(String username, Long id);
    Optional<VolumeInstance> findByReference(String reference);
}
