package uk.ac.ebi.tsc.portal.api.account.service;

import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account findByReference(String reference) {
        return this.accountRepository.findByReference(reference).orElseThrow(
                () -> new UserNotFoundException(reference));
    }

    public Account findByUsername(String username) {
        return this.accountRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException(username));
    }

    public Account findByEmail(String email) {
        return this.accountRepository.findByEmail(email).stream().findFirst().orElseThrow(
                () -> new UserNotFoundException(email));
    }
    
    
    public Account save(Account account) {
        return this.accountRepository.save(account);
    }

}
