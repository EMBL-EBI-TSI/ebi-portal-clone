package uk.ac.ebi.tsc.portal.security;

import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TokenAuthenticationServiceTest {

    private TokenAuthenticationService subject;

    private TokenHandler mockHandler;
    private AccountRepository mockAccountRepo = mock(AccountRepository.class);

    UserDetailsService mockService;

    public TokenAuthenticationServiceTest() {
        UserDetails aUser = mock(UserDetails.class);
        when(aUser.getUsername()).thenReturn("pretend-user");

        mockHandler = mock(TokenHandler.class);
        when(mockHandler.loadUserFromTokenSub("pretend-invalid-token")).thenThrow(new RuntimeException("Exception: Pretend Invalid Token"));
        when(mockHandler.loadUserFromTokenSub("pretend-valid-token")).thenReturn(aUser);
        when(mockHandler.loadUserFromTokenName("pretend-invalid-token")).thenThrow(new RuntimeException("Exception: Pretend Invalid Token"));
        when(mockHandler.loadUserFromTokenName("pretend-valid-token")).thenReturn(aUser);
        when(mockHandler.parseNameFromToken("pretend-valid-token")).thenReturn("pretend-user");
        when(mockHandler.parseUserNameFromToken("pretend-valid-token")).thenReturn("pretend-user");

        Account mockAccount = mock(Account.class);

        when(mockAccountRepo.findByUsername("pretend-user")).thenReturn(Optional.of(mockAccount));
        subject = new TokenAuthenticationService(mockHandler, mockAccountRepo);
    }

    @Test
    public void
    returns_auth_for_valid_token() {
        HttpServletRequest request = withAuthorizationHeader("Bearer pretend-valid-token");
        Authentication auth = subject.getAuthentication(request);
        assertEquals(auth.getName(), "pretend-user");
    }

    @Test
    public void
    detects_missing_authorization_header() {
        HttpServletRequest request = withAuthorizationHeader(null);
        Authentication auth = subject.getAuthentication(request);
        assertNull(auth);
    }

    @Test
    public void
    detects_empty_authorization_header() {
        HttpServletRequest request = withAuthorizationHeader("");
        Authentication auth = subject.getAuthentication(request);
        assertNull(auth);
    }

    @Test
    public void
    detects_non_bearer_authorization_header() {
        HttpServletRequest request = withAuthorizationHeader("blah ");
        Authentication auth = subject.getAuthentication(request);
        assertNull(auth);
    }

    @Test
    public void
    detects_invalid_jwt() {
        HttpServletRequest request = withAuthorizationHeader("Bearer pretend-invalid-token");
        Authentication auth = subject.getAuthentication(request);
        assertNull(auth);
    }

    private HttpServletRequest withAuthorizationHeader(String value) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(value);
        return request;
    }

}
