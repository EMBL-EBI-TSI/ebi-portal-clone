package uk.ac.ebi.tsc.portal.api.deployment.controller;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.isA;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.sql.Date;
import java.util.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import uk.ac.ebi.tsc.aap.client.repo.DomainRepository;
import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;
import uk.ac.ebi.tsc.portal.api.account.service.AccountService;
import uk.ac.ebi.tsc.portal.api.application.controller.InvalidApplicationInputException;
import uk.ac.ebi.tsc.portal.api.application.repo.Application;
import uk.ac.ebi.tsc.portal.api.application.repo.ApplicationCloudProvider;
import uk.ac.ebi.tsc.portal.api.application.repo.ApplicationRepository;
import uk.ac.ebi.tsc.portal.api.application.service.ApplicationNotFoundException;
import uk.ac.ebi.tsc.portal.api.application.service.ApplicationService;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParametersRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopy;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParametersService;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParamsCopyService;
import uk.ac.ebi.tsc.portal.api.configuration.controller.InvalidConfigurationInputException;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigDeploymentParamsCopy;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigDeploymentParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.configuration.repo.Configuration;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParameters;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParametersRepository;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationRepository;
import uk.ac.ebi.tsc.portal.api.configuration.service.ConfigDeploymentParamsCopyService;
import uk.ac.ebi.tsc.portal.api.configuration.service.ConfigurationDeploymentParametersService;
import uk.ac.ebi.tsc.portal.api.configuration.service.ConfigurationNotFoundException;
import uk.ac.ebi.tsc.portal.api.configuration.service.ConfigurationService;
import uk.ac.ebi.tsc.portal.api.deployment.repo.*;
import uk.ac.ebi.tsc.portal.api.deployment.service.*;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;
import uk.ac.ebi.tsc.portal.api.team.repo.Team;
import uk.ac.ebi.tsc.portal.api.team.repo.TeamRepository;
import uk.ac.ebi.tsc.portal.api.team.service.TeamService;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceRepository;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceStatusRepository;
import uk.ac.ebi.tsc.portal.clouddeployment.application.ApplicationDeployerBash;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentSecretService;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDeployerException;
import uk.ac.ebi.tsc.portal.usage.deployment.service.DeploymentIndexService;
import uk.ac.ebi.tsc.portal.usage.tracker.DeploymentStatusTracker;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
public class DeploymentRestControllerTest {

	private static final String A_CLOUD_PROVIDER_PARAMS_NAME = "OS TEST";
	public final String A_USER_NAME = "A User Name";
	public final String A_CLOUD_PROVIDER = "OSTACK";
	String salt= "salt";
	String password= "password";


	@MockBean
	private DeploymentRepository deploymentRepository;

	@MockBean
	private DeploymentService deploymentService;

	@MockBean
	private ConfigurationService configurationService;

	@MockBean
	private ConfigurationRepository configurationRepository;

	@MockBean
	private ConfigurationDeploymentParametersService configurationDeploymentParametersService;

	@MockBean
	private ConfigurationDeploymentParametersRepository configurationDeploymentParametersRepository;

	@MockBean
	private TeamService teamService;

	@MockBean
	private TeamRepository teamRepository;

	@MockBean
	DomainService domainService;

	@MockBean
	DomainRepository domainRepository;

	@MockBean
	DeploymentConfigurationService deploymentConfigurationService;

	@MockBean
	DeploymentConfigurationRepository deploymentConfigurationRepository;

	@MockBean
	DeploymentApplicationRepository deploymentApplicationRepository;

	@MockBean
	DeploymentApplicationService deploymentApplicationService;

	@MockBean
	DeploymentGeneratedOutputService deploymentGeneratedOutputService;

	DeploymentRestController subject;

	@MockBean
	Principal principal;

	String tempKey = "dGhlcG9ydGFsZGV2ZWxvcGVkYnl0c2lpc2F3ZXNvbWU=";

	@MockBean
	CloudProviderParamsCopyRepository cloudProviderParametersCopyRepository;

	@MockBean
	CloudProviderParamsCopyService cloudProviderParametersCopyService;

	@MockBean
	CloudProviderParametersService cloudProviderParametersService;

