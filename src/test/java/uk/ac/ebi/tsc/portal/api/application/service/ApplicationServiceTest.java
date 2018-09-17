package uk.ac.ebi.tsc.portal.api.application.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ebi.tsc.aap.client.model.Domain;
import uk.ac.ebi.tsc.aap.client.model.User;
import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.application.repo.Application;
import uk.ac.ebi.tsc.portal.api.application.repo.ApplicationRepository;
import uk.ac.ebi.tsc.portal.api.team.controller.TeamResource;
import uk.ac.ebi.tsc.portal.api.team.repo.Team;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class ApplicationServiceTest {

	private ApplicationRepository applicationRepositoryMock;
	private Application applicationMock;
	private ApplicationService testCandidate;
	private DomainService domainService;
	private User user;
	private HttpServletRequest request;
	private TeamResource teamResource;
	private Account accountMock;
	private Principal principalMock;
	private Team team;
	private Domain domain;

	String tokenArray = "some token";
	String token = "token" ;
	String username = "username";
	String appName = "appName";
	String appnotpresent = "appnotpresent";
	String teamName = "teamName";
	String userEmail = "userEmail";
	
	@Before
	public void setUp(){
		
		applicationRepositoryMock = mock(ApplicationRepository.class);
		applicationMock = mock(Application.class);
		domainService = mock(DomainService.class);
		user = mock(User.class);
		request = mock(HttpServletRequest.class);
		testCandidate = new ApplicationService(applicationRepositoryMock, domainService);
		principalMock = mock(Principal.class);
		teamResource = mock(TeamResource.class);
		team = mock(Team.class);
		domain = mock(Domain.class);
		
		when(this.principalMock.getName()).thenReturn("A user name");
		when(user.getEmail()).thenReturn(userEmail);
		this.accountMock = mock(Account.class);
		when(this.accountMock.getId()).thenReturn(1L);
		when(this.accountMock.getUsername()).thenReturn("A user name");
		when(this.accountMock.getEmail()).thenReturn("an@email.com");
		when(this.accountMock.getPassword()).thenReturn("A password");
		when(this.accountMock.getOrganisation()).thenReturn("An organisation");
		
	}

	/**
	 * A user can have 1 or more applications.
	 * Test if this is the case.
	 */
	@Test
	public void testFindByAccountUsername(){
		mockApplications();
		Collection<Application> applications = testCandidate.findByAccountUsername(username, new Sort("name.asc"));
		assertEquals(applications.size(), 1);
	}

	/**
	 * A user can have only 1 application, with a unique application name.
	 * Test if this is the case
	 */
	@Test
	public void testFindByAccountUsernameAndName(){
		mockApplications();
		Application application = testCandidate.findByAccountUsernameAndName(username, appName);
		assertNotNull(application);
	}

	/**
	 * A user cannot have an application without a name
	 * Test if this is the case
	 */
	@Test(expected = NullPointerException.class)
	public void testFindByAccountUsernameAndNameWithNullAppname(){
		mockApplications();
		testCandidate.findByAccountUsernameAndName(username, null);
	}

	/**
	 * If an application does not exist, user should receive ApplicationNotFoundException
	 * 
	 */
	@Test(expected = ApplicationNotFoundException.class)
	public void testFindByAccountUsernameAndNameAppNotExists(){
		mockApplications();
		testCandidate.findByAccountUsernameAndName(username, appnotpresent);
	}

	/**
	 * when domain returning a list of users
	 */
	@Test
	public void testGetSharedApplicationByAccountPass(){
		
		getTeamResource(team);
		getDomain();
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		Set<Application> applications = new HashSet<>();
		when(applicationMock.getName()).thenReturn(appName);
		applications.add(applicationMock);
		team.setApplicationsBelongingToTeam(applications);
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
		Application application = testCandidate.getSharedApplicationByApplicationName(accountMock, token, user, appName);
		assertTrue(application.getName().equals(appName));
	}
	
	@Test
	public void testGetSharedApplicationByAccountNoDomainPass(){
		
		getTeamResourceNoDomain(team);
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		Set<Application> applications = new HashSet<>();
		when(applicationMock.getName()).thenReturn(appName);
		applications.add(applicationMock);
		team.setApplicationsBelongingToTeam(applications);
		accountMock.setMemberOfTeams(teams);
		when(accountMock.getMemberOfTeams()).thenReturn(teams);
		Answer teamAnswer = new Answer<Set<Team>>(){
			@Override
			public Set<Team> answer(InvocationOnMock invocation){
				return teams;
			}};
		when(accountMock.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
		Application application = testCandidate.getSharedApplicationByApplicationName(accountMock, token, user, appName);
		assertTrue(application.getName().equals(appName));
	}
	
	/**
	 * when domain returns a list of users
	 */
	@Test
	public void testGetSharedApplicationByNamePass(){
		
		getTeamResource(team);
		getDomain();
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		Set<Application> applications = new HashSet<>();
		when(applicationMock.getName()).thenReturn(appName);
		applications.add(applicationMock);
		team.setApplicationsBelongingToTeam(applications);
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
		Application application = testCandidate.getSharedApplicationByApplicationName(accountMock, token, user, appName);
		assertTrue(application.getName().equals(appName));
	}
	
	@Test
	public void testGetSharedApplicationByNameNoDomainPass(){
		
		getTeamResourceNoDomain(team);
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		Set<Application> applications = new HashSet<>();
		when(applicationMock.getName()).thenReturn(appName);
		applications.add(applicationMock);
		team.setApplicationsBelongingToTeam(applications);
		accountMock.setMemberOfTeams(teams);
		when(accountMock.getMemberOfTeams()).thenReturn(teams);
		Answer teamAnswer = new Answer<Set<Team>>(){
			@Override
			public Set<Team> answer(InvocationOnMock invocation){
				return teams;
			}};
		when(accountMock.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
		Application application = testCandidate.getSharedApplicationByApplicationName(accountMock, token, user, appName);
		assertTrue(application.getName().equals(appName));
	}
	
	/**
	 * domain not returning list of users, throwing exception
	 */
	@Test
	public void testGetSharedApplicationByAccountFail(){
		
		getTeamResource(team);
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		getDomain();
		Set<Application> applications = new HashSet<>();
		when(applicationMock.getName()).thenReturn(appName);
		applications.add(applicationMock);
		team.setApplicationsBelongingToTeam(applications);
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
		Set<Application>  a = team.getApplicationsBelongingToTeam();
		Application application = testCandidate.getSharedApplicationByApplicationName(accountMock, token, user, appName);
		assertTrue(application == null);
	}
	
	/**
	 * domain not returning list of users, throwing exception
	 */
	@Test
	public void testGetSharedApplicationByNameFail(){
		
		getTeamResource(team);
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		getDomain();
		Set<Application> applications = new HashSet<>();
		when(applicationMock.getName()).thenReturn(appName);
		applications.add(applicationMock);
		team.setApplicationsBelongingToTeam(applications);
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
		Set<Application>  a = team.getApplicationsBelongingToTeam();
		Application application = testCandidate.getSharedApplicationByApplicationName(accountMock, token, user, appName);
		assertTrue(application == null);
	}
	
	@Test
	public void testIsApplicationShared(){
		Set<Account> accounts = new HashSet<>();
		accounts.add(accountMock);
		given(team.getAccountsBelongingToTeam()).willReturn(accounts);
		getTeamResource(team);
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		given(applicationMock.getSharedWithTeams()).willReturn(teams);
		boolean isShared = testCandidate.isApplicationSharedWithAccount(team, accountMock, applicationMock);
		assertTrue(isShared);
	}

	private void mockApplications(){
		Collection applications = new LinkedList<>();
		applications.add(applicationMock);
		when(applicationMock.getName()).thenReturn(appName);when(applicationRepositoryMock.findByAccountUsername(username, new Sort("name.asc"))).thenReturn(applications);
		when(applicationRepositoryMock.findByAccountUsernameAndName(username, appName)).thenReturn(Optional.of(applicationMock));
		when(applicationRepositoryMock.findByAccountUsernameAndName(username, appnotpresent)).thenThrow(ApplicationNotFoundException.class);
	}

	private void getRequest(){
		given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn(tokenArray);
	}

	private void getTeamResource(Team team){

		given(team.getAccount()).willReturn(accountMock);
		given(team.getName()).willReturn(teamName);
		given(teamResource.getName()).willReturn(teamName);
		given(team.getDomainReference()).willReturn("a reference");
		Set<Application> applications = new HashSet<>();
		applications.add(applicationMock);
		team.setApplicationsBelongingToTeam(applications);
		given(team.getApplicationsBelongingToTeam()).willReturn(applications);
	}
	
	private void getTeamResourceNoDomain(Team team){

		given(team.getAccount()).willReturn(accountMock);
		given(team.getName()).willReturn(teamName);
		given(teamResource.getName()).willReturn(teamName);
		given(team.getDomainReference()).willReturn(null);
		Set<Application> applications = new HashSet<>();
		applications.add(applicationMock);
		team.setApplicationsBelongingToTeam(applications);
		given(team.getApplicationsBelongingToTeam()).willReturn(applications);
	}
	
	private void getDomain(){
		String domainReference = "a domain reference";
		given(domain.getDomainReference()).willReturn(domainReference);
		given(domainService.createDomain("TEAM_"+team.getName().toUpperCase()+"_PORTAL", "Domain TEAM_"+team.getName()+"_PORTAL"+" created", token)).willReturn(domain);
		Mockito.when(domainService.getDomainByReference(Mockito.anyString(), Mockito.anyString())).thenReturn(domain);
	}

}
