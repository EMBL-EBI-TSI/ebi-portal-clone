package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CloudProviderParamsCopyRepository extends JpaRepository<CloudProviderParamsCopy, Long>{
	//find by name alone would not give you the right record, use name and reference combination.
	//reference is unique to each cloud provider params copy
    Collection<CloudProviderParamsCopy> findByAccountUsername(String username);
    Optional<CloudProviderParamsCopy> findById(Long cloudCredentialsId);
	Optional<CloudProviderParamsCopy> findByCloudProviderParametersReference(String reference);
}
