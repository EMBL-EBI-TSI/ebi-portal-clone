package uk.ac.ebi.tsc.portal.api.volumesetup.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDownloaderException;
import uk.ac.ebi.tsc.portal.clouddeployment.volume.VolumeSetupDownloader;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetup;
import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetupRepository;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
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
@RunWith(SpringRunner.class)
@WebAppConfiguration
public class VolumeSetupRestControllerTest {

    private static final String APPS_ROOT_FOLDER = "/a/path/has/no/name";

    private static final int CREATED_HTTP_STATUS = 201;
    private static final int OK_HTTP_STATUS = 200;

    VolumeSetupRepository mockVolumeSetupRepo = mock(VolumeSetupRepository.class);
    VolumeSetupDownloader mockVolumeSetupDownloader = mock(VolumeSetupDownloader.class);

    private Account accountMock;
    private Principal principalMock;

    VolumeSetupRestController subject;

    @Before
    public void setUp() {
        subject = new VolumeSetupRestController(mockVolumeSetupRepo, mockVolumeSetupDownloader);

        Properties props = new Properties();
        props.put("be.applications.root", APPS_ROOT_FOLDER);

        subject.setProperties(props);

        this.principalMock = mock(Principal.class);
        when(this.principalMock.getName()).thenReturn("A user name");

        this.accountMock = mock(Account.class);
        when(this.accountMock.getId()).thenReturn(1L);
        when(this.accountMock.getUsername()).thenReturn("A user name");
        when(this.accountMock.getEmail()).thenReturn("an@email.com");
        when(this.accountMock.getPassword()).thenReturn("A password");
        when(this.accountMock.getOrganisation()).thenReturn("An organisation");

    }

    @Test public void
    can_add_volume_setup_given_repo_uri() throws IOException, ApplicationDownloaderException {
        String theUri = "blah";
        String theName = "an-app-has-no-name";
        VolumeSetup mockVolumeSetup = mockVolumeSetup(theUri, APPS_ROOT_FOLDER + File.separator + theName, theName);

        when(mockVolumeSetupRepo.save(mockVolumeSetup)).thenReturn(mockVolumeSetup);
        VolumeSetupResource inputResource = new VolumeSetupResource(mockVolumeSetup);
        when(mockVolumeSetupRepo.findByAccountUsernameAndName(this.accountMock.getUsername(), inputResource.getName())).thenReturn(Optional.empty());
        when(mockVolumeSetupDownloader.downloadVolumeSetup(APPS_ROOT_FOLDER, inputResource.getRepoUri(), this.accountMock.getUsername())).thenReturn(mockVolumeSetup);

        // do the request
        ResponseEntity response = subject.add(this.principalMock,inputResource);

        // check assertions
        assertThat(response.getStatusCode().value(), is(CREATED_HTTP_STATUS));

    }

    @Test public void
    can_delete_volume_setup_given_repo_uri() throws IOException, ApplicationDownloaderException {
        String theUri = "blah";
        String theName = "an-app-has-no-name";
        mockSavedVolumeSetup(theUri, APPS_ROOT_FOLDER + File.separator + theName, theName);

        ResponseEntity response = subject.deleteVolumeSetupByAccountUsernameAndName(this.principalMock,theName);

        assertThat(response.getStatusCode().value(), is(OK_HTTP_STATUS));
    }

    private VolumeSetup mockVolumeSetup(String repoUri, String repoPath, String name) throws IOException, ApplicationDownloaderException {
        VolumeSetup mockVolumeSetup = mock(VolumeSetup.class);

        when(mockVolumeSetup.getRepoPath()).thenReturn(repoPath);
        when(mockVolumeSetup.getName()).thenReturn(name);
        when(mockVolumeSetup.getRepoUri()).thenReturn(repoUri);
        when(mockVolumeSetup.getId()).thenReturn(1L);
        when(mockVolumeSetup.getAccount()).thenReturn(this.accountMock);

        return mockVolumeSetup;
    }

    private VolumeSetup mockSavedVolumeSetup(String repoUri, String repoPath, String name) throws IOException, ApplicationDownloaderException {
        VolumeSetup mockVolumeSetup = mock(VolumeSetup.class);
        when(mockVolumeSetup.getRepoPath()).thenReturn(repoPath);
        when(mockVolumeSetup.getName()).thenReturn(name);
        when(mockVolumeSetup.getRepoUri()).thenReturn(repoUri);
        when(mockVolumeSetup.getId()).thenReturn(1L);

        when(mockVolumeSetupRepo.findByAccountUsernameAndName(this.principalMock.getName(),name)).thenReturn(Optional.of(mockVolumeSetup));
        when(mockVolumeSetupRepo.findById(1L)).thenReturn(Optional.of(mockVolumeSetup));

        when(mockVolumeSetupDownloader.removeVolumeSetup(mockVolumeSetup)).thenReturn(0);
        when(mockVolumeSetupDownloader.downloadVolumeSetup(APPS_ROOT_FOLDER, repoUri, this.principalMock.getName())).thenReturn(mockVolumeSetup);

        return mockVolumeSetup;
    }

}
