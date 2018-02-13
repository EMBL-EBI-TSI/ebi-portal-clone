package uk.ac.ebi.tsc.portal.security;

import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.aap.client.repo.TokenService;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentConfigurationRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentStatusRepository;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;
import uk.ac.ebi.tsc.portal.api.team.repo.TeamRepository;
import uk.ac.ebi.tsc.portal.clouddeployment.application.ApplicationDeployerBash;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class TokenAuthenticationServiceTest {

    private TokenAuthenticationService subject;

    private TokenHandler mockHandler;
    private AccountRepository mockAccountRepo = mock(AccountRepository.class);
    private DeploymentRepository mockDeploymentRepository = mock(DeploymentRepository.class);
    private DeploymentConfigurationRepository mockDeploymentConfigurationRepository = mock(DeploymentConfigurationRepository.class);
    private DeploymentStatusRepository mockDeploymentStatusRepository = mock(DeploymentStatusRepository.class);
    private CloudProviderParamsCopyRepository mockCloudProviderParamsCopyRepository = mock(CloudProviderParamsCopyRepository.class);
    private TeamRepository mockTeamRepository = mock(TeamRepository.class);
    private ApplicationDeployerBash mockApplicationDeployerBash = mock(ApplicationDeployerBash.class);
    private DomainService mockDomainService = mock(DomainService.class);
    private EncryptionService mockEncryptionService = mock(EncryptionService.class);
    private TokenService mockTokenService = mock(TokenService.class);

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
        when(mockAccount.getEmail()).thenReturn("user@domain.com");
        when(mockAccountRepo.findByUsername("pretend-user")).thenReturn(Optional.of(mockAccount));

        subject = new TokenAuthenticationService(
                mockHandler,
                mockAccountRepo,
                mockDeploymentRepository,
                mockDeploymentStatusRepository,
                mockDeploymentConfigurationRepository,
                mockCloudProviderParamsCopyRepository,
                mockTeamRepository,
                mockApplicationDeployerBash,
                mockDomainService,
                mockTokenService,
                mockEncryptionService,
                "salt",
                "password",
                "aap.ecp.user",
                "aap.ecp.password");
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
