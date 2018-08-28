package uk.ac.ebi.tsc.portal.security;

import org.junit.Test;
import org.springframework.security.core.Authentication;
import uk.ac.ebi.tsc.aap.client.model.User;
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
 */
public class EcpAuthenticationServiceTest {

    private EcpAuthenticationService subject;

    private uk.ac.ebi.tsc.aap.client.security.TokenAuthenticationService mockAuthService =
            mock(uk.ac.ebi.tsc.aap.client.security.TokenAuthenticationService.class);

    private AccountRepository mockAccountRepo = mock(AccountRepository.class);
    private TokenService mockTokenService = mock(TokenService.class);
    private DeploymentRepository mockDeploymentRepository = mock(DeploymentRepository.class);
    private DeploymentConfigurationRepository mockDeploymentConfigurationRepository = mock(DeploymentConfigurationRepository.class);
    private DeploymentStatusRepository mockDeploymentStatusRepository = mock(DeploymentStatusRepository.class);
    private CloudProviderParamsCopyRepository mockCloudProviderParamsCopyRepository = mock(CloudProviderParamsCopyRepository.class);
    private TeamRepository mockTeamRepository = mock(TeamRepository.class);
    private ApplicationDeployerBash mockApplicationDeployerBash = mock(ApplicationDeployerBash.class);
    private DomainService mockDomainService = mock(DomainService.class);
    private EncryptionService mockEncryptionService = mock(EncryptionService.class);


    public EcpAuthenticationServiceTest() {

        subject = new EcpAuthenticationService(
                mockAuthService,
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
                "aap.ecp.password"

        );

    }

    @Test
    public void
    returns_auth_for_valid_token() {
        User mockUser = mock(User.class);
        when(mockUser.getUserName()).thenReturn("pretend-user");
        when(mockUser.getUsername()).thenReturn("pretend-user");

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn("pretend-user");
        when(mockAuth.getDetails()).thenReturn(mockUser);

        HttpServletRequest mockRequest = withAuthorizationHeader("Bearer pretend-valid-token");

        Account mockAccount = mock(Account.class);
        when(mockAccount.getUsername()).thenReturn("pretend-user");
        when(mockAccount.getGivenName()).thenReturn("pretend-user");
        when(mockAccount.getEmail()).thenReturn("user@domain.com");
        when(mockAccountRepo.findByUsername("pretend-user")).thenReturn(Optional.of(mockAccount));

        when(mockAuthService.getAuthentication(mockRequest)).thenReturn(mockAuth);
        when(mockTokenService.getAAPToken("ecp-account-username","ecp-account-password")).thenReturn("ecp-aap-pretend-valid-token");

        Authentication auth = subject.getAuthentication(mockRequest);
        assertEquals(auth.getName(), "pretend-user");

    }

    @Test public void
    detects_invalid_jwt() {
        HttpServletRequest request = withAuthorizationHeader("Bearer pretend-invalid-token");
        when(request.getHeader("Authorization").
                equals("Bearer pretend-invalid-token")).thenReturn(null);
        Authentication auth = subject.getAuthentication(request);
        assertNull(auth);
    }

    private HttpServletRequest withAuthorizationHeader(String value) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(value);
        return request;
    }

}
