package uk.ac.ebi.tsc.portal.api.volumeinstance.controller;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Date;
import java.util.Optional;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;
import uk.ac.ebi.tsc.portal.api.account.service.AccountService;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParametersService;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstance;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceRepository;
import uk.ac.ebi.tsc.portal.api.volumeinstance.service.VolumeInstanceService;
import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetup;
import uk.ac.ebi.tsc.portal.api.volumesetup.service.VolumeSetupService;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDeployerException;
import uk.ac.ebi.tsc.portal.clouddeployment.volume.VolumeDeployerBash;

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

    @MockBean
    AccountService accountService; 
        
    @MockBean
    VolumeSetupService volumeSetupService;
    
    @MockBean
    CloudProviderParametersService cloudCredentialsService;
    
    @MockBean
    VolumeInstanceService volumeInstanceService;
    
    @MockBean
    VolumeDeployerBash volumeDeployerBash;
    
    @MockBean
    VolumeInstanceRestController subject;

    @Before
    public void setUp() {
        Properties props = new Properties();
        props.put("be.applications.root", "blah");
        props.put("be.deployments.root", "bleh");
        props.put("os.user.name", "blih");
        props.put("os.password", "bloh");
        props.put("os.tenancy.name", "bluh");
        props.put("os.auth.url", "blyh");
        subject.setProperties(props);
        ReflectionTestUtils.setField(subject, "cloudProviderParametersService", cloudCredentialsService);
        ReflectionTestUtils.setField(subject, "volumeSetupService", volumeSetupService);
        ReflectionTestUtils.setField(subject, "accountService", accountService);
        ReflectionTestUtils.setField(subject, "volumeInstanceService", volumeInstanceService);
        ReflectionTestUtils.setField(subject, "volumeDeployerBash", volumeDeployerBash);
    }

    @Test public void
    can_delete_volume_instance_given_id() throws IOException, ApplicationDeployerException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException, InvalidKeySpecException {
        String theId = "blah";
        Account mockAccount = mockAccount();
        VolumeInstance mockVolumeInstance = mockVolumeInstance(theId, mockAccount);

        CloudProviderParameters mockCloudProviderParameters = mockVolumeInstance.getCloudProviderParameters();

        when(cloudCredentialsService.findByNameAndAccountUsername(
                mockCloudProviderParameters.getName(),
                mockAccount.getUsername()))
                .thenReturn(mockCloudProviderParameters);
        
        when(subject.removeVolumeInstanceByReference(theId)).thenCallRealMethod();
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
        when(volumeInstanceService.findByReference(reference)).thenReturn(mockVolumeInstance);
        return mockVolumeInstance;
    }

    private Account mockAccount() {
        Account mockAccount = mock(Account.class);
        when(mockAccount.getUsername()).thenReturn(A_USER_NAME);
        when(mockAccount.getGivenName()).thenReturn(A_USER_NAME);
        when(mockAccount.getFirstJoinedDate()).thenReturn(A_DATE);
        when(mockAccount.getReference()).thenReturn(A_REFERENCE);

        when(this.accountService.findByUsername(A_USER_NAME)).thenReturn(mockAccount);
        return mockAccount;
    }

}
