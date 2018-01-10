package uk.ac.ebi.tsc.portal.api.deployment.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public interface DeploymentConfigurationRepository extends JpaRepository<DeploymentConfiguration, Long> {
	Optional<DeploymentConfiguration> findByName(String name);
    Optional<DeploymentConfiguration> findByDeployment(Deployment deployment);
}
