package uk.ac.ebi.tsc.portal.api.configuration.service;


import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ebi.tsc.aap.client.model.Domain;
import uk.ac.ebi.tsc.aap.client.model.User;
import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.application.controller.ApplicationRestController;
import uk.ac.ebi.tsc.portal.api.application.repo.Application;
import uk.ac.ebi.tsc.portal.api.configuration.controller.ConfigurationDeploymentParametersResource;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigDeploymentParamsCopy;
import uk.ac.ebi.tsc.portal.api.configuration.repo.Configuration;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParameters;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParametersRepository;
import uk.ac.ebi.tsc.portal.api.deployment.controller.DeploymentRestController;
import uk.ac.ebi.tsc.portal.api.deployment.repo.Deployment;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentConfiguration;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentStatusEnum;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentService;
import uk.ac.ebi.tsc.portal.api.team.repo.Team;
import uk.ac.ebi.tsc.portal.api.utils.SendMail;

public class ConfigurationDeploymentParametersService {

	private static final Logger logger = LoggerFactory.getLogger(ConfigurationDeploymentParametersService.class);

	private final ConfigurationDeploymentParametersRepository configurationDeploymentParametersRepository;
	private final DomainService domainService;

	@Autowired
	public ConfigurationDeploymentParametersService(ConfigurationDeploymentParametersRepository configurationDeploymentParametersRepository,
			DomainService domainService ) {
		this.configurationDeploymentParametersRepository = configurationDeploymentParametersRepository;
		this.domainService = domainService;
	}

	public ConfigurationDeploymentParameters findByNameAndAccountUserName(String name, String userName) {
		return this.configurationDeploymentParametersRepository.findByNameAndAccountUsername(name, userName).orElseThrow(
				() -> new ConfigurationDeploymentParametersNotFoundException(name));
	}
	
	public ConfigurationDeploymentParameters findById(Long id) {
		return this.configurationDeploymentParametersRepository.findById(id).orElseThrow(
				() -> new ConfigurationDeploymentParametersNotFoundException(id));
	}

	public ConfigurationDeploymentParameters findByName(String name) {
		return this.configurationDeploymentParametersRepository.findByName(name).stream().findFirst().orElseThrow(
				() -> new ConfigurationDeploymentParametersNotFoundException(name));
	}
	
	public ConfigurationDeploymentParameters findByReference(String reference) {
		return this.configurationDeploymentParametersRepository.findByReference(reference).orElseThrow(
				() -> new ConfigurationDeploymentParametersNotFoundException(reference));
	}

	public ConfigurationDeploymentParameters save(ConfigurationDeploymentParameters deploymentParameters) {
		return this.configurationDeploymentParametersRepository.save(deploymentParameters);
	}

	public Collection<ConfigurationDeploymentParameters> findAll() {
		return this.configurationDeploymentParametersRepository.findAll();
	}

	public void delete(ConfigurationDeploymentParameters deploymentParameters) {
		this.configurationDeploymentParametersRepository.delete(deploymentParameters);
	}

	public Collection<ConfigurationDeploymentParameters> findByAccountUsername(String username){
		return this.configurationDeploymentParametersRepository.findByAccountUsername(username);
	}

	public Set<ConfigurationDeploymentParameters> getSharedDeploymentParametersByAccount(Account account,
			String token, User user) {
		Set<ConfigurationDeploymentParameters> sharedDeploymentParameters = new HashSet<>();
		logger.info("In ConfigurationDeploymentParametersService: getting shared deployment parameters");

		for(Team memberTeam: account.getMemberOfTeams()){
			try{
				logger.info("In ConfigurationDeploymentParametersService: checking if team has a domain reference ");
				if(memberTeam.getDomainReference() != null){
					logger.info("In ConfigurationDeploymentParametersService: check if user is a domain member");
					Domain domain = domainService.getDomainByReference(memberTeam.getDomainReference(), token);
					if(domain != null){
						Collection<User> users = domainService.getAllUsersFromDomain(domain.getDomainReference(), token);
						User domainUser = users.stream().filter(u -> u.getEmail().equals(user.getEmail())).findAny().orElse(null);
						if(domainUser != null){
							logger.info("In ConfigurationDeploymentParametersService:  returning shared deployment parameters , if user is member of team and domain");
							sharedDeploymentParameters.addAll(memberTeam.getConfigDepParamsBelongingToTeam());
						}
					}
				}else{
					logger.info("In ConfigurationDeploymentParametersService:  returning shared deployment parameters , if user is member of team and no domain present");
					sharedDeploymentParameters.addAll(memberTeam.getConfigDepParamsBelongingToTeam());
				}
			}catch(Exception e){
				logger.error("In ConfigurationDeploymentParametersService: Could not add all shared deployment parameters from team " + memberTeam.getName());
			}
		}
		return sharedDeploymentParameters;
	}

	public ConfigurationDeploymentParameters getSharedApplicationByDeploymentParametersName(Account account,  String token, User user, String cdpName ){

		logger.info("In ConfigurationDeploymentParametersService:  getting shared configuration deployment parameters by name");
		for(Team memberOfTeam: account.getMemberOfTeams()){
			try{
				logger.info("In ConfigurationDeploymentParametersService: checking if team has a domain reference ");
				if(memberOfTeam.getDomainReference() != null){
					logger.info("In ConfigurationDeploymentParametersService:  check if user is a domain member");
					Domain domain = domainService.getDomainByReference(memberOfTeam.getDomainReference(), token);
					if(domain != null){
						Collection<User> users = domainService.getAllUsersFromDomain(domain.getDomainReference(), token);
						User domainUser = users.stream().filter(u -> u.getEmail().equals(user.getEmail())).findAny().orElse(null);
						if(domainUser != null){
							logger.info("In ConfigurationDeploymentParametersService:  returning the shared configuration deployment parameters, if user is member of team and domain");
							return memberOfTeam.getConfigDepParamsBelongingToTeam().stream().filter(cdp -> cdpName.equals(cdp.getName()))
									.findFirst().get();
						}
					}
				}else{
					logger.info("In ConfigurationDeploymentParametersService:  returning shared deployment parameters by name, if user is member of team and no domain present");
					return memberOfTeam.getConfigDepParamsBelongingToTeam().stream().filter(cdp -> cdpName.equals(cdp.getName()))
							.findFirst().get();
				}
			}catch(Exception e){
				logger.error("In ConfigurationDeploymentParametersService: Could not add shared shared configuration deployment parameters " + cdpName + e.getMessage() );
			}
		}
		return null;
	}
	
	

}
