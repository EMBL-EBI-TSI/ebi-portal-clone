package uk.ac.ebi.tsc.portal.api.configuration.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import uk.ac.ebi.tsc.aap.client.model.Domain;
import uk.ac.ebi.tsc.aap.client.model.User;
import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.application.repo.Application;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParameters;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParametersRepository;
import uk.ac.ebi.tsc.portal.api.team.controller.TeamResource;
import uk.ac.ebi.tsc.portal.api.team.repo.Team;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class ConfigurationDeploymentParametersServiceTest {
	
	@MockBean
	private ConfigurationDeploymentParametersService cdpService;

	@MockBean
	private ConfigurationDeploymentParametersRepository cdpRepository;

	@MockBean
	private ConfigurationDeploymentParameters cdp;
	
	@MockBean
	private Team team;

	@MockBean
	private TeamResource teamResource ;
	
	@MockBean
	Domain domain;
	
	
	private DomainService domainService;
	private User user;
	private Account accountMock;
	private Principal principalMock;

	String tokenArray = "some token";
	String token = "token" ;
	String userEmail = "userEmail";
	String cdpName = "cdpName";
	String teamName = "teamName";
	
	@Before
	public void setUp(){
		
		cdpRepository = mock(ConfigurationDeploymentParametersRepository.class);
		domainService = mock(DomainService.class);
		user = mock(User.class);
		accountMock = mock(Account.class);
		principalMock = mock(Principal.class);
		when(this.principalMock.getName()).thenReturn("A user name");
		when(user.getEmail()).thenReturn(userEmail);
		when(this.accountMock.getId()).thenReturn(1L);
		when(this.accountMock.getUsername()).thenReturn("A user name");
		when(this.accountMock.getEmail()).thenReturn("an@email.com");
		when(this.accountMock.getPassword()).thenReturn("A password");
		when(this.accountMock.getOrganisation()).thenReturn("An organisation");
		ReflectionTestUtils.setField(cdpService, "configurationDeploymentParametersRepository", cdpRepository);
		ReflectionTestUtils.setField(cdpService, "domainService", domainService);
	}
	
	/**
	 * test if service returns a deploymentparameter
	 * by its name
	 */
	@Test
	public void testFindByName(){
		List<ConfigurationDeploymentParameters> listCdp = new ArrayList<>();
		listCdp.add(cdp);
		String cdpName = "cdpName";
		given(cdpRepository.findByName(cdpName)).willReturn(listCdp);
		given(cdpService.findByName(cdpName)).willCallRealMethod();
		assertEquals(cdpService.findByName(cdpName), cdp);
	}
	
	/**
	 * test if service throws 'ConfigurationDeploymentParametersNotFoundException'
	 * if the deployment parameter is not found
	 */
	@Test(expected = ConfigurationDeploymentParametersNotFoundException.class)
	public void testFindByNameThrowsException(){
		String cdpName = "cdpName";
		given(cdpRepository.findByName(cdpName)).willThrow(ConfigurationDeploymentParametersNotFoundException.class);
		given(cdpService.findByName(cdpName)).willCallRealMethod();
		assertEquals(cdpService.findByName(cdpName), cdp);
	}
	
	/**
	 * test if service saves the deployment parameters
	 */
	@Test
	public void testFindAll(){
		List<ConfigurationDeploymentParameters> cdpSet = new ArrayList<>();
		cdpSet.add(cdp);
		given(cdpRepository.findAll()).willReturn(cdpSet);
		given(cdpService.findAll()).willCallRealMethod();
		assertEquals(cdpService.findAll(), cdpSet);
	}
	
	/**
	 * test if service saves the deployment parameter
	 */
	@Test
	public void testSave(){
		given(cdpRepository.save(cdp)).willReturn(cdp);
		given(cdpService.save(cdp)).willCallRealMethod();
		assertEquals(cdpService.save(cdp), cdp );
	}
	
	@Test
	public void testGetSharedDeploymentParametersByAccountPass(){
		
		getTeamResource(team);
		getDomain();
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		Set<ConfigurationDeploymentParameters> cdps = new HashSet<>();
		when(cdp.getName()).thenReturn(cdpName);
		cdps.add(cdp);
		team.setConfigDepParamsBelongingToTeam(cdps);
		accountMock.setMemberOfTeams(teams);
		when(accountMock.getMemberOfTeams()).thenReturn(teams);
		List<User> users = new ArrayList<>();
		users.add(user);
		when(domainService.getAllUsersFromDomain(Mockito.anyString(), Mockito.anyString())).thenReturn(users);
		Answer teamAnswer = new Answer<Set<Team>>(){
			@Override
			public Set<Team> answer(InvocationOnMock invocation){
				return teams;
		}};
		when(accountMock.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
		when(cdpService.getSharedDeploymentParametersByAccount(accountMock, token, user)).thenCallRealMethod();
		Set<ConfigurationDeploymentParameters> cdpsReturned = cdpService.getSharedDeploymentParametersByAccount(accountMock, token, user);
		assertTrue(cdpsReturned.size() == 1);
	}
	
	@Test
	public void testGetSharedDeploymentParametersByAccountNoDomainPass(){
		
		getTeamResourceNoDomain(team);
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		Set<ConfigurationDeploymentParameters> cdps = new HashSet<>();
		when(cdp.getName()).thenReturn(cdpName);
		cdps.add(cdp);
		team.setConfigDepParamsBelongingToTeam(cdps);
		accountMock.setMemberOfTeams(teams);
		when(accountMock.getMemberOfTeams()).thenReturn(teams);
		Answer teamAnswer = new Answer<Set<Team>>(){
			@Override
			public Set<Team> answer(InvocationOnMock invocation){
				return teams;
		}};
		when(accountMock.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
		when(cdpService.getSharedDeploymentParametersByAccount(accountMock, token, user)).thenCallRealMethod();
		Set<ConfigurationDeploymentParameters> cdpsReturned = cdpService.getSharedDeploymentParametersByAccount(accountMock, token, user);
		assertTrue(cdpsReturned.size() == 1);
	}
	
	@Test
	public void testGetSharedDeploymentParametersByDeploymentParameterNamePass(){
		
		getTeamResource(team);
		getDomain();
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		Set<ConfigurationDeploymentParameters> cdps = new HashSet<>();
		when(cdp.getName()).thenReturn(cdpName);
		cdps.add(cdp);
		team.setConfigDepParamsBelongingToTeam(cdps);
		accountMock.setMemberOfTeams(teams);
		when(accountMock.getMemberOfTeams()).thenReturn(teams);
		List<User> users = new ArrayList<>();
		users.add(user);
		when(domainService.getAllUsersFromDomain(Mockito.anyString(), Mockito.anyString())).thenReturn(users);
		Answer teamAnswer = new Answer<Set<Team>>(){
			@Override
			public Set<Team> answer(InvocationOnMock invocation){
				return teams;
		}};
		when(accountMock.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
		when(cdpService.getSharedApplicationByDeploymentParametersName(accountMock, token, user, cdpName)).thenCallRealMethod();
		ConfigurationDeploymentParameters cdpsReturned = cdpService.getSharedApplicationByDeploymentParametersName(accountMock, token, user, cdpName);
		assertTrue(cdpsReturned.getName().equals(cdpName));
	}
	
	@Test
	public void testGetSharedDeploymentParametersByDeploymentParameterNameNoDomainPass(){
		
		getTeamResourceNoDomain(team);
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		Set<ConfigurationDeploymentParameters> cdps = new HashSet<>();
		when(cdp.getName()).thenReturn(cdpName);
		cdps.add(cdp);
		team.setConfigDepParamsBelongingToTeam(cdps);
		accountMock.setMemberOfTeams(teams);
		when(accountMock.getMemberOfTeams()).thenReturn(teams);
		Answer teamAnswer = new Answer<Set<Team>>(){
			@Override
			public Set<Team> answer(InvocationOnMock invocation){
				return teams;
		}};
		when(accountMock.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
		when(cdpService.getSharedApplicationByDeploymentParametersName(accountMock, token, user, cdpName)).thenCallRealMethod();
		ConfigurationDeploymentParameters cdpsReturned = cdpService.getSharedApplicationByDeploymentParametersName(accountMock, token, user, cdpName);
		assertTrue(cdpsReturned.getName().equals(cdpName));
	}
	
	@Test
	public void testGetSharedDeploymentParametersByDeploymentParameterNameReturnsNull(){
		
		getTeamResource(team);
		getDomain();
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		Set<ConfigurationDeploymentParameters> cdps = new HashSet<>();
		when(cdp.getName()).thenReturn(cdpName);
		cdps.add(cdp);
		team.setConfigDepParamsBelongingToTeam(cdps);
		accountMock.setMemberOfTeams(teams);
		when(accountMock.getMemberOfTeams()).thenReturn(teams);
		List<User> users = new ArrayList<>();
		users.add(user);
		when(domainService.getAllUsersFromDomain(Mockito.anyString(), Mockito.anyString())).thenThrow(Exception.class);
		Answer teamAnswer = new Answer<Set<Team>>(){
			@Override
			public Set<Team> answer(InvocationOnMock invocation){
				return teams;
		}};
		when(accountMock.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
		when(cdpService.getSharedApplicationByDeploymentParametersName(accountMock, token, user, cdpName)).thenCallRealMethod();
		ConfigurationDeploymentParameters cdpsReturned = cdpService.getSharedApplicationByDeploymentParametersName(accountMock, token, user, cdpName);
		assertTrue(cdpsReturned == null);
	}
	@Test
	public void testGetSharedDeploymentParametersByAccountReturnsNull(){
		
		getTeamResource(team);
		getDomain();
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		Set<ConfigurationDeploymentParameters> cdps = new HashSet<>();
		when(cdp.getName()).thenReturn(cdpName);
		cdps.add(cdp);
		team.setConfigDepParamsBelongingToTeam(cdps);
		accountMock.setMemberOfTeams(teams);
		when(accountMock.getMemberOfTeams()).thenReturn(teams);
		List<User> users = new ArrayList<>();
		users.add(user);
		when(domainService.getAllUsersFromDomain(Mockito.anyString(), Mockito.anyString())).thenThrow(Exception.class);
		Answer teamAnswer = new Answer<Set<Team>>(){
			@Override
			public Set<Team> answer(InvocationOnMock invocation){
				return teams;
		}};
		when(accountMock.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
		when(cdpService.getSharedDeploymentParametersByAccount(accountMock, token, user)).thenCallRealMethod();
		Set<ConfigurationDeploymentParameters> cdpsReturned = cdpService.getSharedDeploymentParametersByAccount(accountMock, token, user);
		assertTrue(cdpsReturned.size() == 0);
	}
	
	private void getTeamResource(Team team){

		given(team.getAccount()).willReturn(accountMock);
		given(team.getName()).willReturn(teamName);
		given(teamResource.getName()).willReturn(teamName);
		given(team.getDomainReference()).willReturn("a reference");
		Set<ConfigurationDeploymentParameters> deploymentParameters = new HashSet<>();
		deploymentParameters.add(cdp);
		team.setConfigDepParamsBelongingToTeam(deploymentParameters);
		given(team.getConfigDepParamsBelongingToTeam()).willReturn(deploymentParameters);
	}
	
	private void getTeamResourceNoDomain(Team team){

		given(team.getAccount()).willReturn(accountMock);
		given(team.getName()).willReturn(teamName);
		given(teamResource.getName()).willReturn(teamName);
		given(team.getDomainReference()).willReturn(null);
		Set<ConfigurationDeploymentParameters> deploymentParameters = new HashSet<>();
		deploymentParameters.add(cdp);
		team.setConfigDepParamsBelongingToTeam(deploymentParameters);
		given(team.getConfigDepParamsBelongingToTeam()).willReturn(deploymentParameters);
	}
	
	private void getDomain(){
		String domainReference = "a domain reference";
		given(domain.getDomainReference()).willReturn(domainReference);
		given(domainService.createDomain("TEAM_"+team.getName().toUpperCase()+"_PORTAL", "Domain TEAM_"+team.getName()+"_PORTAL"+" created", token)).willReturn(domain);
		Mockito.when(domainService.getDomainByReference(Mockito.anyString(), Mockito.anyString())).thenReturn(domain);
	}
}
