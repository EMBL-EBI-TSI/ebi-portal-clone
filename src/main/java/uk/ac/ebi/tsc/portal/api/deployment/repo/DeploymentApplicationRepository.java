package uk.ac.ebi.tsc.portal.api.deployment.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public interface DeploymentApplicationRepository extends JpaRepository<DeploymentApplication, Long> {
	List<DeploymentApplication> findByAccountIdAndRepoPath(Long accountId, String repoPath);
}