	@MockBean
	CloudProviderParametersRepository cloudProviderParametersRepository;

	@MockBean
	ConfigDeploymentParamsCopyService configurationDeploymentParamsCopyService;

	@MockBean
	ConfigDeploymentParamsCopyRepository configurationDeploymentParamsCopyRepository;

	@MockBean
	DeploymentConfiguration deploymentConfiguration;

	@MockBean
	CloudProviderParameters cloudProviderParameters;

	@MockBean
	AccountService accountService;

	@MockBean
	AccountRepository accountRepository;

	@MockBean
	ApplicationRepository applicationRepository;

	@MockBean
	ApplicationService applicationService;

	@MockBean
	DeploymentStatusRepository deploymentStatusRepository;

	@MockBean
	VolumeInstanceRepository volumeInstanceRepositoryRepository;

	@MockBean
	VolumeInstanceStatusRepository volumeInstanceStatusRepository;

	@MockBean
	ApplicationDeployerBash applicationDeployerBash;

	@MockBean
	DeploymentStatusTracker deploymentStatusTracker;

	@MockBean
	DeploymentIndexService deploymentIndexService;

	@MockBean
	EncryptionService encryptionService;
	
	@MockBean
	DeploymentSecretService deploymentSecretService;

	String cppReference = "cppReference";
	@Before 
	public void setUp() {
		subject = new DeploymentRestController(
				deploymentRepository, 
				deploymentStatusRepository,
				accountRepository,
				applicationRepository, 
				volumeInstanceRepositoryRepository, 
				volumeInstanceStatusRepository,
				cloudProviderParametersRepository,
				configurationRepository, 
				teamRepository, 
				applicationDeployerBash,
				deploymentStatusTracker, 
				configurationDeploymentParametersRepository,
				domainService,
				deploymentConfigurationRepository,
				deploymentApplicationRepository,
				cloudProviderParametersCopyRepository,
				configurationDeploymentParamsCopyRepository,
				encryptionService,
				deploymentSecretService,
				deploymentGeneratedOutputService,
				salt,
				password);


		ReflectionTestUtils.setField(deploymentService, "deploymentRepository", deploymentRepository);
		ReflectionTestUtils.setField(configurationDeploymentParametersService, "configurationDeploymentParametersRepository",
				configurationDeploymentParametersRepository);
		ReflectionTestUtils.setField(cloudProviderParametersCopyService, "cloudProviderParametersCopyRepository", cloudProviderParametersCopyRepository);
		ReflectionTestUtils.setField(cloudProviderParametersService, "cloudProviderParametersRepository", cloudProviderParametersRepository);
		ReflectionTestUtils.setField(configurationService, "configurationRepository", configurationRepository);
		ReflectionTestUtils.setField(accountService, "accountRepository", accountRepository);
		ReflectionTestUtils.setField(applicationService, "applicationRepository", applicationRepository);
		ReflectionTestUtils.setField(deploymentApplicationService, "deploymentApplicationRepository", deploymentApplicationRepository);
		ReflectionTestUtils.setField(teamService, "teamRepository", teamRepository);
		Properties props = new Properties();
		props.put("be.applications.root", "blah");    
		props.put("be.deployments.root", "bleh");
		props.put("os.user.name", "blih");
		props.put("os.password", "bloh");
		props.put("os.tenancy.name", "bluh");
		props.put("os.auth.url", "blyh");
		subject.setProperties(props);
	}

	@Test
	public void can_delete_deployment_given_id() throws IOException, ApplicationDeployerException,
	NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
	IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		String theId = "blah";
		deployment(theId);

		CloudProviderParameters mockCloudProviderParameters = mock(CloudProviderParameters.class);
		when(mockCloudProviderParameters.getReference()).thenReturn(cppReference);
		when(cloudProviderParametersRepository.findByNameAndAccountUsername(A_CLOUD_PROVIDER_PARAMS_NAME, A_USER_NAME))
		.thenReturn(Optional.of(mockCloudProviderParameters));
		when(cloudProviderParametersService.findByReference(cppReference)).thenReturn(mockCloudProviderParameters);
		when(cloudProviderParametersRepository.findByReference(cppReference)).thenReturn(Optional.of(mockCloudProviderParameters));
		Account mockAccount = mock(Account.class);
		when(mockAccount.getUsername()).thenReturn(A_USER_NAME);
		when(mockAccount.getId()).thenReturn(1L);
		when(mockAccount.getEmail()).thenReturn("an@email.com");
		when(mockAccount.getPassword()).thenReturn("A password");
		when(mockAccount.getOrganisation()).thenReturn("An organisation");
		when(mockCloudProviderParameters.getAccount()).thenReturn(mockAccount);

