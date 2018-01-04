package uk.ac.ebi.tsc.portal.api.volumeinstance.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
public interface VolumeInstanceStatusRepository extends JpaRepository<VolumeInstanceStatus, Long> {
    Optional<VolumeInstanceStatus> findByVolumeInstanceId(Long deploymentId);
}
