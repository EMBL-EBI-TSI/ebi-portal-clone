package uk.ac.ebi.tsc.portal.api.configuration.repo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.tsc.portal.api.application.repo.Application;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Repository
public interface ConfigurationDeploymentParametersRepository extends JpaRepository<ConfigurationDeploymentParameters, Long>  {
	List<ConfigurationDeploymentParameters> findByName(String name);
	Optional<ConfigurationDeploymentParameters> findById(Long id);
	Optional<ConfigurationDeploymentParameters> findByNameAndAccountUsername(String name, String username);
	Collection<ConfigurationDeploymentParameters> findByAccountUsername(String username);
	Optional<ConfigurationDeploymentParameters> findByReference(String reference);
}
