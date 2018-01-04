package uk.ac.ebi.tsc.portal.api.configuration.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigDeploymentParamCopyRepository extends JpaRepository<ConfigDeploymentParamCopy, Long>  {
}
