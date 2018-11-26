package uk.ac.ebi.tsc.portal.api.configuration.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import uk.ac.ebi.tsc.aap.client.model.Domain;
import uk.ac.ebi.tsc.aap.client.model.User;
import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller.CloudProviderParametersFieldResource;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller.CloudProviderParametersResource;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParametersField;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParametersService;
import uk.ac.ebi.tsc.portal.api.configuration.controller.ConfigurationDeploymentParameterResource;
import uk.ac.ebi.tsc.portal.api.configuration.controller.ConfigurationDeploymentParametersResource;
import uk.ac.ebi.tsc.portal.api.configuration.controller.ConfigurationResource;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigDeploymentParamsCopy;
import uk.ac.ebi.tsc.portal.api.configuration.repo.Configuration;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParameter;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParameters;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationRepository;
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
public class ConfigurationServiceTest {
	
    @MockBean
    private ConfigurationService configurationService;
    
    @MockBean
    private ConfigurationRepository configurationRepository;
    
    @MockBean
    private Configuration configuration;
    
    @MockBean
	private Team team;

	@MockBean
	private TeamResource teamResource ;
	
	@MockBean
	Domain domain;
	
	@MockBean
	DomainService domainService;
	
	@MockBean
	User user;
	
	@MockBean
	Account accountMock;
	
	@MockBean
	ConfigurationResource input;
	
	@MockBean
	ConfigurationDeploymentParameters cdps;
	
	@MockBean
	CloudProviderParametersService cppService;
	
	@MockBean
	ConfigurationDeploymentParametersService cdpService;
	
	@MockBean
	CloudProviderParameters cpp;
	
	@MockBean
	ConfigDeploymentParamsCopyService cdpCopyService;
	
	@MockBean
	Principal principalMock;
	String tokenArray = "some token";
	String token = "token" ;
	String userEmail = "userEmail";
	String configurationName = "configurationName";
	String teamName = "teamName";
	
    
    @Before
    public void setUp(){
    	configurationRepository = mock(ConfigurationRepository.class);
    	when(user.getEmail()).thenReturn(userEmail);
    	when(this.principalMock.getName()).thenReturn("A user name");
		when(user.getEmail()).thenReturn(userEmail);
		when(this.accountMock.getId()).thenReturn(1L);
		when(this.accountMock.getUsername()).thenReturn("A user name");
		when(this.accountMock.getEmail()).thenReturn("an@email.com");
		when(this.accountMock.getPassword()).thenReturn("A password");
		when(this.accountMock.getOrganisation()).thenReturn("An organisation");
    	ReflectionTestUtils.setField(configurationService, "configurationRepository", configurationRepository);
    	ReflectionTestUtils.setField(configurationService, "domainService", domainService);
    	ReflectionTestUtils.setField(configurationService, "cppService", cppService);
    	ReflectionTestUtils.setField(configurationService, "cdpService", cdpService);
    }
    
    /**
     * test if service returns a configuration by its name and
     * the account username
     */
    @Test
    public void testFindByNameAndAccountUsernameValidConfiguration(){
    	String name = "configurationName";
    	String username = "userName";
    	given(configuration.getName()).willReturn(name);
    	given(configurationRepository.findByNameAndAccountUsername(name, username)).willReturn(Optional.of(configuration));
    	given(configurationService.findByNameAndAccountUsername(name, username)).willReturn(configuration);
    	assertEquals(configurationService.findByNameAndAccountUsername(name, username).getName(),
    			name);
    }
    
    /**
     * test if 'NullPointerException' is thrown if
     * a null is returned 
     */
    @Test(expected = NullPointerException.class)
    public void testFindByNameAndAccountUsernameNullConfiguration(){
    	String name = "configurationName";
    	String username = "userName";
    	given(configuration.getName()).willReturn(name);
    	given(configurationRepository.findByNameAndAccountUsername(name, username)).willReturn(null);
    	configurationService.findByNameAndAccountUsername(name, username).getName();
    }
    
