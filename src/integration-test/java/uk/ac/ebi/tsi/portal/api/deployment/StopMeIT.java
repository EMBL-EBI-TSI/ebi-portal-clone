package uk.ac.ebi.tsi.portal.api.deployment;

import static org.junit.Assert.assertFalse;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.tsc.portal.BePortalApiApplication;
import uk.ac.ebi.tsc.portal.api.deployment.repo.StopMeSecret;
import uk.ac.ebi.tsc.portal.api.deployment.repo.StopMeSecretRepository;
import uk.ac.ebi.tsc.portal.config.WebConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {WebConfiguration.class, BePortalApiApplication.class})
@TestPropertySource("classpath:integrationTest.properties")
@AutoConfigureMockMvc
public class StopMeIT {

    @Autowired
    StopMeSecretRepository stopMeSecretRepository;
    
    @Test
    public void findAll() throws Exception 
    {
        stopMeSecretRepository.findAll();
    }
    
    @Test
    public void findByDeploymentIdAndSecret() throws Exception 
    {
        Optional<StopMeSecret> r = stopMeSecretRepository.findByDeploymentIdAndSecret(1, "secret");
        
        assertFalse(r.isPresent());
    }
}
