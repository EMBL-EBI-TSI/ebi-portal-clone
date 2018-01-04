package uk.ac.ebi.tsc.portal.api.configuration.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {
    Collection<Configuration> findByAccountUsername(String username);
    Optional<Configuration> findByNameAndAccountUsername(String name, String username);
    Optional<Configuration> findByNameAndAccountEmail(String name, String email);
	Optional<Configuration> findByReference(String reference);
}