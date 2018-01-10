package uk.ac.ebi.tsc.portal.api.account.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class AccountServiceTest {

	@MockBean
	private AccountService accountService;

	@MockBean
	private AccountRepository accountRepository;

	@MockBean
	private Account account;

	String email = "some@email";
	String reference = "somereference";
	String username = "username";

	@Before
	public void setUp(){
		accountRepository = mock(AccountRepository.class);
		ReflectionTestUtils.setField(accountService, "accountRepository", accountRepository);
	}

	@Test
	public void testFindByEmail(){
		List<Account> accounts = new ArrayList<>();
		accounts.add(account);
		given(accountRepository.findByEmail(email)).willReturn(accounts);
		given(accountService.findByEmail(email)).willCallRealMethod();
		assertEquals(accountService.findByEmail(email), account);
	}

	@Test(expected = UserNotFoundException.class)
	public void testFindByEmailThrowsException(){
		given(accountRepository.findByEmail(email)).willThrow(UserNotFoundException.class);
		given(accountService.findByEmail(email)).willCallRealMethod();
		accountService.findByEmail(email);
	}

	@Test
	public void testFindByReference(){
		given(accountRepository.findByReference(reference)).willReturn(Optional.of(account));
		given(accountService.findByReference(reference)).willCallRealMethod();
		assertEquals(accountService.findByReference(reference), account);
	}

	@Test(expected = UserNotFoundException.class)
	public void testFindByReferenceThrowsException(){
		given(accountRepository.findByReference(reference)).willThrow(UserNotFoundException.class);
		given(accountService.findByReference(reference)).willCallRealMethod();
		accountService.findByReference(reference);
	}

	@Test
	public void testFindByUsername(){
		given(accountRepository.findByUsername(username)).willReturn(Optional.of(account));
		given(accountService.findByUsername(username)).willCallRealMethod();
		assertEquals(accountService.findByUsername(username), account);
	}

	@Test(expected = UserNotFoundException.class)
	public void testFindByUsernameThrowsException(){
		given(accountRepository.findByUsername(username)).willThrow(UserNotFoundException.class);
		given(accountService.findByUsername(username)).willCallRealMethod();
		accountService.findByUsername(username);
	}
	
	public void testSave(){
		given(accountRepository.save(account)).willReturn(account);
    	Account accountToBeReturned = accountRepository.save(account);
    	given(accountService.save(account)).willCallRealMethod();
    	Account accountReturned = accountService.save(account);
    	assertEquals(accountToBeReturned, accountReturned);
	}


}
