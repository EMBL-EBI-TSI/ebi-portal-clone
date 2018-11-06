package uk.ac.ebi.tsc.portal.clouddeployment.application;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import io.vavr.Tuple2;
import io.vavr.control.Either;
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
        
        ProcessRunner processRunnerMock = new ProcessRunner() {

            @Override
            Either<Tuple2<Integer, String>, Integer> run(String... cmd) {
                
                System.out.println(String.format("[run] >>> %s", String.join(" ", cmd)));
                
                return super.run(cmd);
            }
        };
        
        applicationDownloader = new ApplicationDownloader(accountServiceMock, processRunnerMock);
    }


    @Test
    public void downloadApplication() throws Exception {
        
        when(accountServiceMock.findByUsername(anyString()))
            .thenReturn(new Account());
        
        String repoUri = "https://github.com/EMBL-EBI-TSI/cpa-instance.git";
        
        applicationDownloader.downloadApplication(applicationsFolder.getRoot().toString(), repoUri, "username");
    }
}
