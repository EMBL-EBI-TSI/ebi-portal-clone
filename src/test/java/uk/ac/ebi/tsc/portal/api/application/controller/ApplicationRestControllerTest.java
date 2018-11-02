
package uk.ac.ebi.tsc.portal.api.application.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.tsc.aap.client.model.User;
import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;
import uk.ac.ebi.tsc.portal.api.account.service.AccountService;
import uk.ac.ebi.tsc.portal.api.application.repo.Application;
import uk.ac.ebi.tsc.portal.api.application.repo.ApplicationRepository;
import uk.ac.ebi.tsc.portal.api.application.service.ApplicationNotFoundException;
import uk.ac.ebi.tsc.portal.api.application.service.ApplicationService;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentApplication;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentApplicationRepository;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentApplicationService;
import uk.ac.ebi.tsc.portal.api.team.repo.Team;
import uk.ac.ebi.tsc.portal.api.team.repo.TeamRepository;
import uk.ac.ebi.tsc.portal.clouddeployment.application.ApplicationDownloader;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDownloaderException;
import uk.ac.ebi.tsc.portal.clouddeployment.model.ApplicationManifest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

import static org.hamcrest.core.Is.is;
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
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class ApplicationRestControllerTest {

	ApplicationRepository mockApplicationRepo = mock(ApplicationRepository.class);
	ApplicationDownloader mockApplicationDownloader = mock(ApplicationDownloader.class);
	AccountRepository mockAccountRepo = mock(AccountRepository.class);
	AccountService accountServiceMock = mock(AccountService.class);
	TeamRepository teamRepoMock = mock(TeamRepository.class);
	ApplicationManifest applicationManifestMock = mock(ApplicationManifest.class);
	ApplicationService applicationService = mock(ApplicationService.class);
	AccountService accountService = mock(AccountService.class);
	DomainService domainService = mock(DomainService.class);
	uk.ac.ebi.tsc.aap.client.security.TokenHandler tokenHandler = mock(uk.ac.ebi.tsc.aap.client.security.TokenHandler.class);
	HttpServletRequest request = mock(HttpServletRequest.class);
	DeploymentApplicationService deploymentApplicationService = mock(DeploymentApplicationService.class);
	DeploymentApplicationRepository deploymentApplicationRepo = mock(DeploymentApplicationRepository.class);
	Principal principalMock = mock(Principal.class);
	User user = mock(User.class);
	Account accountMock = mock(Account.class);
	ApplicationRestController subject;

	private static final String APPS_ROOT_FOLDER = "/a/path/has/no/name";
	private static final int CREATED_HTTP_STATUS = 201;
	private static final int OK_HTTP_STATUS = 200;
	String tokenArray = "some token";
	String token = "token" ;


	@Before
	public void setUp() {

		subject = new ApplicationRestController(mockApplicationRepo, mockAccountRepo, 
				mockApplicationDownloader, teamRepoMock, tokenHandler, domainService, deploymentApplicationRepo );
		Properties props = new Properties();
		props.put("be.applications.root", APPS_ROOT_FOLDER);
		subject.setProperties(props);
		when(this.principalMock.getName()).thenReturn("A user name");
		when(this.accountMock.getId()).thenReturn(1L);
		when(this.accountMock.getUsername()).thenReturn("A user name");
		when(this.accountMock.getEmail()).thenReturn("an@email.com");
		when(this.accountMock.getPassword()).thenReturn("A password");
		when(this.accountMock.getOrganisation()).thenReturn("An organisation");
		when(this.accountMock.getMemberOfTeams()).thenReturn((Set<Team>)mock(Set.class));

	}

	/**
	 * Test if user can list applications associated with his/her account 
	 * @throws IOException
	 * @throws ApplicationDownloaderException
	 */
	@Test public void
	can_list_all_application_for_account() throws IOException, ApplicationDownloaderException{

		when(principalMock.getName()).thenReturn("username");
		when(mockAccountRepo.findByUsername("username")).thenReturn(Optional.of(accountMock));
		when(accountMock.getApplications()).thenReturn((Set<Application>) mock(Set.class));

		Resources<ApplicationResource> resources = subject.getAllApplications(principalMock, new Sort("name.asc"));
		assertNotNull(resources);
	}

	/**
	 * Test if user can retrieve an application by his/her account user name and application name
	 * @throws IOException
	 * @throws ApplicationDownloaderException
	 */
	@Test public void
	can_get_application_by_account_name() throws IOException, ApplicationDownloaderException{

		String repoUri = "repoUri";
		String appName = "someName";

		Application application = mockApplication(repoUri, APPS_ROOT_FOLDER + File.separator + appName, appName);
		mockSavedApplication(application, repoUri);
		when(mockApplicationRepo.findByAccountUsernameAndName(this.accountMock.getUsername(), appName)).thenReturn(Optional.of(application));

		ApplicationResource applicationResource = subject.getApplicationByAccountUsernameAndName(principalMock, appName);
		assertNotNull(applicationResource);
	}

	/**
	 * Test if user can add an application to his/her own account using the repository URI
	 * @throws IOException
	 * @throws ApplicationDownloaderException
	 */
	@Test public void
	can_add_application_given_repo_uri() throws IOException, ApplicationDownloaderException {

		String theUri = "blah";
		String theName = "an-app-has-no-name";
		Application mockApplication = mockApplication(theUri, APPS_ROOT_FOLDER + File.separator + theName, theName);

		when(mockApplicationRepo.save(mockApplication)).thenReturn(mockApplication);
		ApplicationResource inputResource = new ApplicationResource(mockApplication);
		when(mockApplicationRepo.findByAccountUsernameAndName(this.accountMock.getUsername(), inputResource.getName())).thenReturn(Optional.empty());
		when(mockApplicationDownloader.downloadApplication(APPS_ROOT_FOLDER, inputResource.getRepoUri(), this.accountMock.getUsername())).thenReturn(mockApplication);

		// do the request
		ResponseEntity response = subject.add(principalMock, inputResource);

		// check assertions
		assertThat(response.getStatusCode().value(), is(CREATED_HTTP_STATUS));

	}

	/**
	 * Test if user can delete an application using application name, from his/her own account 
	 * @throws IOException
	 * @throws ApplicationDownloaderException
	 */
	@Test public void
	can_delete_application_given_repo_uri() throws IOException, ApplicationDownloaderException {

		String theUri = "blah";
		String theName = "an-app-has-no-name";

		Application application = mockApplication(theUri, APPS_ROOT_FOLDER + File.separator + theName, theName);
		mockSavedApplication(application, theUri);

		ResponseEntity response = subject.deleteApplicationByAccountUsernameAndName(principalMock, theName);

		assertThat(response.getStatusCode().value(), is(OK_HTTP_STATUS));
	}

	/**
	 * Test if user can list all shared applications associated with his/her own account
	 * @throws IOException
	 * @throws ApplicationDownloaderException
	 */
	@Test public void
	can_list_all_shared_application_for_account() throws IOException, ApplicationDownloaderException{

		String username = "username";
		ReflectionTestUtils.setField(subject, "applicationService", applicationService);
		ReflectionTestUtils.setField(subject, "accountService", accountService);

		when(accountService.findByUsername(username)).thenReturn(accountMock);
		when(mockAccountRepo.findByUsername(username)).thenReturn(Optional.of(accountMock));

		//user is a member of one team which has one application
		Set<Team> teams = new HashSet<>();
		Team team = mock(Team.class);
		teams.add(team);
		Set<Application> applications = new HashSet<>();
		Application applicationMock = mock(Application.class);
		applications.add(applicationMock);
		when(team.getApplicationsBelongingToTeam()).thenReturn(applications);
		Answer teamAnswer = new Answer<Set<Team>>(){
			@Override
			public Set<Team> answer(InvocationOnMock invocation){
				return teams;
			}
		};
		when(accountMock.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
		when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("some token");
		when(principalMock.getName()).thenReturn(username);
		getApplicationResource(applicationMock);
		Mockito.when(applicationService.getSharedApplicationsByAccount(
				any(Account.class),
				any(String.class),
				any(User.class))).thenReturn(applications);
		Resources<ApplicationResource> applicationList = subject.getSharedApplicationByAccount(request, principalMock);
		assertNotNull(applicationList);
		assertEquals(1, applicationList.getContent().size());

	}

	/**
	 * test that if user is not a member of any team
	 * @throws IOException
	 * @throws ApplicationDownloaderException
	 */
	@Test public void
	can_get_not_null_when_account_not_member_of_any_team() throws IOException, ApplicationDownloaderException{

		String username = "username";
		ReflectionTestUtils.setField(subject, "applicationService", applicationService);
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		when(accountService.findByUsername(username)).thenReturn(accountMock);
		when(mockAccountRepo.findByUsername(username)).thenReturn(Optional.of(accountMock));
		Set<Application> applications = new HashSet<>();
		Application applicationMock = mock(Application.class);
		applications.add(applicationMock);
		when(accountMock.getMemberOfTeams()).thenReturn(new HashSet<>()) ; 
		when(principalMock.getName()).thenReturn(username);
		getApplicationResource(applicationMock);
		when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("some token");
		Resources<ApplicationResource> applicationList = subject.getSharedApplicationByAccount(request, principalMock);
		assertNotNull(applicationList);
		assertEquals(0, applicationList.getContent().size());

	}

	/**
	 * test if an application is created from the manifest file
	 * @throws IOException
	 * @throws ApplicationDownloaderException
	 */
	@Test
	public void can_application_be_created_from_manifest() throws IOException, ApplicationDownloaderException{

		String applicationName = "App";
		String  deploymentParameterMock = "deploymentParameter";
		List<String> deploymentParameterCollection = new LinkedList<>();
		deploymentParameterCollection.add(deploymentParameterMock);
		applicationManifestMock.deploymentParameters = deploymentParameterCollection;
		applicationManifestMock.applicationName = applicationName;
		Application mockApplication = new ApplicationDownloader(accountServiceMock).fromManifestToApplication("repoUri", "path", accountMock, applicationManifestMock);
		assertNotNull(mockApplication);
		assertThat(mockApplication.getName(), is(applicationName));

	}

	/**
	 * test if user can see the applications he owns
	 */
	@Test
	public void can_user_see_owned_applications(){

		ReflectionTestUtils.setField(subject, "applicationService", applicationService);
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		String username = "username";
		Sort sortMock = mock(Sort.class);
		when(principalMock.getName()).thenReturn(username);
		when(accountMock.getUsername()).thenReturn(username);
		when(accountService.findByUsername(username)).thenReturn(accountMock);
		Application accountMockApp = mock(Application.class);
		Set<Application> accountApplications = new HashSet<>();
		accountApplications.add(accountMockApp);
		when(mockApplicationRepo.findByAccountUsername(username, sortMock)).thenReturn(accountApplications);
		when(applicationService.findByAccountUsername(username, sortMock)).thenReturn(accountApplications);
		getApplicationResource(accountMockApp);
		Resources<ApplicationResource> applicationList = subject.getAllApplications(principalMock, sortMock);
		assertNotNull(applicationList);
		assertEquals(1, applicationList.getContent().size());
	}

	/**
	 * test if user can see the applications he owns
	 */
	@Test
	public void can_get_not_null_when_account_has_no_owned_applications(){

		ReflectionTestUtils.setField(subject, "applicationService", applicationService);
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		String username = "username";

		when(principalMock.getName()).thenReturn(username);
		when(accountMock.getUsername()).thenReturn(username);
		when(accountService.findByUsername(username)).thenReturn(accountMock);
		when(mockApplicationRepo.findByAccountUsername(username, new Sort("name.asc"))).thenReturn(new HashSet<>());
		when(applicationService.findByAccountUsername(username, new Sort("sort.name"))).thenReturn(new HashSet<>());
		Resources<ApplicationResource> applicationList = subject.getAllApplications(principalMock, new Sort("name.asc"));
		assertNotNull(applicationList);
		assertEquals(0, applicationList.getContent().size());
	}

	@Test
	public void can_get_shared_applications_by_account(){

		ReflectionTestUtils.setField(subject, "applicationService", applicationService);
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		ReflectionTestUtils.setField(subject, "tokenHandler", tokenHandler);
		ReflectionTestUtils.setField(applicationService, "domainService", domainService);
		this.getRequest();
		Application application = mock(Application.class);
		getApplicationResource(application);
		Set<Application> applications = new HashSet<>();
		applications.add(application);
		when(applicationService.getSharedApplicationsByAccount(any(Account.class), Mockito.anyString(), any(User.class))).thenReturn(applications);
		Resources resources = subject.getSharedApplicationByAccount(request, principalMock);
		assertEquals(resources.getContent().size(), 1);
	}

	@Test
	public void can_get_shared_applications_by_applicationName_pass() throws IOException, ApplicationDownloaderException{

		ReflectionTestUtils.setField(subject, "applicationService", applicationService);
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		ReflectionTestUtils.setField(subject, "tokenHandler", tokenHandler);
		ReflectionTestUtils.setField(applicationService, "domainService", domainService);
		this.getRequest();
		String appName = "name";
		Application application = mockApplication("repoUri", "repoPath", appName);
		when(applicationService.findByAccountUsernameAndName(Mockito.anyString(), Mockito.anyString())).thenReturn(application);
		when(applicationService.getSharedApplicationByApplicationName(any(Account.class), Mockito.anyString(), any(User.class), Mockito.anyString())).thenReturn(application);
		ApplicationResource resources = subject.getSharedByName(request, principalMock, "application");
		assertTrue(resources.getId() != null);
	}

	@Test(expected = ApplicationNotFoundException.class)
	public void can_get_shared_applications_by_applicationName_fail() throws IOException, ApplicationDownloaderException{

		ReflectionTestUtils.setField(subject, "applicationService", applicationService);
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		ReflectionTestUtils.setField(subject, "tokenHandler", tokenHandler);
		ReflectionTestUtils.setField(applicationService, "domainService", domainService);
		this.getRequest();
		when(applicationService.findByAccountUsernameAndName(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
		when(applicationService.getSharedApplicationByApplicationName(any(Account.class), Mockito.anyString(), any(User.class), Mockito.anyString())).thenReturn(null);
		ApplicationResource resources = subject.getSharedByName(request, principalMock, "application");
	}
	
	@Test
	public void can_delete_application_repo_when_no_deployments() throws IOException, ApplicationDownloaderException{
		
		String repoPath = "APPS_ROOT_FOLDER + File.separator + theName";
		File file = new File(repoPath);
		file.createNewFile();
		assertTrue(file.exists()==true);
		
		String principalName = "principal";
		when(principalMock.getName()).thenReturn(principalName);
		
		String theUri = "blah";
		String theName = "an-app-has-no-name";

		Application application = mockApplication(theUri, repoPath, theName);
		mockSavedApplication(application, theUri);
		
		List<DeploymentApplication> depList = new ArrayList<>();
	
		Answer depListAnswer = new Answer<List<DeploymentApplication>>(){
			@Override
			public List<DeploymentApplication> answer(InvocationOnMock invocation){
				return depList;
			}
		};
	
		when(deploymentApplicationRepo.findByAccountIdAndRepoPath(application.getAccount().getId(), application.getRepoPath()))
		.thenAnswer(depListAnswer);
		
		when(deploymentApplicationService.findByAccountIdAndRepoPath(application.getAccount().getId(), application.getRepoPath()))
		.thenCallRealMethod();
		
		when(mockApplicationDownloader.removeApplication(application)).thenCallRealMethod();
		
		ResponseEntity appDeleted = subject.deleteApplicationByAccountUsernameAndName(principalMock, theName);
		
		assertTrue(file.exists()==false);
		assertTrue(appDeleted.getStatusCode() == HttpStatus.OK);
		file.delete();
	}
	
	@Test
	public void should_not_delete_application_repo_when_deployments_found() throws IOException, ApplicationDownloaderException{
		
		String repoPath = "APPS_ROOT_FOLDER + File.separator + theName";
		File file = new File(repoPath);
		file.createNewFile();
		assertTrue(file.exists()==true);
		
		String principalName = "principal";
		when(principalMock.getName()).thenReturn(principalName);
		
		String theUri = "blah";
		String theName = "an-app-has-no-name";

		Application application = mockApplication(theUri, repoPath, theName);
		mockSavedApplication(application, theUri);
		
		List<DeploymentApplication> depList = new ArrayList<>();
		DeploymentApplication depApp = mock(DeploymentApplication.class);
		depList.add(depApp);
		
		Answer depListAnswer = new Answer<List<DeploymentApplication>>(){
			@Override
			public List<DeploymentApplication> answer(InvocationOnMock invocation){
				return depList;
			}
		};
	
		when(deploymentApplicationRepo.findByAccountIdAndRepoPath(application.getAccount().getId(), application.getRepoPath()))
		.thenAnswer(depListAnswer);
		
		when(deploymentApplicationService.findByAccountIdAndRepoPath(application.getAccount().getId(), application.getRepoPath()))
		.thenCallRealMethod();
		
		when(mockApplicationDownloader.removeApplication(application)).thenCallRealMethod();
		
		ResponseEntity appDeleted = subject.deleteApplicationByAccountUsernameAndName(principalMock, theName);
		
		
		assertTrue(file.exists()==true);
		assertTrue(appDeleted.getStatusCode() == HttpStatus.OK);
		file.delete();
	}

	private void getApplicationResource(Application application){

		when(application.getAccount()).thenReturn(accountMock);
		when(application.getRepoUri()).thenReturn("repoUri");
	}

	private Application mockApplication(String repoUri, String repoPath, String name) throws IOException, ApplicationDownloaderException {

		Application mockApplication = mock(Application.class);
		when(mockApplication.getRepoPath()).thenReturn(repoPath);
		when(mockApplication.getName()).thenReturn(name);
		when(mockApplication.getRepoUri()).thenReturn(repoUri);
		when(mockApplication.getId()).thenReturn(1L);
		when(mockApplication.getAccount()).thenReturn(this.accountMock);
		when(accountMock.getId()).thenReturn(1L);

		return mockApplication;
	}

	private void mockSavedApplication(Application mockApplication, String repoUri) throws IOException, ApplicationDownloaderException {

		when(mockApplicationRepo.findByAccountUsernameAndName(this.principalMock.getName(), mockApplication.getName())).thenReturn(Optional.of(mockApplication));
		when(mockApplicationRepo.findById(1L)).thenReturn(Optional.of(mockApplication));
		when(mockApplicationDownloader.removeApplication(mockApplication)).thenReturn(0);
		when(mockApplicationDownloader.downloadApplication(APPS_ROOT_FOLDER, null, repoUri)).thenReturn(mockApplication);

	}

	private SharedApplicationResource mockSharedApplicationResource(String userEmail, String appName){

		SharedApplicationResource sharedApplicationResourceMock = mock(SharedApplicationResource.class);

		when(sharedApplicationResourceMock.getApplicationName()).thenReturn(appName);
		when(sharedApplicationResourceMock.getUserEmail()).thenReturn(userEmail);
		
		List<Account> accounts = new ArrayList<>();
		accounts.add(accountMock);
		given(mockAccountRepo.findByEmail(userEmail)).willReturn(accounts);
		when(accountMock.getApplications()).thenReturn((Set<Application>) mock(Set.class));
		when(mockAccountRepo.save(accountMock)).thenReturn(accountMock);

		return sharedApplicationResourceMock;

	}

	private void mockSavedApplicationNotToBeFound(Application mockApplication, String repoUri) throws IOException, ApplicationDownloaderException {
		when(mockApplicationRepo.findByAccountUsernameAndName(this.principalMock.getName(), mockApplication.getName())).thenThrow(new ApplicationNotFoundException("noapp"));
	}

	private void getRequest(){
		given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn(tokenArray);
	}

}
