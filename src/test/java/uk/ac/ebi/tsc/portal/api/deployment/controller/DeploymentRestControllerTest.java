package uk.ac.ebi.tsc.portal.api.deployment.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;
import uk.ac.ebi.tsc.portal.api.account.service.AccountService;
import uk.ac.ebi.tsc.portal.api.application.repo.Application;
import uk.ac.ebi.tsc.portal.api.application.repo.ApplicationCloudProvider;
import uk.ac.ebi.tsc.portal.api.application.repo.ApplicationRepository;
import uk.ac.ebi.tsc.portal.api.application.service.ApplicationService;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller.CloudProviderParametersCopyResource;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller.CloudProviderParametersResource;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParametersRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopy;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyField;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParametersService;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParamsCopyService;
import uk.ac.ebi.tsc.portal.api.configuration.repo.*;
import uk.ac.ebi.tsc.portal.api.configuration.service.ConfigDeploymentParamsCopyService;
import uk.ac.ebi.tsc.portal.api.configuration.service.ConfigurationDeploymentParametersService;
import uk.ac.ebi.tsc.portal.api.configuration.service.ConfigurationService;
import uk.ac.ebi.tsc.portal.api.deployment.repo.Deployment;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentApplication;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentApplicationRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentConfiguration;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentConfigurationRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentStatusRepository;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentNotFoundException;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentService;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;
import uk.ac.ebi.tsc.portal.api.team.repo.TeamRepository;
import uk.ac.ebi.tsc.portal.api.team.service.TeamService;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceRepository;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceStatusRepository;
import uk.ac.ebi.tsc.portal.api.volumeinstance.service.VolumeInstanceService;
import uk.ac.ebi.tsc.portal.clouddeployment.application.ApplicationDeployerBash;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDeployerException;
import uk.ac.ebi.tsc.portal.usage.tracker.DeploymentStatusTracker;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.sql.Date;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

