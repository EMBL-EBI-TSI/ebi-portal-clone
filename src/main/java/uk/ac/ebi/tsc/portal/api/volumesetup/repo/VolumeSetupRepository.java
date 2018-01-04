package uk.ac.ebi.tsc.portal.api.volumesetup.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@Repository
public interface VolumeSetupRepository extends JpaRepository<VolumeSetup, Long> {
    Optional<VolumeSetup> findById(Long id);
//    Optional<VolumeSetup> findByRepoUri(String repoUri);
//    Optional<VolumeSetup> findByName(String name);
    Collection<VolumeSetup> findByAccountUsername(String username);
    Optional<VolumeSetup> findByAccountUsernameAndName(String username, String name);
}