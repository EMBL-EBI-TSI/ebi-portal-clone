package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.data.util.ReflectionUtils;
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
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParametersRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopy;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyField;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParametersNotFoundException;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParametersService;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParamsCopyService;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParametersRepository;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationRepository;
import uk.ac.ebi.tsc.portal.api.deployment.controller.DeploymentRestController;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentStatusRepository;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDownloaderException;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class CloudProviderParameterRestControllerTest {

	CloudProviderParametersRestController subject;
	CloudProviderParametersRepository cppRepoMock;
	AccountRepository accountRepoMock;
	CloudProviderParametersResource cppResourceMock;
	SharedCloudProviderParametersResource sharedCppResourceMock;
	Account accountMock;
	Principal principalMock;
	CloudProviderParameters cppMock;
	DomainService domainService;
	uk.ac.ebi.tsc.aap.client.security.TokenHandler tokenHandler;
	HttpServletRequest request;
	CloudProviderParametersService cppService;
	CloudProviderParamsCopyService cppCopyService;
	AccountService accountService;
	User user;
	EncryptionService encryptionService;

	String userName = "userName";
	String cppName = "cppName";
	String cloudProvider = "OSTACK";
	String userEmail = "test@ebi.ac.uk";
	String tokenArray = "some token";
	String token = "token" ;
    String salt= "salt";
    String password= "password";


	DeploymentStatusRepository deploymentStatusRepository;
	DeploymentRepository deploymentRepository;
	DeploymentRestController deploymentRestController;
	ConfigurationRepository configurationRepository;
	ConfigurationDeploymentParametersRepository cdpRepository;
	CloudProviderParamsCopyRepository cloudProviderParametersCopyRepository;
	
	@Before
	public void setUp(){

		cppRepoMock = mock(CloudProviderParametersRepository.class);
		accountRepoMock = mock(AccountRepository.class);
		accountMock = mock(Account.class);
		principalMock = mock(Principal.class);
		cppMock = mock(CloudProviderParameters.class);
		cppResourceMock = mock(CloudProviderParametersResource.class);
		sharedCppResourceMock = mock(SharedCloudProviderParametersResource.class);
		domainService = mock(DomainService.class);
		cppService = mock(CloudProviderParametersService.class);
		accountService = mock(AccountService.class);
		tokenHandler = mock(uk.ac.ebi.tsc.aap.client.security.TokenHandler.class);
		request = mock(HttpServletRequest.class);
		deploymentStatusRepository = mock(DeploymentStatusRepository.class);
		deploymentRestController = mock(DeploymentRestController.class);
		configurationRepository = mock(ConfigurationRepository.class);
		cdpRepository = mock(ConfigurationDeploymentParametersRepository .class);
		cloudProviderParametersCopyRepository = mock(CloudProviderParamsCopyRepository.class);
		deploymentRepository = mock(DeploymentRepository.class);
		cppCopyService = mock(CloudProviderParamsCopyService.class);
		encryptionService = mock(EncryptionService.class);
		accountMock();
		accountRepoMock();
		principalMock();
		cppMock();
		cppRepoMock();
	}
	
	/**
	 * Test if user can get his/her list of CPP 
	 */

	@Test
	public void testGetCurrentUserCredentials() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeySpecException, IOException{

		Resources<CloudProviderParametersResource> resources = subject.getCurrentUserCredentials(principalMock);
		assertNotNull(resources);
	}

	/**
	 * Test if user can get a CPP associated to his/her account by its name 
	 */
	@Test
	public void testGetByName() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeySpecException, IOException{

		CloudProviderParametersResource cppResource = subject.getByName(principalMock, cppName);
		assertNotNull(cppResource);

	}

	/**
	 * Test if user gets exception if he/she tries to get CPP without passing a CPP name
	 */
	@Test(expected = NullPointerException.class ) 
	public void testGetByNameNullCppName() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeySpecException, IOException{

		subject.getByName(principalMock, null);
	}

	/**
	 * Test if user gets exception if he/she tries to get CPP passing empty CPP name
	 */
	@Test(expected = NullPointerException.class ) 
	public void testGetByNameEmptyCppName() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeySpecException, IOException{

		subject.getByName(principalMock, "");
	}

	/**
	 * Test if user can add a CPP to his/her account
	*/
	@Test
	public void addCloudCredential() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException{

		cppResourceMock();
		cppMock();
		ReflectionTestUtils.setField(subject, "cloudProviderParametersService", cppService);
		when(cppService.save(Mockito.any(CloudProviderParameters.class))).thenReturn(cppMock);
		ResponseEntity<?> response = subject.add(principalMock, cppResourceMock);

		assertEquals(response.getStatusCode(), HttpStatus.OK);
	}
	
	/**
	 * Test if throws exception on invalid name
	*/
	@Test(expected =  InvalidCloudProviderParametersInputException.class)
	public void addCloudCredentialInvalidName() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException{
		String cppName = "\\.\\.\\-";
		when(cppResourceMock.getCloudProvider()).thenReturn(cloudProvider);
		when(cppResourceMock.getName()).thenReturn(cppName);
		ResponseEntity<?> response = subject.add(principalMock, cppResourceMock);
	}

	/**
	 * Test if user can delete a CPP by its name from his/her account
	 * @throws Exception 
	 */
	@Test
	public void deleteCloudCredential() throws Exception{
		ReflectionTestUtils.setField(subject, "cloudProviderParametersCopyService", cppCopyService);
		ReflectionTestUtils.setField( cppCopyService, "cloudProviderParametersCopyRepository", cloudProviderParametersCopyRepository);
		CloudProviderParamsCopy cppCopyMock = mock(CloudProviderParamsCopy.class);
		when(cppCopyMock.getAccount()).thenReturn(accountMock);
		when(cloudProviderParametersCopyRepository.findByCloudProviderParametersReference(Mockito.anyString())).thenReturn(Optional.of(cppCopyMock));
		ResponseEntity<?> response = subject.delete(principalMock, cppName);
		assertEquals(response.getStatusCode(), HttpStatus.OK);
	}

	/**
	 * test if a user can list all his own applications
	 */
	@Test
	public void can_list_owned_applications() throws InvalidKeyException, InvalidAlgorithmParameterException, 
	NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, 
	IllegalBlockSizeException, IOException{

		ReflectionTestUtils.setField(subject, "cloudProviderParametersService", cppService);
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		String username = "username";
		when(principalMock.getName()).thenReturn(username);
		when(accountMock.getUsername()).thenReturn(username);
		when(accountService.findByUsername(username)).thenReturn(accountMock);
		CloudProviderParameters accountMockCpp = mock(CloudProviderParameters.class);
		Set<CloudProviderParameters> accountCpp = new HashSet<>();
		accountCpp.add(accountMockCpp);
		when(cppRepoMock.findByAccountUsername(username)).thenReturn(accountCpp);
		when(cppService.findByAccountUsername(username)).thenReturn(accountCpp);
		getCppResoure(accountMockCpp);
		Resources<CloudProviderParametersResource> cppList = subject.getCurrentUserCredentials(principalMock);
		assertNotNull(cppList);
		assertEquals(1, cppList.getContent().size());
	}

	/**
	 * test if a user can see the list of application belonging to the
	 * teams in which he is a member
	 */
	@Test public void
	can_list_all_shared_cpp_for_account() throws IOException, ApplicationDownloaderException{

		String username = "username";
		ReflectionTestUtils.setField(subject, "cloudProviderParametersService", cppService);
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		ReflectionTestUtils.setField(subject, "tokenHandler", tokenHandler);
		getRequest();
		when(accountService.findByUsername(username)).thenReturn(accountMock);
		when(accountRepoMock.findByUsername(username)).thenReturn(Optional.of(accountMock));
		Set<CloudProviderParameters> cpps = new HashSet<>();
		CloudProviderParameters cppMock = mock(CloudProviderParameters.class);
		cpps.add(cppMock);
		when(principalMock.getName()).thenReturn(username);
		getCppResoure(cppMock);
		when(cppService.getSharedCppsByAccount(Mockito.any(Account.class), Mockito.anyString(), Mockito.any(User.class))).thenReturn(cpps);
		Resources<CloudProviderParametersResource> cppList = subject.getSharedCredentialsByAccount(request, principalMock);
		assertNotNull(cppList);
		assertEquals(1, cppList.getContent().size());
	}
	
	
	@Test public void
	can_list_all_shared_cpp_for_account_fail() throws IOException, ApplicationDownloaderException{

		String username = "username";
		ReflectionTestUtils.setField(subject, "cloudProviderParametersService", cppService);
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		ReflectionTestUtils.setField(subject, "tokenHandler", tokenHandler);
		getRequest();
		when(accountService.findByUsername(username)).thenReturn(accountMock);
		when(accountRepoMock.findByUsername(username)).thenReturn(Optional.of(accountMock));
		Set<CloudProviderParameters> cpps = new HashSet<>();
		CloudProviderParameters cppMock = mock(CloudProviderParameters.class);
		when(principalMock.getName()).thenReturn(username);
		getCppResoure(cppMock);
		when(cppService.getSharedCppsByAccount(Mockito.any(Account.class), Mockito.anyString(), Mockito.any(User.class))).thenReturn(new HashSet<>());
		Resources<CloudProviderParametersResource> cppList = subject.getSharedCredentialsByAccount(request, principalMock);
		assertNotNull(cppList);
		assertEquals(0, cppList.getContent().size());
	}
	
	@Test(expected = CloudProviderParametersNotFoundException.class)
	public void
	can_get_shared_cpp_by_name_fail() throws IOException, ApplicationDownloaderException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeySpecException{

		String username = "username";
		ReflectionTestUtils.setField(subject, "cloudProviderParametersService", cppService);
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		ReflectionTestUtils.setField(subject, "tokenHandler", tokenHandler);
		getRequest();
		when(accountService.findByUsername(username)).thenReturn(accountMock);
		when(accountRepoMock.findByUsername(username)).thenReturn(Optional.of(accountMock));
		CloudProviderParameters cppMock = mock(CloudProviderParameters.class);
		when(principalMock.getName()).thenReturn(username);
		getCppResoure(cppMock);
		when(cppService.getSharedCppsByCppName(Mockito.any(Account.class), Mockito.anyString(), Mockito.any(User.class), Mockito.anyString()
				)).thenReturn(null);
		CloudProviderParametersResource cppReturned = subject.getSharedByName(request, principalMock, cppName);
	}
	
	
	@Test public void
	can_get_shared_cpp_by_name_pass()throws IOException, ApplicationDownloaderException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeySpecException{

		String username = "username";
		ReflectionTestUtils.setField(subject, "cloudProviderParametersService", cppService);
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		ReflectionTestUtils.setField(subject, "tokenHandler", tokenHandler);
		getRequest();
		when(accountService.findByUsername(username)).thenReturn(accountMock);
		when(accountRepoMock.findByUsername(username)).thenReturn(Optional.of(accountMock));
		cppMock();
		when(principalMock.getName()).thenReturn(username);
		getCppResoure(cppMock);
		when(cppService.getSharedCppsByCppName(Mockito.any(Account.class), Mockito.anyString(), Mockito.any(User.class), Mockito.anyString()
				)).thenReturn(cppMock);
		when(cppService.findByNameAndAccountUsername(Mockito.anyString(), Mockito.anyString())).thenReturn(cppMock);
		CloudProviderParametersResource cppReturned = subject.getSharedByName(request, principalMock, cppName);
		assertTrue(cppReturned.getName().equals(cppName));
	}
	
	/**
	 * test if a user does not get a NPE if he 
	 * is not a member of any team and hence no shared credentials
	 */
	@Test public void
	can_get_not_null_when_account_not_member_of_any_team() throws IOException, ApplicationDownloaderException{

		String username = "username";
		ReflectionTestUtils.setField(subject, "cloudProviderParametersService", cppService);
		ReflectionTestUtils.setField(subject, "accountService", accountService);

		when(accountService.findByUsername(username)).thenReturn(accountMock);
		when(accountRepoMock.findByUsername(username)).thenReturn(Optional.of(accountMock));
		when(accountMock.getMemberOfTeams()).thenReturn(new HashSet<>()) ; 
		when(principalMock.getName()).thenReturn(username);
		getCppResoure(cppMock);
		getRequest();
		Resources<CloudProviderParametersResource> cppList = subject.getSharedCredentialsByAccount(request, principalMock);
		assertNotNull(cppList);
		assertEquals(0, cppList.getContent().size());
	}
	
	/**
	 * test if a user does not get a NPE if he 
	 * has no credentials
	 */
	@Test public void
	can_get_not_null_when_account_has_no_owned_applications() throws IOException, ApplicationDownloaderException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException{

		ReflectionTestUtils.setField(subject, "cloudProviderParametersService", cppService);
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		String username = "username";
		when(principalMock.getName()).thenReturn(username);
		when(accountMock.getUsername()).thenReturn(username);
		when(accountService.findByUsername(username)).thenReturn(accountMock);
		CloudProviderParameters accountMockCpp = mock(CloudProviderParameters.class);
		when(cppRepoMock.findByAccountUsername(username)).thenReturn(new HashSet<>());
		when(cppService.findByAccountUsername(username)).thenReturn(new HashSet<>());
		getCppResoure(accountMockCpp);
		Resources<CloudProviderParametersResource> cppList = subject.getCurrentUserCredentials(principalMock);
		assertNotNull(cppList);
		assertEquals(0, cppList.getContent().size());
	}
	
	@Test
	public void can_update() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException{
		
		ReflectionTestUtils.setField(subject, "cloudProviderParametersService", cppService);
		ReflectionTestUtils.setField(cppService, "cloudProviderParametersRepository", cppRepoMock);
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		ReflectionTestUtils.setField(subject, "cloudProviderParametersCopyService", cppCopyService);
		ReflectionTestUtils.setField( cppCopyService, "cloudProviderParametersCopyRepository", cloudProviderParametersCopyRepository);
		ReflectionTestUtils.setField(cppService, "encryptionService", encryptionService);
		ReflectionTestUtils.setField(cppCopyService, "encryptionService", encryptionService);
		cppResourceMock();cppMock();
		String username = "username";
		when(principalMock.getName()).thenReturn(username);
		when(accountMock.getUsername()).thenReturn(username);
		when(accountService.findByUsername(username)).thenReturn(accountMock);
		when(cppRepoMock.findByNameAndAccountUsername(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(cppMock));
		when(cppService.findByNameAndAccountUsername(Mockito.anyString(), Mockito.anyString())).thenCallRealMethod();
		Answer<CloudProviderParameters> cppAnswer = new Answer<CloudProviderParameters>(){
			@Override
			public CloudProviderParameters answer(InvocationOnMock invocation) throws Throwable {
				return cppMock;
			}
		};
		cppMock.account = accountMock;
		when(cppService.updateFields(Mockito.any(CloudProviderParameters.class),Mockito.any(CloudProviderParamsCopy.class), Mockito.any(CloudProviderParametersResource.class), Mockito.anyString())).thenAnswer(cppAnswer);
		when(cppCopyService.findByCloudProviderParametersReference(Mockito.anyString())).thenCallRealMethod();
		CloudProviderParamsCopy cppCopyMock = mock(CloudProviderParamsCopy.class);
		when(cppCopyMock.getAccount()).thenReturn(accountMock);
		Collection<CloudProviderParamsCopyField> fields = new ArrayList();
		cppCopyMock.fields = fields;
		when(cppCopyMock.getFields()).thenReturn(fields);
		when(cloudProviderParametersCopyRepository.findByCloudProviderParametersReference(Mockito.anyString())).thenReturn(Optional.of(cppCopyMock));
		ResponseEntity<?> response = subject.update(principalMock, cppResourceMock, cppName);
		assertEquals(response.getStatusCode(), HttpStatus.OK);
	}
	
	@Test
	public void valid_namecan_update() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException{
		
		ReflectionTestUtils.setField(subject, "cloudProviderParametersService", cppService);
		ReflectionTestUtils.setField(cppService, "cloudProviderParametersRepository", cppRepoMock);
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		ReflectionTestUtils.setField(subject, "cloudProviderParametersCopyService", cppCopyService);
		ReflectionTestUtils.setField( cppCopyService, "cloudProviderParametersCopyRepository", cloudProviderParametersCopyRepository);
		ReflectionTestUtils.setField(cppCopyService, "encryptionService", encryptionService);
		ReflectionTestUtils.setField(cppService, "encryptionService", encryptionService);
		cppResourceMock();cppMock();
		String username = "username";
		when(principalMock.getName()).thenReturn(username);
		when(accountMock.getUsername()).thenReturn(username);
		when(accountService.findByUsername(username)).thenReturn(accountMock);
		when(cppRepoMock.findByNameAndAccountUsername(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(cppMock));
		when(cppService.findByNameAndAccountUsername(Mockito.anyString(), Mockito.anyString())).thenCallRealMethod();
		Answer<CloudProviderParameters> cppAnswer = new Answer<CloudProviderParameters>(){
			@Override
			public CloudProviderParameters answer(InvocationOnMock invocation) throws Throwable {
				return cppMock;
			}
		};
		cppMock.account = accountMock;
		when(cppService.updateFields(Mockito.any(CloudProviderParameters.class), Mockito.any(CloudProviderParamsCopy.class), Mockito.any(CloudProviderParametersResource.class), Mockito.anyString())).thenAnswer(cppAnswer);
		CloudProviderParamsCopy cppCopyMock = mock(CloudProviderParamsCopy.class);
		when(cppCopyMock.getAccount()).thenReturn(accountMock);
		when(cloudProviderParametersCopyRepository.findByCloudProviderParametersReference(Mockito.anyString())).thenReturn(Optional.of(cppCopyMock));
		ResponseEntity<?> response = subject.update(principalMock, cppResourceMock, cppName);
		assertEquals(response.getStatusCode(), HttpStatus.OK);
	}
	private void getCppResoure(CloudProviderParameters cpp){
		cpp.account = accountMock;
		when(cpp.getAccount()).thenReturn(accountMock);
	}

	private void accountMock(){
		when(this.accountMock.getId()).thenReturn(1L);
		when(this.accountMock.getUsername()).thenReturn(userName);
		when(this.accountMock.getEmail()).thenReturn("an@email.com");
		when(this.accountMock.getPassword()).thenReturn("A password");
		when(this.accountMock.getOrganisation()).thenReturn("An organisation");
	}

	private void accountRepoMock(){
		List<Account> accounts = new ArrayList<>();
		accounts.add(accountMock);
		given(accountRepoMock.findByEmail(userEmail)).willReturn(accounts);
		
		when(accountRepoMock.findByUsername(userName)).thenReturn(Optional.of(accountMock));
		when(accountRepoMock.findByEmail(userEmail)).thenReturn(accounts);
		when(accountRepoMock.save(accountMock)).thenReturn(accountMock);
	}
	private void principalMock(){
		when(principalMock.getName()).thenReturn(userName);
	}

	private void cppMock(){
		when(cppMock.getAccount()).thenReturn(accountMock);
		when(cppMock.getName()).thenReturn(cppName);
		when(cppMock.getCloudProvider()).thenReturn(cloudProvider);
		when(cppMock.getId()).thenReturn(1L);
	}

	private void cppRepoMock(){
		when(cppRepoMock.findByNameAndAccountUsername(cppName, userName)).thenReturn(Optional.of(cppMock));
	}

	private void cppResourceMock(){
		when(cppResourceMock.getCloudProvider()).thenReturn(cloudProvider);
		when(cppResourceMock.getName()).thenReturn(cppName);
	}

	private void sharedCppResourceMock(){
		when(sharedCppResourceMock.getCloudProviderParameterName()).thenReturn(cppName);
		when(sharedCppResourceMock.getUserEmail()).thenReturn(userEmail);
	}

	private Set<CloudProviderParameters> accountWithCloudeProviderParameter(){
		CloudProviderParameters cpp = new CloudProviderParameters(cppName, "AWS", accountMock);
		Set<CloudProviderParameters> set = new HashSet<>();
		set.add(cpp);
		return set;
	}
	
	private void getRequest(){
		given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn(tokenArray);
	}
}