/**
 * Created on 16/02/2016.
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
public class DeploymentRestControllerTest {

	private static final String A_CLOUD_PROVIDER_PARAMS_NAME = "OS TEST";
	public final String A_USER_NAME = "A User Name";
	public final String A_CLOUD_PROVIDER = "OSTACK";
    String salt= "salt";
    String password= "password";

	DeploymentRepository mockDeploymentRepo = mock(DeploymentRepository.class);
	DeploymentStatusRepository mockDeploymentStatusRepo = mock(DeploymentStatusRepository.class);
	AccountRepository mockAccountRepo = mock(AccountRepository.class);
	ApplicationRepository mockApplicationRepo = mock(ApplicationRepository.class);
	VolumeInstanceRepository mockVolumeRepo = mock(VolumeInstanceRepository.class);
	VolumeInstanceStatusRepository mockVolumeStatusRepo = mock(VolumeInstanceStatusRepository.class);
	CloudProviderParametersRepository mockCloudCredentialsRepo = mock(CloudProviderParametersRepository.class);
	ConfigurationRepository mockConfigurationRepo = mock(ConfigurationRepository.class);
	ApplicationDeployerBash mockApplicationDeployerBash = mock(ApplicationDeployerBash.class);
	DeploymentStatusTracker mockDeploymentStatusTracker = mock(DeploymentStatusTracker.class);
	ConfigurationDeploymentParametersRepository deploymentParametersRepository = mock(
			ConfigurationDeploymentParametersRepository.class);
	TeamRepository teamRepository = mock(TeamRepository.class);

	
	@MockBean
	private DeploymentService deploymentService;

	@MockBean
	private ConfigurationService configurationService;

	@MockBean
	private ConfigurationDeploymentParametersService deploymentParametersService;

	@MockBean
	private TeamService teamService;

	@MockBean
	private DeploymentRepository deploymentRepository;
	
	@MockBean
	DomainService domainService;
	
	@MockBean
	DeploymentConfigurationRepository deploymentConfigurationRepository;
	
	@MockBean
	DeploymentApplicationRepository deploymentApplicationRepository;

	DeploymentRestController subject;

	private Principal principalMock;

	String tempKey = "dGhlcG9ydGFsZGV2ZWxvcGVkYnl0c2lpc2F3ZXNvbWU=";

	@MockBean
	CloudProviderParamsCopyRepository cloudProviderParametersCopyRepository;
	
	@MockBean
	CloudProviderParamsCopyService cloudProviderParametersCopyService;
	
	@MockBean
	CloudProviderParametersService cppService;
	
	@MockBean
	ConfigDeploymentParamsCopyService cdpCopyService;
	
	@MockBean
	ConfigDeploymentParamsCopyRepository cdpCopyRepository;
	
	String cppReference = "some reference";
	String cdpReference = "some reference";
	
	@MockBean
	DeploymentConfiguration deploymentConfiguration;
	
	@MockBean
	CloudProviderParameters cppMock;
	
	@MockBean
	EncryptionService encryptionService;
	
	@Before 
	public void setUp() {
		subject = new DeploymentRestController(mockDeploymentRepo, mockDeploymentStatusRepo, mockAccountRepo,
				mockApplicationRepo, mockVolumeRepo, mockVolumeStatusRepo, mockCloudCredentialsRepo,
				mockConfigurationRepo, teamRepository, mockApplicationDeployerBash, mockDeploymentStatusTracker, deploymentParametersRepository,
				domainService,
				deploymentConfigurationRepository,
				deploymentApplicationRepository,
				cloudProviderParametersCopyRepository,
				cdpCopyRepository,
				encryptionService,
				salt,
				password);
		
		ReflectionTestUtils.setField(cppService, "cloudProviderParametersRepository", mockCloudCredentialsRepo);
		ReflectionTestUtils.setField(deploymentParametersService, "configurationDeploymentParametersRepository", deploymentParametersRepository);

		Properties props = new Properties();
		props.put("be.applications.root", "blah");    
		props.put("be.deployments.root", "bleh");
		props.put("os.user.name", "blih");
		props.put("os.password", "bloh");
		props.put("os.tenancy.name", "bluh");
		props.put("os.auth.url", "blyh");
		subject.setProperties(props);

		this.principalMock = mock(Principal.class);
		when(this.principalMock.getName()).thenReturn("A user name");
	}

	@Test
	public void can_delete_deployment_given_id() throws IOException, ApplicationDeployerException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
			IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
		String theId = "blah";
		deployment(theId);
		
		CloudProviderParameters mockCloudProviderParameters = mock(CloudProviderParameters.class);
		when(mockCloudProviderParameters.getReference()).thenReturn(cppReference);
		when(mockCloudCredentialsRepo.findByNameAndAccountUsername(A_CLOUD_PROVIDER_PARAMS_NAME, A_USER_NAME))
				.thenReturn(Optional.of(mockCloudProviderParameters));
		when(cppService.findByReference(cppReference)).thenReturn(mockCloudProviderParameters);
		when(mockCloudCredentialsRepo.findByReference(cppReference)).thenReturn(Optional.of(mockCloudProviderParameters));
		Account mockAccount = mock(Account.class);
		when(mockAccount.getUsername()).thenReturn(A_USER_NAME);
		when(mockAccount.getId()).thenReturn(1L);
		when(mockAccount.getEmail()).thenReturn("an@email.com");
		when(mockAccount.getPassword()).thenReturn("A password");
		when(mockAccount.getOrganisation()).thenReturn("An organisation");
		when(mockCloudProviderParameters.getAccount()).thenReturn(mockAccount);
		
		ResponseEntity response = subject.removeDeploymentByReference(this.principalMock, theId);

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
		when(mockCloudCredentialsRepo.findByNameAndAccountUsername(A_CLOUD_PROVIDER_PARAMS_NAME, A_USER_NAME))
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
		when(mockDeploymentRepo.findByReference(aNonExistentDeployment)).thenReturn(Optional.empty());

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
		when(mockCloudCredentialsRepo.findByNameAndAccountUsername(A_CLOUD_PROVIDER_PARAMS_NAME, A_USER_NAME))
				.thenReturn(Optional.of(mockCloudProviderParameters));

		subject.readyToTearDown(tempKey, aDeploymentReference);

		verify(mockDeploymentRepo).delete(1234L);
	}

	@Test
	public void can_recognise_an_IP_has_been_given() throws IOException, ApplicationDeployerException,
			NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		String anIp = "123.123.123.123";
		String aReference = "bar";
		Deployment mockDeployment = deployment(aReference);
		when(mockDeployment.getAccessIp()).thenReturn(anIp);
		when(mockDeploymentRepo.findByAccessIp(anIp)).thenReturn(Optional.of(mockDeployment));

		CloudProviderParameters mockCloudProviderParameters = mock(CloudProviderParameters.class);
		when(mockCloudCredentialsRepo.findByNameAndAccountUsername(A_CLOUD_PROVIDER_PARAMS_NAME, A_USER_NAME))
				.thenReturn(Optional.of(mockCloudProviderParameters));

		subject.readyToTearDown(tempKey, anIp);

		verify(mockDeploymentRepo).findByAccessIp(anIp);
	}

	@Test(expected = NullPointerException.class)
	public void test_if_deployment_throws_exception_null_cloud_providers()
			throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException,
			IOException, ApplicationDeployerException {

		DeploymentResource deploymentResourceMock = mock(DeploymentResource.class);
		String accountUserName = "a_name";
		String appName = "an_app";

		when(principalMock.getName()).thenReturn(accountUserName);
		Account mockAccount = mock(Account.class);
		when(mockAccount.getUsername()).thenReturn(accountUserName);
		when(mockAccountRepo.findByUsername(accountUserName)).thenReturn(Optional.of(mockAccount));

		Application applicationMock = mock(Application.class);
		when(applicationMock.getName()).thenReturn(appName);
		when(applicationMock.getAccount()).thenReturn(mockAccount);
		Set applicationCollection = new HashSet<>();
		applicationCollection.add(applicationMock);

		when(mockApplicationRepo.findByAccountUsername(accountUserName)).thenReturn(applicationCollection);
		when(deploymentResourceMock.getApplicationAccountUsername()).thenReturn(accountUserName);
		when(deploymentResourceMock.getApplicationName()).thenReturn(appName);
		when(mockApplicationRepo.findByAccountUsernameAndName(accountUserName, appName))
				.thenReturn(Optional.of(applicationMock));

		subject.addDeployment(principalMock, deploymentResourceMock);

	}

	// run the flow of the add method and check no hiccups
	@Test
	public void test_add_deployment_parameters()
			throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException,
			IOException, ApplicationDeployerException {

		ReflectionTestUtils.setField(deploymentService, "deploymentRepository", deploymentRepository);
		ReflectionTestUtils.setField(subject, "deploymentService", deploymentService);
		AccountService accountService = mock(AccountService.class);
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		ApplicationService applicationService = mock(ApplicationService.class);
		ReflectionTestUtils.setField(subject, "applicationService", applicationService);
		VolumeInstanceService volumeInstanceService = mock(VolumeInstanceService.class);
		ReflectionTestUtils.setField(subject, "volumeInstanceService", volumeInstanceService);
		CloudProviderParametersService cloudProviderParametersService = mock(CloudProviderParametersService.class);
		ReflectionTestUtils.setField(subject, "cloudProviderParametersService", cloudProviderParametersService);
		ReflectionTestUtils.setField(subject, "configurationService", configurationService);
		ApplicationDeployerBash applicationDeployerBash = mock(ApplicationDeployerBash.class);
		ReflectionTestUtils.setField(subject, "applicationDeployerBash", applicationDeployerBash);
		ReflectionTestUtils.setField(deploymentParametersService, "configurationDeploymentParametersRepository",
				deploymentParametersRepository);
		ReflectionTestUtils.setField(subject, "deploymentParametersService", deploymentParametersService);
		ReflectionTestUtils.setField(subject, "cloudProviderParametersCopyService", cloudProviderParametersCopyService);
		ReflectionTestUtils.setField(cloudProviderParametersCopyService, "cloudProviderParametersCopyRepository", cloudProviderParametersCopyRepository);
		ReflectionTestUtils.setField(cloudProviderParametersCopyService, "encryptionService", encryptionService);
		
		Principal principal = mock(Principal.class);
		String principalName = "username";
		when(principal.getName()).thenReturn(principalName);

		Deployment mockDeployment = mock(Deployment.class);
		DeploymentResource input = mock(DeploymentResource.class);
		String keyName = "keyName";
		String keyValue = "keyValue";

		// inputs
		Collection<DeploymentAssignedInputResource> inputResources = new ArrayList<>();
		DeploymentAssignedInputResource inputResource = mock(DeploymentAssignedInputResource.class);
		when(inputResource.getInputName()).thenReturn(keyName);
		when(inputResource.getAssignedValue()).thenReturn(keyValue);
		inputResources.add(inputResource);
		when(input.getAssignedInputs()).thenReturn(inputResources);

		// volumes
		Collection<DeploymentAttachedVolumeResource> volumeResources = new ArrayList<>();
		when(input.getAttachedVolumes()).thenReturn(null);

		// assigned parameters
		String parameterName = "floating_ip_pool";
		String parameterValue = "to_change";
		Map<String, String> assignedParameters = new HashMap<>();
		assignedParameters.put(parameterName, parameterValue);
		Collection<DeploymentAssignedParameterResource> assignedParameterResources = new ArrayList<>();
		DeploymentAssignedParameterResource parameterResource = mock(DeploymentAssignedParameterResource.class);
		when(parameterResource.getParameterName()).thenReturn(parameterName);
		when(parameterResource.getParameterValue()).thenReturn(parameterValue);
		assignedParameterResources.add(parameterResource);
		when(input.getAssignedParameters()).thenReturn(assignedParameterResources);

		// assigned configuration
		String configurationName = "configName";
		String configurationUsername = "configUsername";
		when(input.getConfigurationName()).thenReturn(configurationName);
		when(input.getConfigurationAccountUsername()).thenReturn(configurationUsername);

		String cloudProviderParametersName = "cppName";
		CloudProviderParametersResource cloudProviderParametersResource = mock(CloudProviderParametersResource.class);
		when(cloudProviderParametersResource.getName()).thenReturn(cloudProviderParametersName);
		when(cloudProviderParametersResource.getAccountUsername()).thenReturn(principalName);
		//when(input.getCloudProviderParameters()).thenReturn(cloudProviderParametersResource);

		//input.cloudProviderParameters = cloudProviderParametersResource;
		CloudProviderParameters selectedCloudProviderParameters = mock(CloudProviderParameters.class);
		when(cloudProviderParametersService.findByNameAndAccountUsername(Mockito.anyString(),
			Mockito.anyString())).thenReturn(selectedCloudProviderParameters);
		String cloudProvider = "ostack";
		selectedCloudProviderParameters.cloudProvider = cloudProvider;
		when(selectedCloudProviderParameters.getCloudProvider()).thenReturn(cloudProvider);

		Account account = mock(Account.class);
		selectedCloudProviderParameters.account = account;
		when(selectedCloudProviderParameters.getAccount()).thenReturn(account);
		when(accountService.findByUsername(principalName)).thenReturn(account);
		when(account.getGivenName()).thenReturn(principalName);
		when(account.getUsername()).thenReturn(principalName);
		when(input.getApplicationAccountUsername()).thenReturn(principalName);
		
		account.username = principalName;
		when(account.getUsername()).thenReturn(principalName);
		when(accountService.findByUsername(principalName)).thenReturn(account);
		when(accountService.findByEmail(input.getApplicationAccountUsername())).thenReturn(account);

		String applicationName = "appName";
		DeploymentApplication deploymentApplication = 
				mock(DeploymentApplication.class);
		when(deploymentApplication.getName()).thenReturn(applicationName);
		when(deploymentApplication.getAccount()).thenReturn(account);
		when(input.getApplicationName()).thenReturn(applicationName);

		Configuration config = mock(Configuration.class);
		String configName = "configName";
		given(configurationService.findByNameAndAccountEmail(input.getConfigurationName(),
				input.getConfigurationAccountUsername())).willReturn(config);
		ConfigurationDeploymentParameters configurationParameters = mock(ConfigurationDeploymentParameters.class);
		String configurationParameterName = "configurationParameterName";
		when(configurationParameters.getName()).thenReturn(configurationParameterName);
		when(deploymentParametersService.findByName(configurationParameterName)).thenReturn(configurationParameters);
		ConfigurationDeploymentParameter configurationParameter = mock(ConfigurationDeploymentParameter.class);
		when(configurationParameter.getKey()).thenReturn(parameterName);
		when(configurationParameter.getValue()).thenReturn(parameterValue);
		Set<ConfigurationDeploymentParameter> allParameters = new HashSet<>();
		allParameters.add(configurationParameter);
		when(configurationParameters.getConfigurationDeploymentParameter()).thenReturn(allParameters);
		when(config.getName()).thenReturn(configName);
		when(config.getAccount()).thenReturn(account);
		when(account.getEmail()).thenReturn(configurationUsername);

		
		Application application = mock(Application.class);
		when(application.getName()).thenReturn(applicationName);
		when(applicationService.findByAccountUsernameAndName(principalName, applicationName)).thenReturn(application);
		when(application.getAccount()).thenReturn(account);
		when(input.getApplicationName()).thenReturn(applicationName);
		when(applicationService.findByAccountUsernameAndName(principalName, applicationName)).thenReturn(application);
		
		Collection<ApplicationCloudProvider> acp = new ArrayList<>();
		when(application.getCloudProviders()).thenReturn(acp);
		application.cloudProviders = acp;
		
		String cppReference = "someReference";
		CloudProviderParametersCopyResource cppCopyResource = mock(CloudProviderParametersCopyResource.class);
		when(input.getCloudProviderParametersCopy()).thenReturn(cppCopyResource);
		when(cppCopyResource.getCloudProviderParametersReference()).thenReturn(cppReference);
		when(selectedCloudProviderParameters.getReference()).thenReturn(cppReference);
		when(cloudProviderParametersService.findByReference(Mockito.anyString())).thenReturn(selectedCloudProviderParameters);
		CloudProviderParamsCopy cppCopy = mock(CloudProviderParamsCopy.class);

		Collection<CloudProviderParamsCopyField> fields = new ArrayList();
		cppCopy.fields = fields;
		when(cppCopy.getFields()).thenReturn(fields);
		when(cloudProviderParametersCopyRepository.findByCloudProviderParametersReference(Mockito.anyString())).thenReturn(Optional.of(cppCopy));
		when(cloudProviderParametersCopyService.findByCloudProviderParametersReference(Mockito.anyString())).thenCallRealMethod();
		when(cppCopy.getAccount()).thenReturn(account);
		
		when(input.getConfigurationName()).thenReturn(configurationName);
		when(input.getConfigurationAccountUsername()).thenReturn(configurationUsername);
		Long configId = 1L;
		String reference = "reference";
		Deployment deployment = mock(Deployment.class);
		when(configurationService.findByNameAndAccountUsername(configName, principalName)).thenReturn(config);
		given(deploymentService.save(isA(Deployment.class))).willCallRealMethod();
		given(deploymentRepository.save(isA(Deployment.class))).willReturn(deployment);
		when(deployment.getDeploymentApplication()).thenReturn(deploymentApplication);
		DeploymentConfiguration depConfiguration = mock(DeploymentConfiguration.class);
		when(depConfiguration.getName()).thenReturn("some name");
		when(deployment.getDeploymentConfiguration()).thenReturn(depConfiguration);
		when(deployment.getReference()).thenReturn(reference);
		when(deployment.getAccount()).thenReturn(account);
		
		ResponseEntity<?> addedDeployment = subject.addDeployment(principal, input);
		assertNotNull(addedDeployment.getBody());
		assertTrue(addedDeployment.getStatusCode().equals(HttpStatus.CREATED));
	}
	
	@Test
	public void test_deployment_application_created()
			throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException,
			IOException, ApplicationDeployerException {

		ReflectionTestUtils.setField(deploymentService, "deploymentRepository", deploymentRepository);
		ReflectionTestUtils.setField(subject, "deploymentService", deploymentService);
		AccountService accountService = mock(AccountService.class);
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		ApplicationService applicationService = mock(ApplicationService.class);
		ReflectionTestUtils.setField(subject, "applicationService", applicationService);
		VolumeInstanceService volumeInstanceService = mock(VolumeInstanceService.class);
		ReflectionTestUtils.setField(subject, "volumeInstanceService", volumeInstanceService);
		CloudProviderParametersService cloudProviderParametersService = mock(CloudProviderParametersService.class);
		ReflectionTestUtils.setField(subject, "cloudProviderParametersService", cloudProviderParametersService);
		ReflectionTestUtils.setField(subject, "configurationService", configurationService);
		ApplicationDeployerBash applicationDeployerBash = mock(ApplicationDeployerBash.class);
		ReflectionTestUtils.setField(subject, "applicationDeployerBash", applicationDeployerBash);
		ReflectionTestUtils.setField(deploymentParametersService, "configurationDeploymentParametersRepository",
				deploymentParametersRepository);
		ReflectionTestUtils.setField(subject, "deploymentParametersService", deploymentParametersService);
		ReflectionTestUtils.setField(subject, "cloudProviderParametersCopyService", cloudProviderParametersCopyService);
		ReflectionTestUtils.setField(cloudProviderParametersCopyService, "cloudProviderParametersCopyRepository", cloudProviderParametersCopyRepository);
		ReflectionTestUtils.setField(cloudProviderParametersCopyService, "encryptionService", encryptionService);
		Principal principal = mock(Principal.class);
		String principalName = "username";
		when(principal.getName()).thenReturn(principalName);

		Deployment mockDeployment = mock(Deployment.class);
		DeploymentResource input = mock(DeploymentResource.class);
		String keyName = "keyName";
		String keyValue = "keyValue";

		// inputs
		Collection<DeploymentAssignedInputResource> inputResources = new ArrayList<>();
		DeploymentAssignedInputResource inputResource = mock(DeploymentAssignedInputResource.class);
		when(inputResource.getInputName()).thenReturn(keyName);
		when(inputResource.getAssignedValue()).thenReturn(keyValue);
		inputResources.add(inputResource);
		when(input.getAssignedInputs()).thenReturn(inputResources);

		// volumes
		Collection<DeploymentAttachedVolumeResource> volumeResources = new ArrayList<>();
		when(input.getAttachedVolumes()).thenReturn(null);

		// assigned parameters
		String parameterName = "floating_ip_pool";
		String parameterValue = "to_change";
		Map<String, String> assignedParameters = new HashMap<>();
		assignedParameters.put(parameterName, parameterValue);
		Collection<DeploymentAssignedParameterResource> assignedParameterResources = new ArrayList<>();
		DeploymentAssignedParameterResource parameterResource = mock(DeploymentAssignedParameterResource.class);
		when(parameterResource.getParameterName()).thenReturn(parameterName);
		when(parameterResource.getParameterValue()).thenReturn(parameterValue);
		assignedParameterResources.add(parameterResource);
		when(input.getAssignedParameters()).thenReturn(assignedParameterResources);

		// assigned configuration
		String configurationName = "configName";
		String configurationUsername = "configUsername";
		when(input.getConfigurationName()).thenReturn(configurationName);
		when(input.getConfigurationAccountUsername()).thenReturn(configurationUsername);

		String cloudProviderParametersName = "cppName";
		CloudProviderParametersResource cloudProviderParametersResource = mock(CloudProviderParametersResource.class);
		when(cloudProviderParametersResource.getName()).thenReturn(cloudProviderParametersName);
		when(cloudProviderParametersResource.getAccountUsername()).thenReturn(principalName);

		//input.cloudProviderParameters = cloudProviderParametersResource;
		CloudProviderParameters selectedCloudProviderParameters = mock(CloudProviderParameters.class);
		when(cloudProviderParametersService.findByNameAndAccountUsername(Mockito.anyString(),
				Mockito.anyString())).thenReturn(selectedCloudProviderParameters);
		String cppReference = "someReference";
		selectedCloudProviderParameters.reference = cppReference;
		when(selectedCloudProviderParameters.getReference()).thenReturn(cppReference);
		when(cloudProviderParametersService.findByReference(cppReference)).thenReturn(selectedCloudProviderParameters);
		
		String cloudProvider = "ostack";
		selectedCloudProviderParameters.cloudProvider = cloudProvider;
		when(selectedCloudProviderParameters.getCloudProvider()).thenReturn(cloudProvider);
		
		Account account = mock(Account.class);
		selectedCloudProviderParameters.account = account;
		when(selectedCloudProviderParameters.getAccount()).thenReturn(account);
		when(accountService.findByUsername(principalName)).thenReturn(account);
		when(account.getGivenName()).thenReturn(principalName);
		when(account.getUsername()).thenReturn(principalName);
		when(input.getApplicationAccountUsername()).thenReturn(principalName);
		
		//input set cloudproviderparams copy
		
		CloudProviderParametersCopyResource cppCopyResource = mock(CloudProviderParametersCopyResource.class);
		when(input.getCloudProviderParametersCopy()).thenReturn(cppCopyResource);
		when(cppCopyResource.getCloudProviderParametersReference()).thenReturn(cppReference);
		
		
		CloudProviderParamsCopy cppCopy = mock(CloudProviderParamsCopy.class);
		cppCopy.cloudProviderParametersReference = cppReference;
		Collection<CloudProviderParamsCopyField> fields = new ArrayList();
		cppCopy.fields = fields;
		when(cppCopy.getFields()).thenReturn(fields);
		when(cloudProviderParametersCopyService.findByCloudProviderParametersReference(cppReference)).thenCallRealMethod();
		when(cloudProviderParametersCopyRepository.findByCloudProviderParametersReference(cppReference)).thenReturn(Optional.of(cppCopy));
		when(cppCopy.getAccount()).thenReturn(account);
		account.username = principalName;
		when(account.getUsername()).thenReturn(principalName);
		when(accountService.findByUsername(principalName)).thenReturn(account);
		when(accountService.findByEmail(input.getApplicationAccountUsername())).thenReturn(account);

		String applicationName = "appName";
		DeploymentApplication deploymentApplication = 
				mock(DeploymentApplication.class);
		when(deploymentApplication.getName()).thenReturn(applicationName);
		when(deploymentApplication.getAccount()).thenReturn(account);
		when(input.getApplicationName()).thenReturn(applicationName);

		Configuration config = mock(Configuration.class);
		String configName = "configName";
		given(configurationService.findByNameAndAccountEmail(input.getConfigurationName(),
				input.getConfigurationAccountUsername())).willReturn(config);
		ConfigurationDeploymentParameters configurationParameters = mock(ConfigurationDeploymentParameters.class);
		String configurationParameterName = "configurationParameterName";
		when(configurationParameters.getName()).thenReturn(configurationParameterName);
		when(deploymentParametersService.findByName(configurationParameterName)).thenReturn(configurationParameters);
		ConfigurationDeploymentParameter configurationParameter = mock(ConfigurationDeploymentParameter.class);
		when(configurationParameter.getKey()).thenReturn(parameterName);
		when(configurationParameter.getValue()).thenReturn(parameterValue);
		Set<ConfigurationDeploymentParameter> allParameters = new HashSet<>();
		allParameters.add(configurationParameter);
		when(configurationParameters.getConfigurationDeploymentParameter()).thenReturn(allParameters);
		when(config.getName()).thenReturn(configName);
		when(config.getAccount()).thenReturn(account);
		when(account.getEmail()).thenReturn(configurationUsername);

		
		Application application = mock(Application.class);
		when(application.getName()).thenReturn(applicationName);
		when(applicationService.findByAccountUsernameAndName(principalName, applicationName)).thenReturn(application);
		when(application.getAccount()).thenReturn(account);
		when(input.getApplicationName()).thenReturn(applicationName);
		when(applicationService.findByAccountUsernameAndName(principalName, applicationName)).thenReturn(application);
		
		Collection<ApplicationCloudProvider> acp = new ArrayList<>();
		when(application.getCloudProviders()).thenReturn(acp);
		application.cloudProviders = acp;
		
		when(input.getConfigurationName()).thenReturn(configurationName);
		when(input.getConfigurationAccountUsername()).thenReturn(configurationUsername);
		Long configId = 1L;
		String reference = "reference";
		Deployment deployment = mock(Deployment.class);
		when(configurationService.findByNameAndAccountUsername(configName, principalName)).thenReturn(config);
		given(deploymentService.save(isA(Deployment.class))).willCallRealMethod();
		given(deploymentRepository.save(isA(Deployment.class))).willReturn(deployment);
		when(deployment.getDeploymentApplication()).thenReturn(deploymentApplication);
		DeploymentConfiguration depConfiguration = mock(DeploymentConfiguration.class);
		when(depConfiguration.getName()).thenReturn("some name");
		when(deployment.getDeploymentConfiguration()).thenReturn(depConfiguration);
		when(deployment.getReference()).thenReturn(reference);
		when(deployment.getAccount()).thenReturn(account);
		ResponseEntity<?> addedDeployment = subject.addDeployment(principal, input);
		assertNotNull(addedDeployment.getBody());
		assertTrue(addedDeployment.getStatusCode().equals(HttpStatus.CREATED));
		assertTrue(application.cloudProviders.containsAll(deploymentApplication.getCloudProviders()));
		assertTrue(deployment.getDeploymentApplication().equals(deploymentApplication));
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
		when(mockDeploymentRepo.findByReference(reference)).thenReturn(Optional.of(mockDeployment));
		when(mockDeployment.getCloudProviderParametersReference()).thenReturn(cppReference);
		DeploymentConfiguration deploymentConfiguration = mock(DeploymentConfiguration.class);
		when(mockDeployment.getDeploymentConfiguration()).thenReturn(deploymentConfiguration);
		when(deploymentConfiguration.getName()).thenReturn("some string");
		when(deploymentConfiguration.getConfigDeploymentParametersReference()).thenReturn(reference);
		when(mockCloudCredentialsRepo.findByReference(Mockito.anyString())).thenReturn(Optional.of(cppMock));
		when(cppMock.getAccount()).thenReturn(mockAccount);
		java.sql.Date date = mock(java.sql.Date.class);
		when(mockAccount.getFirstJoinedDate()).thenReturn(date);
		when(mockAccount.getReference()).thenReturn("some ref");
		when(mockAccount.getGivenName()).thenReturn("given name");
		ConfigurationDeploymentParameters cdps = mock(ConfigurationDeploymentParameters.class);
		when(deploymentParametersService.findByReference(reference)).thenReturn(cdps);
		when(deploymentParametersRepository.findByReference(reference)).thenReturn(Optional.of(cdps));
		when(cdps.getReference()).thenReturn(reference);
		return mockDeployment;
	}

}
