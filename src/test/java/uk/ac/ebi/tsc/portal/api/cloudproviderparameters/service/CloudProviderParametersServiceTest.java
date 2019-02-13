package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service;

import static org.junit.Assert.assertEquals;
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
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import uk.ac.ebi.tsc.aap.client.model.Domain;
import uk.ac.ebi.tsc.aap.client.model.User;
import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.application.repo.Application;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller.CloudProviderParametersFieldResource;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller.CloudProviderParametersResource;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParametersField;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParametersRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopy;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyField;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;
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
public class CloudProviderParametersServiceTest {
	
	@MockBean
	private CloudProviderParametersService cloudProviderParametersService;

	@MockBean
	private CloudProviderParametersRepository cloudProviderParametersRepository;

	@MockBean
	private CloudProviderParameters cpp;
	
	@MockBean
	private Account account;
	
	@MockBean
	private User user;
	
	@MockBean
	private HttpServletRequest request;
	
	@MockBean
	private TeamResource teamResource;
	
	@MockBean
	private Principal principalMock;
	
	@MockBean
	private Team team;
	
	@MockBean
	private Domain domain;
	
	@MockBean
	private CloudProviderParamsCopyService cloudProviderParamsCopyService;

	@MockBean
	private CloudProviderParamsCopyRepository cloudProviderParamsCopyRepository;
	
	@MockBean
	private DomainService domainService;
	
	@MockBean
	private EncryptionService encryptionService;
	

	String tokenArray = "some token";
	String token = "token" ;
	String username = "username";
	String cppName = "cppName";
	String appnotpresent = "appnotpresent";
	String teamName = "teamName";
	String userEmail = "userEmail";
	String userName = "username";
    String salt= "salt";
    String password= "password";
	
	@Before
	public void setUp(){
		
		when(this.principalMock.getName()).thenReturn("A user name");
		when(user.getEmail()).thenReturn(userEmail);
		when(this.account.getId()).thenReturn(1L);
		when(this.account.getUsername()).thenReturn("A user name");
		when(this.account.getEmail()).thenReturn("an@email.com");
		when(this.account.getPassword()).thenReturn("A password");
		when(this.account.getOrganisation()).thenReturn("An organisation");
		ReflectionTestUtils.setField(cloudProviderParametersService, "cloudProviderParametersRepository", cloudProviderParametersRepository);
		ReflectionTestUtils.setField(cloudProviderParametersService, "domainService", domainService);
		ReflectionTestUtils.setField(cloudProviderParametersService, "cloudProviderParametersCopyService", cloudProviderParamsCopyService);
		ReflectionTestUtils.setField(cloudProviderParamsCopyService, "cloudProviderParametersCopyRepository", cloudProviderParamsCopyRepository);
		ReflectionTestUtils.setField(cloudProviderParamsCopyService, "encryptionService", encryptionService);
		ReflectionTestUtils.setField(cloudProviderParametersService, "encryptionService", encryptionService);
	}
	
	/**
	 * If user has set cloud provider parameters they are returned
	 */
	@Test
	public void testFindByAccountUsername() throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException, IOException{
		List<CloudProviderParameters> cppCollection = new ArrayList<>();
		given(cpp.getAccount()).willReturn(account);
		cppCollection.add(cpp);
		assertTrue(cppCollection.size()==1);
		given(cloudProviderParametersRepository.findByAccountUsername(userName)).willReturn(cppCollection);
		given(cloudProviderParametersService.findByAccountUsername(userName)).willCallRealMethod();
		Map<String, String> decryptedValues = new HashMap<>();
		given(encryptionService.decryptOne(cpp)).willReturn(decryptedValues);
		Collection<CloudProviderParameters> cpps = cloudProviderParametersService.findByAccountUsername(userName);
		assertTrue(cpps.size()==1);
	}
	
	/**
	 * If user account has no cloud provider parameters set, them
	 * NullPointerException is thrown
	 */
	@Test(expected =  NullPointerException.class)
	public void testFindByAccountUsernameThrowsException() throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException, IOException{
		given(cloudProviderParametersRepository.findByAccountUsername(userName)).willThrow(NullPointerException.class);
		given(cloudProviderParametersService.findByAccountUsername(userName)).willCallRealMethod();
		cloudProviderParametersService.findByAccountUsername(userName);
	}
	
