package uk.ac.ebi.tsc.portal.api.configuration.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Repository
public interface ConfigDeploymentParamCopyRepository extends JpaRepository<ConfigDeploymentParamCopy, Long>  {
}
