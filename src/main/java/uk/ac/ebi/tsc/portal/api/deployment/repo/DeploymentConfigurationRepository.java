package uk.ac.ebi.tsc.portal.api.deployment.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeploymentConfigurationRepository extends JpaRepository<DeploymentConfiguration, Long> {
	Optional<DeploymentConfiguration> findByName(String name);
    Optional<DeploymentConfiguration> findByDeployment(Deployment deployment);
}
