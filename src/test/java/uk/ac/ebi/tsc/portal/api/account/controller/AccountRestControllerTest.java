package uk.ac.ebi.tsc.portal.api.account.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import uk.ac.ebi.tsc.portal.api.account.controller.AccountRestController;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;
import uk.ac.ebi.tsc.portal.api.account.service.AccountService;
import uk.ac.ebi.tsc.portal.api.account.service.UserNotFoundException;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class AccountRestControllerTest {

	@MockBean
	private AccountRestController subject;
	
	@MockBean
	private AccountService accountService;
	
	@MockBean
	private AccountRepository accountRepository;
	
	@MockBean
	private Account account;
	
	@MockBean
	private Principal principal;
	private String principalName = "principalName";
	
	
	@Before
	public void setUp(){
		ReflectionTestUtils.setField(subject, "accountService", accountService);
		ReflectionTestUtils.setField(accountService, "accountRepository", accountRepository);
	}
	
	/**
	 * test if service can get account of currently logged user
	 */
	@Test
	public void testGetCurrentAccount(){
		getPrincipal();
		getAccount();
		given(subject.getCurrentAccount(principal)).willCallRealMethod();
		assertTrue(subject.getCurrentAccount(principal) != null );
	}
	
	/**
	 * test if service can get account of currently logged user, using username
	 */
	@Test
	public void testGetCurrentAccountByName(){
		getPrincipal();
		getAccount();
		given(accountService.findByUsername(principalName)).willReturn(account);
		
		given(subject.getAccountByUsername(principalName)).willCallRealMethod();
		assertTrue(subject.getAccountByUsername(principalName) != null );
		
	}
	
	/**
	 * test if service throws 'User Not Found' , if the user
	 * does not have an account
	 */
	@Test(expected = UserNotFoundException.class)
	public void testGetCurrentAccountByNameThrowsException(){
		UserNotFoundException userNFE = mock(UserNotFoundException.class);
		getPrincipal();
		getAccount();
		given(accountService.findByUsername(principalName)).willCallRealMethod();
		given(subject.getAccountByUsername(principalName)).willThrow(userNFE);
		subject.getAccountByUsername(principalName);
	}
	
	private void getPrincipal(){
		given(principal.getName()).willReturn(principalName);
	}
	
	private void getAccount(){
		given(accountService.findByUsername(principalName)).willReturn(account);
		given(account.getUsername()).willReturn(principalName);
	}
}