    /**
     * test if 'ConfigurationNotFoundException' is thrown if
     * a configuration cannot be found
     */
    @Test(expected = ConfigurationNotFoundException.class)
    public void testFindByNameAndAccountUsernameThrowsException(){
    	String name = "configurationName";
    	String username = "userName";
    	given(configurationRepository.findByNameAndAccountUsername(name, username)).willThrow(ConfigurationNotFoundException.class);
    	given(configurationRepository.findByNameAndAccountUsername(name, username)).willCallRealMethod();
    }
    
    /**
     * test if service returns set of configurations for an user
     * 
     */
    @Test
    public void testFindByNameReturnsNotNull(){
    	String username = "userName";
    	given(configurationRepository.findByAccountUsername(username)).willReturn((Set<Configuration>) mock(Set.class));
    	assertNotNull(configurationService.findByAccountUsername( username));
    }
    
    /**
     * test if service returns set of configurations for an user
     * or atleast empty set if no configuration is found
     */
    @Test
    public void testFindByNameReturnsEmptyCollection(){
    	String username = "userName";
    	given(configurationRepository.findByAccountUsername(username)).willReturn((Collection<Configuration>) mock(Collection.class));
    	assertEquals(configurationService.findByAccountUsername( username).size(), 0);
    }
    
    /**
     * test if configuration is saved successfully and returned
     */
    @Test
    public void testSave(){
    	given(configurationRepository.save(configuration)).willReturn(configuration);
    	Configuration configurationToBeReturned = configurationRepository.save(configuration);
    	given(configurationService.save(configuration)).willCallRealMethod();
    	Configuration configurationReturned = configurationService.save(configuration);
    	assertEquals(configurationToBeReturned, configurationReturned);
    }
    
    @Test
	public void testGetSharedConfigurationsByAccountPass(){
		
		getTeamResource(team);
		getDomain();
		Set<Team> teams = new HashSet<>();
		teams.add(team);
		Set<Configuration> configurations = new HashSet<>();
		configurations.add(configuration);
		team.setConfigurationsBelongingToTeam(configurations);
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
		when(configurationService.getSharedConfigurationsByAccount(accountMock, token, user)).thenCallRealMethod();
		Set<Configuration> configurationsReturned = configurationService.getSharedConfigurationsByAccount(accountMock, token, user);
		assertTrue(configurationsReturned.size() == 1);
	}
    
