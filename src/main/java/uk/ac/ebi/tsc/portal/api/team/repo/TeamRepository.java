package uk.ac.ebi.tsc.portal.api.team.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long>{
 
	Optional<Team> findByName(String name);
	Collection<Team> findByAccountUsername(String accountUsername);
    Optional<Team> findByNameAndAccountUsername(String teamName, String name);
    Optional<Team> findByDomainReference(String domainReference);
}