		ResponseEntity response = subject.removeDeploymentByReference(principal, theId);

		assertThat(response.getStatusCode().value(), is(200));
	}

	@Test
	public void can_receive_notification_that_deployment_is_ready_for_teardown()
			throws IOException, ApplicationDeployerException, NoSuchPaddingException, InvalidKeyException,
			NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException, InvalidKeySpecException {
		String theId = "foo";
		deployment(theId);

		CloudProviderParameters mockCloudProviderParameters = mock(CloudProviderParameters.class);
		when(cloudProviderParametersRepository.findByNameAndAccountUsername(A_CLOUD_PROVIDER_PARAMS_NAME, A_USER_NAME))
		.thenReturn(Optional.of(mockCloudProviderParameters));

		ResponseEntity response = subject.readyToTearDown(tempKey, theId);
		assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
	}

	@Test
	public void ignores_teardown_notification_unless_the_correct_secret_is_given()
			throws IOException, ApplicationDeployerException, NoSuchPaddingException, InvalidKeyException,
			NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException, InvalidKeySpecException {
		String anIncorrectKey = "hey";
		ResponseEntity response = subject.readyToTearDown(anIncorrectKey, null);
		assertThat(response.getStatusCode().value(), is(404));
	}

	@Test(expected = DeploymentNotFoundException.class)
	public void returns_appropriate_error_when_deployment_not_found() throws IOException, ApplicationDeployerException,
	NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException,
	BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		String aNonExistentDeployment = "foo";
		when(deploymentRepository.findByReference(aNonExistentDeployment)).thenReturn(Optional.empty());

		subject.readyToTearDown(tempKey, aNonExistentDeployment);
	}

	@Test
	public void triggers_deletion_of_deployment_when_receiving_teardown_notification()
			throws IOException, ApplicationDeployerException, NoSuchPaddingException, InvalidKeyException,
			NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException, InvalidKeySpecException {
		String aDeploymentReference = "bar";
		Deployment mockDeployment = deployment(aDeploymentReference);
		when(mockDeployment.getId()).thenReturn(1234L);
		//when(mockDeployment.getCloudProviderParameters().getCloudProvider()).thenReturn(A_CLOUD_PROVIDER);

		CloudProviderParameters mockCloudProviderParameters = mock(CloudProviderParameters.class);
		when(cloudProviderParametersRepository.findByNameAndAccountUsername(A_CLOUD_PROVIDER_PARAMS_NAME, A_USER_NAME))
		.thenReturn(Optional.of(mockCloudProviderParameters));

		subject.readyToTearDown(tempKey, aDeploymentReference);

		verify(deploymentRepository).delete(1234L);
	}

	@Test
	public void can_recognise_an_IP_has_been_given() throws IOException, ApplicationDeployerException,
	NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException,
	BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		String anIp = "123.123.123.123";
		String aReference = "bar";
		Deployment mockDeployment = deployment(aReference);
		when(mockDeployment.getAccessIp()).thenReturn(anIp);
		when(deploymentRepository.findByAccessIp(anIp)).thenReturn(Optional.of(mockDeployment));

		CloudProviderParameters mockCloudProviderParameters = mock(CloudProviderParameters.class);
		when(cloudProviderParametersRepository.findByNameAndAccountUsername(A_CLOUD_PROVIDER_PARAMS_NAME, A_USER_NAME))
		.thenReturn(Optional.of(mockCloudProviderParameters));

		subject.readyToTearDown(tempKey, anIp);

		verify(deploymentRepository).findByAccessIp(anIp);
	}

	@Test(expected = NullPointerException.class)
	public void test_if_deployment_throws_exception_null_cloud_providers()
			throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException,
			IOException, ApplicationDeployerException {

		DeploymentResource deploymentResourceMock = mock(DeploymentResource.class);
		String accountUserName = "a_name";
		String appName = "an_app";

		when(principal.getName()).thenReturn(accountUserName);
		Account mockAccount = mock(Account.class);
		when(mockAccount.getUsername()).thenReturn(accountUserName);
		when(accountRepository.findByUsername(accountUserName)).thenReturn(Optional.of(mockAccount));

		Application applicationMock = mock(Application.class);
		when(applicationMock.getName()).thenReturn(appName);
		when(applicationMock.getAccount()).thenReturn(mockAccount);
		Set applicationCollection = new HashSet<>();
		applicationCollection.add(applicationMock);

		when(applicationRepository.findByAccountUsername(accountUserName,new Sort("sort.name"))).thenReturn(applicationCollection);
		when(deploymentResourceMock.getApplicationAccountUsername()).thenReturn(accountUserName);
		when(deploymentResourceMock.getApplicationName()).thenReturn(appName);
		when(deploymentResourceMock.getConfigurationAccountUsername()).thenReturn(accountUserName);
		when(deploymentResourceMock.getConfigurationName()).thenReturn("some_configuration_name");
		when(applicationRepository.findByAccountUsernameAndName(accountUserName, appName))
		.thenReturn(Optional.of(applicationMock));

		subject.addDeployment(new MockHttpServletRequest(), principal, deploymentResourceMock);

	}

	// run the flow of the add method and check no hiccups and deployment is created
	@Test
	public void test_add_deployment()
			throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException,
			IOException, ApplicationDeployerException {

		String sharedWithUsername = "sharedWithUsername";
		Principal principal = mock(Principal.class);
		given(principal.getName()).willReturn(sharedWithUsername);


		//get account of the user with whom application and configuration are shared
		Account account = mock(Account.class);
		String accountReference = "accountReference";
		given(accountRepository.findByUsername(sharedWithUsername)).willReturn(Optional.of(account));
		given(accountService.findByUsername(sharedWithUsername)).willCallRealMethod();
		given(account.getGivenName()).willReturn(sharedWithUsername);
		given(account.getUsername()).willReturn(sharedWithUsername);
		given(account.getFirstJoinedDate()).willReturn(new Date(0, 0, 0));
		given(account.getReference()).willReturn(accountReference);

		//get account of the user who owns application and configuration
		Account owner =  mock(Account.class);
		String username = "username";
		given(owner.getUsername()).willReturn(username);
		given(accountRepository.findByUsername(username)).willReturn(Optional.of(owner));
		given(accountService.findByUsername(username)).willReturn(owner);
		given(owner.getGivenName()).willReturn(username);
		given(owner.getUsername()).willReturn(username);
		given(owner.getFirstJoinedDate()).willReturn(new Date(0, 0, 0));
		given(owner.getReference()).willReturn(accountReference);


		DeploymentResource input = mock(DeploymentResource.class);
		given(input.getConfigurationAccountUsername()).willReturn(username);
		given(input.getApplicationAccountUsername()).willReturn(username);
		String sshkey = "sshkey";
		given(input.getUserSshKey()).willReturn(sshkey);

		//team
		Team team = mock(Team.class);
		String teamName = "someTeamName";
		String domainReference = "some ref";
		given(team.getName()).willReturn(teamName);
		given(team.getDomainReference()).willReturn(domainReference);
		
		//application
		given(input.getApplicationAccountUsername()).willReturn(username);
		String applicationName = "applicationName";
		given(input.getApplicationName()).willReturn(applicationName);
		Application application = mock(Application.class);
		given(application.getName()).willReturn(applicationName);
		given(applicationRepository.findByAccountUsernameAndName(username,applicationName)).willReturn(Optional.of(application));
		given(applicationService.findByAccountUsernameAndName(username,applicationName)).willReturn(application);

		//set up teams, sharedwith user is a member of only one of these teams
		Set<Account> teamAccounts = new HashSet<>();
		teamAccounts.add(account);
		teamAccounts.add(owner);
		given(team.getAccountsBelongingToTeam()).willReturn(teamAccounts);
		Set<Team> sharedWithTeams = new HashSet<>();
		sharedWithTeams.add(team);
		
		//application is shared not owned
		given(applicationRepository.findByAccountUsernameAndName(sharedWithUsername,applicationName))
		.willThrow(ApplicationNotFoundException.class);
		given(applicationService.findByAccountUsernameAndName(sharedWithUsername,applicationName))
		.willThrow(ApplicationNotFoundException.class);
		
		Set<Application> applications = new HashSet<>();
		applications.add(application);
		given(team.getApplicationsBelongingToTeam()).willReturn(applications);
		when(application.getSharedWithTeams()).thenReturn(sharedWithTeams);
		
		given(account.getMemberOfTeams()).willReturn(sharedWithTeams);
		given(applicationService.isApplicationSharedWithAccount(account, application)).willCallRealMethod();
		
		//configuration
		String configurationName = "config";
		Configuration config = mock(Configuration.class);
		when(config.getSharedWithTeams()).thenReturn(sharedWithTeams);
		when(input.getConfigurationAccountUsername()).thenReturn(username);
		when(input.getConfigurationName()).thenReturn(configurationName);
		when(config.getName()).thenReturn(configurationName);
		when(configurationRepository.findByNameAndAccountUsername(input.getConfigurationName(), account.getUsername())).thenThrow(ConfigurationNotFoundException.class);
		when(configurationService.findByNameAndAccountUsername(input.getConfigurationName(), account.getUsername())).thenCallRealMethod();
		when(configurationService.findByNameAndAccountUsername(input.getConfigurationName(), input.getConfigurationAccountUsername()))
		.thenReturn(config);
		when(configurationRepository.findByNameAndAccountUsername(input.getConfigurationName(), input.getConfigurationAccountUsername()))
		.thenReturn(Optional.of(config));
		when(config.getHardUsageLimit()).thenReturn(1.0);
		when(configurationService.getTotalConsumption(config, deploymentIndexService)).thenReturn(0.5);
		
		//cdp
		String cdpReference = "cdpReference";
		String cdpName = "cdpName";
		given(config.getConfigDeployParamsReference()).willReturn(cdpReference);
		ConfigDeploymentParamsCopy configDeploymentParamsCopy = mock(ConfigDeploymentParamsCopy.class);
		given(configDeploymentParamsCopy.getName()).willReturn(cdpName);
		given(configDeploymentParamsCopy.getConfigurationDeploymentParametersReference()).willReturn(cdpReference);
		given(configurationDeploymentParamsCopyRepository.findByConfigurationDeploymentParametersReference(cdpReference))
		.willReturn(Optional.of(configDeploymentParamsCopy));
		given(configurationDeploymentParamsCopyService.findByConfigurationDeploymentParametersReference(cdpReference))
		.willReturn(configDeploymentParamsCopy);
		List<ConfigDeploymentParamsCopy> cdpCopyList = new ArrayList<>();
		cdpCopyList.add(configDeploymentParamsCopy);
		given(configurationDeploymentParamsCopyRepository.findByName(cdpName)).willReturn(cdpCopyList);
		given(configurationDeploymentParamsCopyService.findByName(cdpName)).willReturn(configDeploymentParamsCopy);
		given(configurationService.isConfigurationSharedWithAccount(account, config)).willCallRealMethod();
		given(configurationDeploymentParamsCopyRepository.findByConfigurationDeploymentParametersReference(cdpReference))
		.willReturn(Optional.of(configDeploymentParamsCopy));
		given(configurationDeploymentParamsCopyService.findByConfigurationDeploymentParametersReference(cdpReference))
		.willReturn(configDeploymentParamsCopy);
		
		//assigned cloud provider parameters
		String cloudProviderParametersName = "cppName";
		config.cloudProviderParametersName = cloudProviderParametersName;
		config.setCloudProviderParametersName(cloudProviderParametersName);
		when(config.getCloudProviderParametersName()).thenReturn(cloudProviderParametersName);
		when(cloudProviderParameters.getName()).thenReturn(cloudProviderParametersName);
		CloudProviderParameters selectedCloudProviderParameters = mock(CloudProviderParameters.class);
		given(cloudProviderParametersRepository.findByNameAndAccountUsername(cloudProviderParametersName,
				username)).willReturn(Optional.of(selectedCloudProviderParameters));
		given(cloudProviderParametersService.findByNameAndAccountUsername(cloudProviderParametersName,
				username)).willReturn(selectedCloudProviderParameters);
		given(selectedCloudProviderParameters.getAccount()).willReturn(account);
		String cloudProviderParametersReference = "cppReference";
		given(selectedCloudProviderParameters.getReference()).willReturn(cloudProviderParametersReference);
		String cloudProvider = "ostack";
		given(selectedCloudProviderParameters.getCloudProvider()).willReturn(cloudProvider);
		given(cloudProviderParametersService.isCloudProviderParametersSharedWithAccount(account, selectedCloudProviderParameters)).willCallRealMethod();
		
		//application cloud providers
		Collection<ApplicationCloudProvider> acpList = new ArrayList<>();
		ApplicationCloudProvider acp = mock(ApplicationCloudProvider.class);
		given(acp.getName()).willReturn("somename");
		given(acp.getPath()).willReturn("somepath");
		acpList.add(acp);
		given(application.getCloudProviders()).willReturn(acpList);

		//cloud provider parameters copy
		CloudProviderParamsCopy cppCopy = mock(CloudProviderParamsCopy.class);
		given(cloudProviderParametersCopyRepository.findByCloudProviderParametersReference(cloudProviderParametersReference))
		.willReturn(Optional.of(cppCopy));
		given(cloudProviderParametersCopyService.findByCloudProviderParametersReference(cloudProviderParametersReference))
		.willReturn(cppCopy);
		given(cppCopy.getAccount()).willReturn(account);

		//deployment application
		DeploymentApplication deploymentApplication = mock(DeploymentApplication.class);
		given(deploymentApplicationService.createDeploymentApplication(application)).willReturn(deploymentApplication);
		given(deploymentApplication.getName()).willReturn(applicationName);
		given(deploymentApplication.getAccount()).willReturn(account);
		Collection<DeploymentApplicationCloudProvider> dacpList = new ArrayList<>();
		given(deploymentApplicationRepository.save(deploymentApplication)).willReturn(deploymentApplication);
		given(deploymentApplicationService.save(deploymentApplication)).willReturn(deploymentApplication);

		// application inputs 
		Collection<DeploymentAssignedInputResource> inputResources = new ArrayList<>();
		DeploymentAssignedInputResource inputResource = mock(DeploymentAssignedInputResource.class);
		when(inputResource.getInputName()).thenReturn("somename");
		when(inputResource.getAssignedValue()).thenReturn("somevalue");
		inputResources.add(inputResource);
		when(input.getAssignedInputs()).thenReturn(inputResources);

		Deployment deployment = mock(Deployment.class);
		given(deployment.getId()).willReturn(1l);
		given(deploymentService.save(isA(Deployment.class))).willCallRealMethod();
		given(deploymentRepository.save(isA(Deployment.class))).willReturn(deployment);
		given(deployment.getAccount()).willReturn(account);
		given(deployment.getDeploymentApplication()).willReturn(deploymentApplication);

		ResponseEntity<?> addedDeployment = subject.addDeployment(new MockHttpServletRequest(), principal, input);
		assertNotNull(addedDeployment.getBody());
		assertTrue(addedDeployment.getStatusCode().equals(HttpStatus.CREATED));
		assertTrue(application.getCloudProviders().containsAll(deploymentApplication.getCloudProviders()));
		assertTrue(deployment.getDeploymentApplication().equals(deploymentApplication));
	}

	@Test(expected = InvalidConfigurationInputException.class)
	public void configuration_name_not_specified() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException, IOException, ApplicationDeployerException{

		String username = "username";
		Principal principal = mock(Principal.class);
		given(principal.getName()).willReturn(username);

		DeploymentResource input = mock(DeploymentResource.class);
		given(input.getConfigurationName()).willReturn(null);
		given(input.getApplicationAccountUsername()).willReturn(username);

		//get account
		Account account = mock(Account.class);
		given(accountRepository.findByUsername(username)).willReturn(Optional.of(account));
		given(accountService.findByUsername(username)).willReturn(account);
		given(account.getGivenName()).willReturn(username);
		given(account.getUsername()).willReturn(username);

		//application
		String applicationName = "applicationName";
		given(input.getApplicationName()).willReturn(applicationName);
		Application application = mock(Application.class);
		given(application.getName()).willReturn(applicationName);
		given(application.getAccount()).willReturn(account);
		given(applicationRepository.findByAccountUsernameAndName(username,applicationName)).willReturn(Optional.of(application));
		given(applicationService.findByAccountUsernameAndName(username,applicationName)).willCallRealMethod();

		ResponseEntity<?> addedDeployment = subject.addDeployment(new MockHttpServletRequest(), principal, input);
	}


	@Test(expected = InvalidConfigurationInputException.class)
	public void configuration_owner_name_not_specified() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException, IOException, ApplicationDeployerException{

		String username = "username";
		Principal principal = mock(Principal.class);
		given(principal.getName()).willReturn(username);

		DeploymentResource input = mock(DeploymentResource.class);
		given(input.getConfigurationName()).willReturn("somename");
		given(input.getApplicationAccountUsername()).willReturn(username);
		given(input.getConfigurationAccountUsername()).willReturn(null);

		//get account
		Account account = mock(Account.class);
		given(accountRepository.findByUsername(username)).willReturn(Optional.of(account));
		given(accountService.findByUsername(username)).willReturn(account);
		given(account.getGivenName()).willReturn(username);
		given(account.getUsername()).willReturn(username);

		//application
		String applicationName = "applicationName";
		given(input.getApplicationName()).willReturn(applicationName);
		Application application = mock(Application.class);
		given(application.getName()).willReturn(applicationName);
		given(application.getAccount()).willReturn(account);
		given(applicationRepository.findByAccountUsernameAndName(username,applicationName)).willReturn(Optional.of(application));
		given(applicationService.findByAccountUsernameAndName(username,applicationName)).willCallRealMethod();

		ResponseEntity<?> addedDeployment = subject.addDeployment(new MockHttpServletRequest(), principal, input);
	}

	@Test(expected = InvalidApplicationInputException.class)
	public void invalid_application_input_no_app_name() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException, IOException, ApplicationDeployerException{

		String username = "username";
		Principal principal = mock(Principal.class);
		given(principal.getName()).willReturn(username);

		DeploymentResource input = mock(DeploymentResource.class);
		given(input.getApplicationName()).willReturn(null);

		ResponseEntity<?> addedDeployment = subject.addDeployment(new MockHttpServletRequest(), principal, input);
	}

	@Test(expected = InvalidApplicationInputException.class)
	public void invalid_application_input_no_app_owner_acc_username() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException, IOException, ApplicationDeployerException{

		String username = "username";
		Principal principal = mock(Principal.class);
		given(principal.getName()).willReturn(username);

		DeploymentResource input = mock(DeploymentResource.class);
		given(input.getApplicationAccountUsername()).willReturn(null);

		ResponseEntity<?> addedDeployment = subject.addDeployment(new MockHttpServletRequest(), principal, input);
	}

	@Test(expected=ApplicationNotFoundException.class)
	public void app_not_found_exception() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException, IOException, ApplicationDeployerException{

		String username = "username";
		Principal principal = mock(Principal.class);
		given(principal.getName()).willReturn(username);

		DeploymentResource input = mock(DeploymentResource.class);
		given(input.getConfigurationName()).willReturn("somename");
		given(input.getConfigurationAccountUsername()).willReturn(username);
		given(input.getApplicationAccountUsername()).willReturn(username);

		//get account
		Account account = mock(Account.class);
		given(accountRepository.findByUsername(username)).willReturn(Optional.of(account));
		given(accountService.findByUsername(username)).willReturn(account);
		given(account.getGivenName()).willReturn(username);
		given(account.getUsername()).willReturn(username);

		//application
		String applicationName = "applicationName";
		given(input.getApplicationName()).willReturn(applicationName);
		Application application = mock(Application.class);
		given(application.getName()).willReturn(applicationName);
		given(application.getAccount()).willReturn(account);
		
		//team
		String domainReference = "some ref";
//		given(input.getDomainReference()).willReturn(domainReference);
//		given(teamRepository.findByDomainReference(domainReference)).willReturn(Optional.of(mock(Team.class)));
//		given(teamService.findByDomainReference(domainReference)).willCallRealMethod();
		given(applicationRepository.findByAccountUsernameAndName(username,applicationName)).willThrow(ApplicationNotFoundException.class);
		given(applicationService.findByAccountUsernameAndName(username,applicationName)).willCallRealMethod();

		ResponseEntity<?> addedDeployment = subject.addDeployment(new MockHttpServletRequest(), principal, input);

	}

	private Deployment deployment(String reference) {
		Deployment mockDeployment = mock(Deployment.class);
		when(mockDeployment.getReference()).thenReturn(reference);
		CloudProviderParameters mockCloudProviderParameters = mock(CloudProviderParameters.class);
		DeploymentApplication mockDeploymentApp = mock(DeploymentApplication.class);
		when(mockDeploymentApp.getRepoPath()).thenReturn("irrelevant");
		when(mockDeployment.getDeploymentApplication()).thenReturn(mockDeploymentApp);
		Account mockAccount = mock(Account.class);
		when(mockAccount.getUsername()).thenReturn(A_USER_NAME);
		when(mockDeployment.getAccount()).thenReturn(mockAccount);
		when(deploymentRepository.findByReference(reference)).thenReturn(Optional.of(mockDeployment));
		when(mockDeployment.getCloudProviderParametersReference()).thenReturn(cppReference);
		DeploymentConfiguration deploymentConfiguration = mock(DeploymentConfiguration.class);
		when(mockDeployment.getDeploymentConfiguration()).thenReturn(deploymentConfiguration);
		when(deploymentConfiguration.getName()).thenReturn("some string");
		when(deploymentConfiguration.getConfigDeploymentParametersReference()).thenReturn(reference);
		when(cloudProviderParametersRepository.findByReference(Mockito.anyString())).thenReturn(Optional.of(mockCloudProviderParameters));
		when(mockCloudProviderParameters.getAccount()).thenReturn(mockAccount);
		java.sql.Date date = mock(java.sql.Date.class);
		when(mockAccount.getFirstJoinedDate()).thenReturn(date);
		when(mockAccount.getReference()).thenReturn("some ref");
		when(mockAccount.getGivenName()).thenReturn("given name");
		ConfigurationDeploymentParameters cdps = mock(ConfigurationDeploymentParameters.class);
		when(configurationDeploymentParametersService.findByReference(reference)).thenReturn(cdps);
		when(configurationDeploymentParametersRepository.findByReference(reference)).thenReturn(Optional.of(cdps));
		when(cdps.getReference()).thenReturn(reference);
		return mockDeployment;
	}

	@Test
	public void baseUrl() throws Exception {

        /*
            Portal Dev          https://dev.api.portal.tsi.ebi.ac.uk
            Portal Master       https://api.portal.tsi.ebi.ac.uk
            Local Deployment    http://localhost:8080

            With server path    https://api.portal.tsi.ebi.ac.uk/deployments/TSI000000000000001/stopme

         */

		assertEquals( "http://localhost:8080"               , subject.baseURL(mockRequest("localhost", 8080)) );
		assertEquals( "http://dev.api.portal.tsi.ebi.ac.uk" , subject.baseURL(mockRequest("dev.api.portal.tsi.ebi.ac.uk")) );
		assertEquals( "http://api.portal.tsi.ebi.ac.uk"     , subject.baseURL(mockRequest("api.portal.tsi.ebi.ac.uk", -1, "/deployments/TSI000000000000001/stopme")) );
	}

	MockHttpServletRequest mockRequest(String host)            {  return mockRequest(host, -1);	          }
	MockHttpServletRequest mockRequest(String host, int port)  {  return mockRequest(host, port, null);   }

	MockHttpServletRequest mockRequest(String host, int port, String path) {

		MockHttpServletRequest request = new MockHttpServletRequest();

		if (path != null)
			request.setRequestURI(path);

//	    request.setLocalPort(8080);
//	    request.setRemotePort(8080);

		if (port != -1)
			request.setServerPort(port);

//	    request.setProtocol("https");

//	    request.setRemoteHost("remoteHost");
//	    request.setLocalName("remoteHost");
		request.setServerName(host);

		return request;
	}
}