    @Test
 	public void testGetSharedConfigurationsByAccountNoDomainPass(){
 		
 		getTeamResourceNoDomain(team);
 		getDomain();
 		Set<Team> teams = new HashSet<>();
 		teams.add(team);
 		Set<Configuration> configurations = new HashSet<>();
 		configurations.add(configuration);
 		team.setConfigurationsBelongingToTeam(configurations);
 		accountMock.setMemberOfTeams(teams);
 		when(accountMock.getMemberOfTeams()).thenReturn(teams);
 		Answer teamAnswer = new Answer<Set<Team>>(){
 			@Override
 			public Set<Team> answer(InvocationOnMock invocation){
 				return teams;
 		}};
 		when(accountMock.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
 		when(configurationService.getSharedConfigurationsByAccount(accountMock, token, user)).thenCallRealMethod();
 		Set<Configuration> configurationsReturned = configurationService.getSharedConfigurationsByAccount(accountMock, token, user);
 		assertTrue(configurationsReturned.size() == 1);
 	}
    
    @Test
   	public void testGetSharedConfigurationsByNamePass(){
   		
   		getTeamResource(team);
   		getDomain();
   		Set<Team> teams = new HashSet<>();
   		teams.add(team);
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
   		when(configurationService.getSharedConfigurationByName(accountMock, token, user, configurationName)).thenCallRealMethod();
   		Configuration configurationsReturned = configurationService.getSharedConfigurationByName(accountMock, token, user, configurationName);
   		assertTrue(configurationsReturned.getName().equals(configurationName));
   	}
    
    @Test
   	public void testGetSharedConfigurationsByNameNoDomainPass(){
   		
   		getTeamResourceNoDomain(team);
   		Set<Team> teams = new HashSet<>();
   		teams.add(team);
   		accountMock.setMemberOfTeams(teams);
   		when(accountMock.getMemberOfTeams()).thenReturn(teams);
   		Answer teamAnswer = new Answer<Set<Team>>(){
   			@Override
   			public Set<Team> answer(InvocationOnMock invocation){
   				return teams;
   		}};
   		when(accountMock.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
   		when(configurationService.getSharedConfigurationByName(accountMock, token, user, configurationName)).thenCallRealMethod();
   		Configuration configurationsReturned = configurationService.getSharedConfigurationByName(accountMock, token, user, configurationName);
   		assertTrue(configurationsReturned.getName().equals(configurationName));
   	}
    
    @Test
   	public void testGetSharedConfigurationsByNameThrowsException(){
   		
   		getTeamResource(team);
   		getDomain();
   		Set<Team> teams = new HashSet<>();
   		teams.add(team);
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
   		when(configurationService.getSharedConfigurationByName(accountMock, token, user, configurationName)).thenCallRealMethod();
   		Configuration configurationsReturned = configurationService.getSharedConfigurationByName(accountMock, token, user, configurationName);
   		assertTrue(configurationsReturned == null);
   	}
    
    @Test
   	public void testGetSharedConfigurationsByAccountReturnsEmpty(){
   		
   		getTeamResource(team);
   		getDomain();
   		Set<Team> teams = new HashSet<>();
   		teams.add(team);
   		Set<Configuration> configurations = new HashSet<>();
   		configurations.add(configuration);
   		team.setConfigurationsBelongingToTeam(configurations);
   		accountMock.setMemberOfTeams(teams);
   		when(accountMock.getMemberOfTeams()).thenReturn(teams);
   		List<User> users = new ArrayList<>();
   		users.add(user);
   		when(domainService.getAllUsersFromDomain(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
   		Answer teamAnswer = new Answer<Set<Team>>(){
   			@Override
   			public Set<Team> answer(InvocationOnMock invocation){
   				return teams;
   		}};
   		when(accountMock.getMemberOfTeams()).thenAnswer(teamAnswer) ; 
   		when(configurationService.getSharedConfigurationsByAccount(accountMock, token, user)).thenCallRealMethod();
   		Set<Configuration> configurationsReturned = configurationService.getSharedConfigurationsByAccount(accountMock, token, user);
   		assertTrue(configurationsReturned.size() == 0);
   	}
    
    @Test
    public void testUpdateConfiguration(){
    	
    	//set input
    	String cppName="a cpp name", dpName= "a dp name", sshKey = "a ssh key";
    	given(input.getCloudProviderParametersName()).willReturn(cppName);
    	given(input.getDeploymentParametersName()).willReturn(dpName);
    	given(input.getSshKey()).willReturn(sshKey);
    	given(input.getName()).willReturn(configurationName);
    	
    	//set configuration
    	given(configuration.getCloudProviderParametersName()).willReturn("a credential name");
    	given(cdps.getName()).willReturn("a name");
    	given(configuration.getSshKey()).willReturn("a sshkey");
    	given(configuration.getName()).willReturn(configurationName);
    	
    	given(cppService.findByNameAndAccountUsername(Mockito.anyString(), Mockito.anyString())).willReturn(cpp);
    	given(configurationService.findByNameAndAccountUsername(Mockito.anyString(), Mockito.anyString())).willReturn(configuration);
    	given(cdpService.findByName(Mockito.anyString())).willReturn(cdps);
    	given(configurationService.save(configuration)).willReturn(configuration);
    	ConfigDeploymentParamsCopy configDeploymentParamsCopy = mock(ConfigDeploymentParamsCopy.class);
    	given(configDeploymentParamsCopy.getName()).willReturn(dpName);
    	given(cdpCopyService.findByConfigurationDeploymentParametersReference(Mockito.anyString())).willReturn(configDeploymentParamsCopy);
    	given(configurationService.update(accountMock, principalMock, input, configurationName, cdpCopyService)).willCallRealMethod();
    	Configuration returnedConfiguration = configurationService.update(accountMock, principalMock, input, configurationName, cdpCopyService);
    	assertNotNull(returnedConfiguration);
    }
	@Test
	public void testUpdateDeploymentParameterCheckUpdated(){
		
		String key = "key";String value = "value";
		ConfigurationDeploymentParameters cdp = Mockito.mock(ConfigurationDeploymentParameters.class);
		ConfigurationDeploymentParameter field = mock(ConfigurationDeploymentParameter.class);
		given(field.getKey()).willReturn(key);given(field.getValue()).willReturn(value);
		Set<ConfigurationDeploymentParameter> cdpFields = new HashSet<>();
		cdpFields.add(field);
		given(cdp.getConfigurationDeploymentParameter()).willReturn(cdpFields);
		cdp.setConfigurationDeploymentParameter(cdpFields);
		
		String keyResource = "key1";String valueResource = "value1";
		ConfigurationDeploymentParametersResource input = 
				Mockito.mock(ConfigurationDeploymentParametersResource.class);
		ConfigurationDeploymentParameterResource fieldResource = mock(ConfigurationDeploymentParameterResource.class);
		given(fieldResource.getKey()).willReturn(keyResource);given(fieldResource.getValue()).willReturn(valueResource);
		Collection<ConfigurationDeploymentParameterResource> cdpFieldResource = new ArrayList<>();
		cdpFieldResource.add(fieldResource);
		given(input.getFields()).willReturn(cdpFieldResource);
		input.setFields(cdpFieldResource);
		given(cdpService.save(cdp)).willReturn(cdp);
		ConfigDeploymentParamsCopy deploymentParametersCopy = Mockito.mock(ConfigDeploymentParamsCopy.class);
		given(configurationService.updateDeploymentParameterFields(cdp,  deploymentParametersCopy, input, cdpCopyService)).willCallRealMethod();
		ConfigurationDeploymentParameters depParam = configurationService.updateDeploymentParameterFields(cdp,  deploymentParametersCopy, input, cdpCopyService);
		assertTrue(depParam != null);
		assertTrue(depParam.getConfigurationDeploymentParameter().size() == 1);
	}
	
	@Test
	public void testUpdateDeploymentParameterCheckRemoved(){
		
		String key = "key";String value = "value";
		String secondKey = "secondKey";String secondValue = "secondValue";
		ConfigurationDeploymentParameters cdp = Mockito.mock(ConfigurationDeploymentParameters.class);
		ConfigurationDeploymentParameter field = mock(ConfigurationDeploymentParameter.class);
		given(field.getKey()).willReturn(key);given(field.getValue()).willReturn(value);
		ConfigurationDeploymentParameter secondField = mock(ConfigurationDeploymentParameter.class);
		given(secondField.getKey()).willReturn(secondKey);given(secondField.getValue()).willReturn(secondValue);
		Set<ConfigurationDeploymentParameter> cdpFields = new HashSet<>();
		cdpFields.add(field);cdpFields.add(secondField);
		given(cdp.getConfigurationDeploymentParameter()).willReturn(cdpFields);
		cdp.setConfigurationDeploymentParameter(cdpFields);
		
		ConfigurationDeploymentParametersResource input = 
				Mockito.mock(ConfigurationDeploymentParametersResource.class);
		ConfigurationDeploymentParameterResource fieldResource = mock(ConfigurationDeploymentParameterResource.class);
		given(fieldResource.getKey()).willReturn(key);given(fieldResource.getValue()).willReturn(value);
		Collection<ConfigurationDeploymentParameterResource> cdpFieldResource = new ArrayList<>();
		cdpFieldResource.add(fieldResource);
		given(input.getFields()).willReturn(cdpFieldResource);
		input.setFields(cdpFieldResource);
		given(cdpService.save(cdp)).willReturn(cdp);
		ConfigDeploymentParamsCopy deploymentParametersCopy = Mockito.mock(ConfigDeploymentParamsCopy.class);
		given(configurationService.updateDeploymentParameterFields(cdp,  deploymentParametersCopy, input, cdpCopyService)).willCallRealMethod();
		ConfigurationDeploymentParameters depParam = configurationService.updateDeploymentParameterFields(cdp,  deploymentParametersCopy, input, cdpCopyService);
		assertTrue(depParam != null);
		assertTrue(depParam.getConfigurationDeploymentParameter().size() == 1);
	}

	@Test
	public void testUpdateCPPCheckAdded() {
		
		String key = "key";String value = "value";
		String secondKey = "secondKey";String secondValue = "secondValue";
		ConfigurationDeploymentParameters cdp = Mockito.mock(ConfigurationDeploymentParameters.class);
		ConfigurationDeploymentParameter field = mock(ConfigurationDeploymentParameter.class);
		given(field.getKey()).willReturn(key);given(field.getValue()).willReturn(value);
		ConfigurationDeploymentParameter secondField = mock(ConfigurationDeploymentParameter.class);
		given(secondField.getKey()).willReturn(secondKey);given(secondField.getValue()).willReturn(secondValue);
		Set<ConfigurationDeploymentParameter> cdpFields = new HashSet<>();
		cdpFields.add(field);cdpFields.add(secondField);
		given(cdp.getConfigurationDeploymentParameter()).willReturn(cdpFields);
		cdp.setConfigurationDeploymentParameter(cdpFields);
		
		ConfigurationDeploymentParametersResource input = 
				Mockito.mock(ConfigurationDeploymentParametersResource.class);
		ConfigurationDeploymentParameterResource fieldResource = mock(ConfigurationDeploymentParameterResource.class);
		given(fieldResource.getKey()).willReturn(key);given(fieldResource.getValue()).willReturn(value);
		ConfigurationDeploymentParameterResource secondFieldResource = mock(ConfigurationDeploymentParameterResource.class);
		given(secondFieldResource.getKey()).willReturn(secondKey);given(secondFieldResource.getValue()).willReturn(secondValue);
		Collection<ConfigurationDeploymentParameterResource> cdpFieldResource = new ArrayList<>();
		cdpFieldResource.add(fieldResource);cdpFieldResource.add(secondFieldResource);
		given(input.getFields()).willReturn(cdpFieldResource);
		input.setFields(cdpFieldResource);
		
		given(cdpService.save(cdp)).willReturn(cdp);
		ConfigDeploymentParamsCopy deploymentParametersCopy = Mockito.mock(ConfigDeploymentParamsCopy.class);
		given(configurationService.updateDeploymentParameterFields(cdp,  deploymentParametersCopy, input, cdpCopyService)).willCallRealMethod();
		ConfigurationDeploymentParameters depParam = configurationService.updateDeploymentParameterFields(cdp,  deploymentParametersCopy, input, cdpCopyService);
		
		assertTrue(depParam != null);
		assertTrue(depParam.getConfigurationDeploymentParameter().size() == 2);
	}
   
	private void getTeamResource(Team team){

		given(team.getAccount()).willReturn(accountMock);
		given(team.getName()).willReturn(teamName);
		given(teamResource.getName()).willReturn(teamName);
		given(team.getDomainReference()).willReturn("a reference");
		Set<Configuration> configurations = new HashSet<>();
		configurations.add(configuration);
		given(configuration.getName()).willReturn(configurationName);
		team.setConfigurationsBelongingToTeam(configurations);
		given(team.getConfigurationsBelongingToTeam()).willReturn(configurations);
	}
    
    private void getTeamResourceNoDomain(Team team){

		given(team.getAccount()).willReturn(accountMock);
		given(team.getName()).willReturn(teamName);
		given(teamResource.getName()).willReturn(teamName);
		given(team.getDomainReference()).willReturn(null);
		Set<Configuration> configurations = new HashSet<>();
		configurations.add(configuration);
		given(configuration.getName()).willReturn(configurationName);
		team.setConfigurationsBelongingToTeam(configurations);
		given(team.getConfigurationsBelongingToTeam()).willReturn(configurations);
	}
	
	private void getDomain(){
		String domainReference = "a domain reference";
		given(domain.getDomainReference()).willReturn(domainReference);
		given(domainService.createDomain("TEAM_"+team.getName().toUpperCase()+"_PORTAL", "Domain TEAM_"+team.getName()+"_PORTAL"+" created", token)).willReturn(domain);
		Mockito.when(domainService.getDomainByReference(Mockito.anyString(), Mockito.anyString())).thenReturn(domain);
	}
    
}
