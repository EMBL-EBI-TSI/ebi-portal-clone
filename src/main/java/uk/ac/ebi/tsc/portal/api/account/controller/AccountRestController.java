package uk.ac.ebi.tsc.portal.api.account.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;
import uk.ac.ebi.tsc.portal.api.account.service.AccountService;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@RestController
@RequestMapping(value = "/account", produces = {MediaType.APPLICATION_JSON_VALUE})
public class AccountRestController {

    private static final Logger logger = LoggerFactory.getLogger(AccountRestController.class);

    private final AccountService accountService;

    @Autowired
    AccountRestController(AccountRepository accountRepository) {
        this.accountService = new AccountService(accountRepository);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.OPTIONS} )
    public AccountResource getCurrentAccount(Principal principal) {
        String userId = principal.getName();
        logger.info("Account " + userId + " (current) requested");
        return new AccountResource(this.accountService.findByUsername(userId));
    }

    @RequestMapping(value = "/{userId:.+}", method = {RequestMethod.GET, RequestMethod.OPTIONS} )
    public AccountResource getAccountByUsername(@PathVariable("userId") String userId) {
        logger.info("Account " + userId + " requested");

        return new AccountResource(this.accountService.findByUsername(userId));
    }

}
