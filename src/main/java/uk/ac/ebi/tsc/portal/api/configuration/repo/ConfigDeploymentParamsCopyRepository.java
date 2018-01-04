package uk.ac.ebi.tsc.portal.api.configuration.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigDeploymentParamsCopyRepository extends JpaRepository<ConfigDeploymentParamsCopy, Long>  {
	Optional<ConfigDeploymentParamsCopy> findByConfigurationDeploymentParametersReference(String reference);
	List<ConfigDeploymentParamsCopy> findByNameAndAccountUsername(String name, String username);
	List<ConfigDeploymentParamsCopy> findByName(String name);
}
