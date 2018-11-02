package uk.ac.ebi.tsc.portal.clouddeployment.application;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;
import uk.ac.ebi.tsc.portal.api.account.service.AccountService;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDownloaderException;


public class ApplicationDownloaderTest {
    
    ApplicationDownloader applicationDownloader;
    AccountService accountServiceMock;
    
    @Rule
    public TemporaryFolder applicationsFolder = new TemporaryFolder();
    
    @Before
    public void before() {
        
        accountServiceMock = mock(AccountService.class);
        
        applicationDownloader = new ApplicationDownloader(accountServiceMock);
    }


    @Test
    public void downloadApplication() throws Exception {
        
        when(accountServiceMock.findByUsername(anyString()))
            .thenReturn(new Account());
        
        String repoUri = "https://github.com/EMBL-EBI-TSI/cpa-instance.git";
        
        applicationDownloader.downloadApplication(applicationsFolder.getRoot().toString(), repoUri, "username");
    }
}