	@Test
	public void testFindByAccountUsernameAndCppName() throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException, IOException{
		given(cpp.getName()).willReturn(cppName);
		given(cloudProviderParametersRepository.findByCloudProviderAndAccountUsername(cppName, userName)).willReturn(getCloudProviderParameters());
		given(cloudProviderParametersService.findByCloudProviderAndAccountUsername(cppName, userName)).willCallRealMethod();
		given(cpp.getAccount()).willReturn(account);
		Collection<CloudProviderParameters> cpps = cloudProviderParametersService.findByCloudProviderAndAccountUsername(cppName, userName);
		assertTrue(cpps.size()==1);
	}
	
	@Test(expected =  NullPointerException.class)
	public void testFindByAccountUsernameAndCppNameThrowsException() throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException, IOException{
		given(cpp.getName()).willReturn(cppName);
		given(cloudProviderParametersRepository.findByCloudProviderAndAccountUsername(cppName, userName)).willThrow(NullPointerException.class);
		given(cloudProviderParametersService.findByCloudProviderAndAccountUsername(cppName, userName)).willCallRealMethod();
		cloudProviderParametersService.findByCloudProviderAndAccountUsername(cppName, userName);
	}
	
	@Test
	public void testFindByNameAndAccountUsername(){
		given(cpp.getName()).willReturn(cppName);
		given(cloudProviderParametersRepository.findByNameAndAccountUsername(cppName, userName)).willReturn(Optional.of(cpp));
		given(cloudProviderParametersService.findByNameAndAccountUsername(cppName, userName)).willCallRealMethod();
		given(cpp.getAccount()).willReturn(account);
		CloudProviderParameters cppReturned = cloudProviderParametersService.findByNameAndAccountUsername(cppName, userName);
		assertTrue(cppReturned.getName().equals(cppName));
	}
	
	/**
	 * if cloud provider could not be found by it name and account user name
	 * throw 'CloudProviderParametersNotFoundException'
	 */
	@Test(expected = CloudProviderParametersNotFoundException.class)
	public void testFindByNameAndAccountUsernameThrowsNotFoundException(){
		given(cpp.getName()).willReturn(cppName);
		given(cloudProviderParametersRepository.findByNameAndAccountUsername(cppName, userName)).willThrow(CloudProviderParametersNotFoundException.class);
		given(cloudProviderParametersService.findByNameAndAccountUsername(cppName, userName)).willCallRealMethod();
		given(cpp.getAccount()).willReturn(account);
		cloudProviderParametersService.findByNameAndAccountUsername(cppName, userName);
	}
	
	@Test
	public void testSave() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException{
		given(cloudProviderParametersRepository.save(cpp)).willReturn(cpp);
		given(cloudProviderParametersService.save(cpp)).willCallRealMethod();
		given(cpp.getAccount()).willReturn(account);
		CloudProviderParameters cppSaved = cloudProviderParametersService.save(cpp);
		assertEquals(cppSaved, cpp);
	}
	
