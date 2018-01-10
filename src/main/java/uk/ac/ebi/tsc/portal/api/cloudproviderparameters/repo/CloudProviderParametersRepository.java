package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Repository
public interface CloudProviderParametersRepository extends JpaRepository<CloudProviderParameters, Long> {
    Collection<CloudProviderParameters> findByAccountUsername(String username);
    Collection<CloudProviderParameters> findByCloudProviderAndAccountUsername(String cloudProvider, String username);
    Optional<CloudProviderParameters> findById(Long cloudCredentialsId);
    Optional<CloudProviderParameters> findByNameAndAccountUsername(String name, String username);
    List<CloudProviderParameters> findByName(String name);
	Optional<CloudProviderParameters> findByReference(String reference);
    
}