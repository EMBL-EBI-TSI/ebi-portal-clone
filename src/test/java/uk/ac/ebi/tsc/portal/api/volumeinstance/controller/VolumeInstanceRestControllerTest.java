package uk.ac.ebi.tsc.portal.api.volumeinstance.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParametersRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstance;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceRepository;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceStatusRepository;
import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetup;
import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetupRepository;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDeployerException;
import uk.ac.ebi.tsc.portal.clouddeployment.volume.VolumeDeployerBash;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Date;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class VolumeInstanceRestControllerTest {

    private static final String APPS_ROOT_FOLDER = "/a/path/has/no/name";

    public final String A_USER_NAME = "A User Name";
    public final Date A_DATE = new Date(1);
    public final String A_REFERENCE = "acc-1010101";
    public final String A_CLOUD_PROVIDER = "OSTACK";
    String salt= "salt";
    String password= "password";

    AccountRepository mockAccountRepo = mock(AccountRepository.class);
    VolumeInstanceRepository mockVolumeInstanceRepo = mock(VolumeInstanceRepository.class);
    VolumeInstanceStatusRepository mockVolumeInstanceStatusRepo = mock(VolumeInstanceStatusRepository.class);
    VolumeSetupRepository mockVolumeSetupRepo = mock(VolumeSetupRepository.class);
    CloudProviderParametersRepository mockCloudCredentialsRepo = mock(CloudProviderParametersRepository.class);
    VolumeDeployerBash mockVolumeDeployerBash = mock(VolumeDeployerBash.class);
    DomainService domainService = mock(DomainService.class);
    CloudProviderParamsCopyRepository cloudProviderParametersCopyRepository = mock(CloudProviderParamsCopyRepository.class);
    EncryptionService encryptionService = mock(EncryptionService.class);

    VolumeInstanceRestController subject;

    @Before
    public void setUp() {
        subject = new VolumeInstanceRestController(
                mockVolumeInstanceRepo,
                mockVolumeInstanceStatusRepo,
                mockAccountRepo,
                mockVolumeSetupRepo,
                mockCloudCredentialsRepo,
                mockVolumeDeployerBash,
                domainService,
                cloudProviderParametersCopyRepository,
                encryptionService,
                salt,
                password
        );

        Properties props = new Properties();
        props.put("be.applications.root", "blah");
        props.put("be.deployments.root", "bleh");
        props.put("os.user.name", "blih");
        props.put("os.password", "bloh");
        props.put("os.tenancy.name", "bluh");
        props.put("os.auth.url", "blyh");
        subject.setProperties(props);
    }

    @Test public void
    can_delete_volume_instance_given_id() throws IOException, ApplicationDeployerException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException, InvalidKeySpecException {
        String theId = "blah";
        Account mockAccount = mockAccount();
        VolumeInstance mockVolumeInstance = mockVolumeInstance(theId, mockAccount);

        CloudProviderParameters mockCloudProviderParameters = mockVolumeInstance.getCloudProviderParameters();

        when(mockCloudCredentialsRepo.findByNameAndAccountUsername(
                mockCloudProviderParameters.getName(),
                mockAccount.getUsername()))
                .thenReturn(Optional.of(mockCloudProviderParameters));
        when(mockCloudCredentialsRepo.findByNameAndAccountUsername(
                mockCloudProviderParameters.getName(),
                mockAccount.getGivenName()))
                .thenReturn(Optional.of(mockCloudProviderParameters));

        ResponseEntity response = subject.removeVolumeInstanceByReference(theId);

        assertThat(response.getStatusCode().value(), is(200));
    }

    private VolumeInstance mockVolumeInstance(String reference, Account mockAccount) {
        VolumeInstance mockVolumeInstance = mock(VolumeInstance.class);
        when(mockVolumeInstance.getReference()).thenReturn(reference);

        CloudProviderParameters mockCpp = mock(CloudProviderParameters.class);
        when(mockCpp.getAccount()).thenReturn(mockAccount);
        when(mockCpp.getCloudProvider()).thenReturn(A_CLOUD_PROVIDER);
        when(mockCpp.getName()).thenReturn(A_CLOUD_PROVIDER);
        when(mockVolumeInstance.getCloudProviderParameters()).thenReturn(mockCpp);
        when(mockVolumeInstance.getCloudProviderParameters().getCloudProvider()).thenReturn(A_CLOUD_PROVIDER);

        VolumeSetup mockVolumeSetup = mock(VolumeSetup.class);
        when(mockVolumeSetup.getRepoPath()).thenReturn("irrelevant");
        when(mockVolumeInstance.getVolumeSetup()).thenReturn(mockVolumeSetup);

        when(mockVolumeInstance.getAccount()).thenReturn(mockAccount);
        when(mockVolumeInstanceRepo.findByReference(reference)).thenReturn(Optional.of(mockVolumeInstance));
        return mockVolumeInstance;
    }

    private Account mockAccount() {
        Account mockAccount = mock(Account.class);
        when(mockAccount.getUsername()).thenReturn(A_USER_NAME);
        when(mockAccount.getGivenName()).thenReturn(A_USER_NAME);
        when(mockAccount.getFirstJoinedDate()).thenReturn(A_DATE);
        when(mockAccount.getReference()).thenReturn(A_REFERENCE);

        when(this.mockAccountRepo.findByUsername(A_USER_NAME)).thenReturn(Optional.of(mockAccount));
        return mockAccount;
    }

}