	/**
	 * when domain returning a list of users
	 */
	@Test
	public void testGetSharedCppsByAccountPass(){
		
		getTeamResource(team);
		getDomain();
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		Set<CloudProviderParameters> cpps = new HashSet<>();
		when(cpp.getName()).thenReturn(cppName);
		cpps.add(cpp);
		team.setCppBelongingToTeam(cpps);
		account.setMemberOfTeams(teams);
		when(account.getMemberOfTeams()).thenReturn(teams);
		List<User> users = new ArrayList<>();
		users.add(user);
		when(domainService.getAllUsersFromDomain(Mockito.anyString(), Mockito.anyString())).thenReturn(users);
		Answer teamAnswer = new Answer<Set<Team>>(){
			@Override
			public Set<Team> answer(InvocationOnMock invocation){
				return teams;
			}};
		when(account.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
		when(cloudProviderParametersService.getSharedCppsByAccount(account, token, user)).thenCallRealMethod();
		Set<CloudProviderParameters> returnedCpps = cloudProviderParametersService.getSharedCppsByAccount(account, token, user);
		assertTrue(returnedCpps.size() == 1);
	}
	
	@Test
	public void testGetSharedCppsByAccountNoDomainPass(){
		
		getTeamResourceNoDomain(team);
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		Set<CloudProviderParameters> cpps = new HashSet<>();
		when(cpp.getName()).thenReturn(cppName);
		cpps.add(cpp);
		team.setCppBelongingToTeam(cpps);
		account.setMemberOfTeams(teams);
		when(account.getMemberOfTeams()).thenReturn(teams);
		Answer teamAnswer = new Answer<Set<Team>>(){
			@Override
			public Set<Team> answer(InvocationOnMock invocation){
				return teams;
			}};
		when(account.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
		when(cloudProviderParametersService.getSharedCppsByAccount(account, token, user)).thenCallRealMethod();
		Set<CloudProviderParameters> returnedCpps = cloudProviderParametersService.getSharedCppsByAccount(account, token, user);
		assertTrue(returnedCpps.size() == 1);
	}
	
	/**
	 * when domain does not return a list of users
	 */
	@Test
	public void testGetSharedCppByAccountFail(){
		
		getTeamResource(team);
		getDomain();
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		Set<CloudProviderParameters> cpps = new HashSet<>();
		when(cpp.getName()).thenReturn(cppName);
		cpps.add(cpp);
		team.setCppBelongingToTeam(cpps);
		account.setMemberOfTeams(teams);
		when(account.getMemberOfTeams()).thenReturn(teams);
		List<User> users = new ArrayList<>();
		users.add(user);
		when(domainService.getAllUsersFromDomain(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
		Answer teamAnswer = new Answer<Set<Team>>(){
			@Override
			public Set<Team> answer(InvocationOnMock invocation){
				return teams;
			}};
		when(account.getMemberOfTeams()).thenAnswer(teamAnswer);
		when(cloudProviderParametersService.getSharedCppsByAccount(account, token, user)).thenCallRealMethod();
		Set<CloudProviderParameters> returnedCpps = cloudProviderParametersService.getSharedCppsByAccount(account, token, user);
		assertTrue(returnedCpps.size() == 0);
	}
	
	/**
	 * domain returning list of users
	 */
	@Test
	public void testGetSharedCppByNamePass(){
		
		getTeamResource(team);
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		getDomain();
		Set<CloudProviderParameters> cpps = new HashSet<>();
		when(cpp.getName()).thenReturn(cppName);
		cpps.add(cpp);
		team.setCppBelongingToTeam(cpps);
		account.setMemberOfTeams(teams);
		when(account.getMemberOfTeams()).thenReturn(teams);
		List<User> users = new ArrayList<>();
		users.add(user);
		when(domainService.getAllUsersFromDomain(Mockito.anyString(), Mockito.anyString())).thenReturn(users);
		Answer teamAnswer = new Answer<Set<Team>>(){
			@Override
			public Set<Team> answer(InvocationOnMock invocation){
				return teams;
			}};
		when(account.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
		when(cloudProviderParametersService.getSharedCppsByCppName(account, token, user, cppName)).thenCallRealMethod();
		CloudProviderParameters credential = cloudProviderParametersService.getSharedCppsByCppName(account, token, user, cppName);
		assertTrue(credential.getName().equals(cppName));
	}
	
	@Test
	public void testGetSharedCppByNamePassNoDomain(){
		
		getTeamResourceNoDomain(team);
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		Set<CloudProviderParameters> cpps = new HashSet<>();
		when(cpp.getName()).thenReturn(cppName);
		cpps.add(cpp);
		team.setCppBelongingToTeam(cpps);
		account.setMemberOfTeams(teams);
		when(account.getMemberOfTeams()).thenReturn(teams);
		Answer teamAnswer = new Answer<Set<Team>>(){
			@Override
			public Set<Team> answer(InvocationOnMock invocation){
				return teams;
			}};
		when(account.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
		when(cloudProviderParametersService.getSharedCppsByCppName(account, token, user, cppName)).thenCallRealMethod();
		CloudProviderParameters credential = cloudProviderParametersService.getSharedCppsByCppName(account, token, user, cppName);
		assertTrue(credential.getName().equals(cppName));
	}
	
	/**
	 * domain not returning list of users, throwing exception
	 */
	@Test
	public void testGetSharedApplicationByNameFail(){
		
		getTeamResource(team);
		getDomain();
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		getDomain();
		Set<CloudProviderParameters> cpps = new HashSet<>();
		when(cpp.getName()).thenReturn(cppName);
		cpps.add(cpp);
		team.setCppBelongingToTeam(cpps);
		account.setMemberOfTeams(teams);
		when(account.getMemberOfTeams()).thenReturn(teams);
		List<User> users = new ArrayList<>();
		users.add(user);
		when(domainService.getAllUsersFromDomain(Mockito.anyString(), Mockito.anyString())).thenThrow(Exception.class);
		Answer teamAnswer = new Answer<Set<Team>>(){
			@Override
			public Set<Team> answer(InvocationOnMock invocation){
				return teams;
			}};
		when(account.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
		Set<Application>  a = team.getApplicationsBelongingToTeam();
		when(cloudProviderParametersService.getSharedCppsByAccount(account, token, user)).thenCallRealMethod();
		CloudProviderParameters credential = cloudProviderParametersService.getSharedCppsByCppName(account, token, user, cppName);
		assertTrue(credential == null);
	}
	
	@Test
	public void testUpdateCPPCheckUpdated() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException{
		String key = "key";String value = "value";
		CloudProviderParameters cpp = Mockito.mock(CloudProviderParameters.class);
		CloudProviderParametersResource input = Mockito.mock(CloudProviderParametersResource.class);
		CloudProviderParametersField field = mock(CloudProviderParametersField.class);
		given(field.getKey()).willReturn(key);given(field.getValue()).willReturn(value);
		Collection<CloudProviderParametersField> cppFields = new ArrayList<>();
		cppFields.add(field);
		given(cpp.getFields()).willReturn(cppFields);
		cpp.fields = cppFields;
		
		CloudProviderParametersFieldResource fieldResource = mock(CloudProviderParametersFieldResource.class);
		given(fieldResource.getKey()).willReturn(key);given(fieldResource.getValue()).willReturn(value);
		Collection<CloudProviderParametersFieldResource> fieldResources = new ArrayList<>();
		fieldResources.add(fieldResource);
		given(input.getFields()).willReturn(fieldResources);
		given(cloudProviderParametersService.save(cpp)).willReturn(cpp);
		
		CloudProviderParamsCopy cppCopy = Mockito.mock(CloudProviderParamsCopy.class);
		
		CloudProviderParamsCopyField copyField = mock(CloudProviderParamsCopyField.class);
		given(field.getKey()).willReturn(key);given(field.getValue()).willReturn(value);
		Collection<CloudProviderParamsCopyField> cppCopyFields = new ArrayList<>();
		cppCopyFields.add(copyField);
		given(cppCopy.getFields()).willReturn(cppCopyFields);
		cppCopy.fields = cppCopyFields;
		
		given(cloudProviderParamsCopyService.save(cppCopy)).willReturn(cppCopy);
		getAccount();
		given(cppCopy.getAccount()).willReturn(account);
		
		given(cloudProviderParametersService.updateFields(cpp, cppCopy, input, userName)).willCallRealMethod();
		CloudProviderParameters credential = cloudProviderParametersService.updateFields(cpp,cppCopy,  input, userName);
		assertTrue(credential != null);
		assertTrue(credential.getFields().size() == 1);
	}
	
	@Test
	public void testUpdateCPPCheckRemoved() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException{
		String key = "key";String value = "value";
		String secondKey = "secondKey";String secondValue = "secondValue";
		CloudProviderParameters cpp = Mockito.mock(CloudProviderParameters.class);
		CloudProviderParametersResource input = Mockito.mock(CloudProviderParametersResource.class);
		CloudProviderParametersField field = mock(CloudProviderParametersField.class);
		given(field.getKey()).willReturn(key);given(field.getValue()).willReturn(value);
		CloudProviderParametersField secondField = mock(CloudProviderParametersField.class);
		given(secondField.getKey()).willReturn(secondKey);given(secondField.getValue()).willReturn(secondValue);
		Collection<CloudProviderParametersField> cppFields = new ArrayList<>();
		cppFields.add(field);cppFields.add(secondField);
		given(cpp.getFields()).willReturn(cppFields);
		cpp.fields = cppFields;
		
		CloudProviderParametersFieldResource fieldResource = mock(CloudProviderParametersFieldResource.class);
		given(fieldResource.getKey()).willReturn(key);given(fieldResource.getValue()).willReturn(value);
		Collection<CloudProviderParametersFieldResource> fieldResources = new ArrayList<>();
		fieldResources.add(fieldResource);
		given(input.getFields()).willReturn(fieldResources);
		given(cloudProviderParametersService.save(cpp)).willReturn(cpp);
		
		CloudProviderParamsCopy cppCopy = Mockito.mock(CloudProviderParamsCopy.class);
		
		CloudProviderParamsCopyField copyField = mock(CloudProviderParamsCopyField.class);
		given(field.getKey()).willReturn(key);given(field.getValue()).willReturn(value);
		Collection<CloudProviderParamsCopyField> cppCopyFields = new ArrayList<>();
		cppCopyFields.add(copyField);
		given(cppCopy.getFields()).willReturn(cppCopyFields);
		cppCopy.fields = cppCopyFields;
		
		given(cloudProviderParamsCopyService.save(cppCopy)).willReturn(cppCopy);
		getAccount();
		given(cppCopy.getAccount()).willReturn(account);
		
		given(cloudProviderParametersService.updateFields(cpp, cppCopy, input, userName)).willCallRealMethod();
		CloudProviderParameters credential = cloudProviderParametersService.updateFields(cpp, cppCopy, input, userName);
		assertTrue(credential != null);
		assertTrue(credential.getFields().size() == 1);
	}

	@Test
	public void testUpdateCPPCheckAdded() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException{
		String key = "key";String value = "value";
		String secondKey = "secondKey";String secondValue = "secondValue";
		CloudProviderParameters cpp = Mockito.mock(CloudProviderParameters.class);
		CloudProviderParametersResource input = Mockito.mock(CloudProviderParametersResource.class);
		CloudProviderParametersField field = mock(CloudProviderParametersField.class);
		given(field.getKey()).willReturn(key);given(field.getValue()).willReturn(value);
		Collection<CloudProviderParametersField> cppFields = new ArrayList<>();
		cppFields.add(field);
		given(cpp.getFields()).willReturn(cppFields);
		cpp.fields = cppFields;
		
		CloudProviderParametersFieldResource fieldResource = mock(CloudProviderParametersFieldResource.class);
		given(fieldResource.getKey()).willReturn(key);given(fieldResource.getValue()).willReturn(value);

		CloudProviderParametersFieldResource secondFieldResource = mock(CloudProviderParametersFieldResource.class);
		given(secondFieldResource.getKey()).willReturn(secondKey);given(secondFieldResource.getValue()).willReturn(secondValue);
		Collection<CloudProviderParametersFieldResource> fieldResources = new ArrayList<>();
		fieldResources.add(fieldResource);fieldResources.add(secondFieldResource);
		given(input.getFields()).willReturn(fieldResources);
		given(cloudProviderParametersService.save(cpp)).willReturn(cpp);
		CloudProviderParamsCopy cppCopy = Mockito.mock(CloudProviderParamsCopy.class);
		
		CloudProviderParamsCopyField copyField = mock(CloudProviderParamsCopyField.class);
		given(field.getKey()).willReturn(key);given(field.getValue()).willReturn(value);
		Collection<CloudProviderParamsCopyField> cppCopyFields = new ArrayList<>();
		cppCopyFields.add(copyField);
		given(cppCopy.getFields()).willReturn(cppCopyFields);
		cppCopy.fields = cppCopyFields;
		
		given(cloudProviderParamsCopyService.save(cppCopy)).willReturn(cppCopy);
		getAccount();
		given(cppCopy.getAccount()).willReturn(account);
		
		given(cloudProviderParametersService.updateFields(cpp, cppCopy, input, userName)).willCallRealMethod();
		CloudProviderParameters credential = cloudProviderParametersService.updateFields(cpp, cppCopy, input, userName);
		assertTrue(credential != null);
		assertTrue(credential.getFields().size() == 2);
	}

	
	private Collection<CloudProviderParameters> getCloudProviderParameters(){
		List<CloudProviderParameters> cppCollection = new ArrayList<>();
		cppCollection.add(cpp);
		return cppCollection;
	}
	
	private void getAccount(){
		String email = "some@email";
		String reference = "somereference";
		Date firstJoinedDate = new Date(2,2,2000);
		given(account.getFirstJoinedDate()).willReturn(firstJoinedDate);
		given(account.getReference()).willReturn(reference);
		given(account.getUsername()).willReturn(userName);
	}
	

	private void getRequest(){
		given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn(tokenArray);
	}

	private void getTeamResource(Team team){

		given(team.getAccount()).willReturn(account);
		given(team.getName()).willReturn(teamName);
		given(teamResource.getName()).willReturn(teamName);
		given(team.getDomainReference()).willReturn("a reference");
		Set<CloudProviderParameters> cpps = new HashSet<>();
		cpps.add(cpp);
		team.setCppBelongingToTeam(cpps);
		given(team.getCppBelongingToTeam()).willReturn(cpps);
	}
	
	private void getTeamResourceNoDomain(Team team){

		given(team.getAccount()).willReturn(account);
		given(team.getName()).willReturn(teamName);
		given(teamResource.getName()).willReturn(teamName);
		given(team.getDomainReference()).willReturn(null);
		Set<CloudProviderParameters> cpps = new HashSet<>();
		cpps.add(cpp);
		team.setCppBelongingToTeam(cpps);
		given(team.getCppBelongingToTeam()).willReturn(cpps);
	}
	
	private void getDomain(){
		String domainReference = "a domain reference";
		given(domain.getDomainReference()).willReturn(domainReference);
		given(domainService.createDomain("TEAM_"+team.getName().toUpperCase()+"_PORTAL", "Domain TEAM_"+team.getName()+"_PORTAL"+" created", token)).willReturn(domain);
		Mockito.when(domainService.getDomainByReference(Mockito.anyString(), Mockito.anyString())).thenReturn(domain);
	}


}
