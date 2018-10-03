package uk.ac.ebi.tsc.portal.clouddeployment.application;

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ebi.tsc.util.JsonUtil.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import uk.ac.ebi.tsc.portal.BePortalApiApplication;
import uk.ac.ebi.tsc.portal.api.deployment.repo.Deployment;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.StopMeSecretRepository;
import uk.ac.ebi.tsc.portal.config.WebConfiguration;
import uk.ac.ebi.tsc.util.JsonUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {WebConfiguration.class, BePortalApiApplication.class})
@TestPropertySource("classpath:integrationTest.properties")
@AutoConfigureMockMvc
public class StopMeIT {

    @Autowired
    StopMeSecretRepository stopMeSecretRepository;
    
    @Autowired
    StopMeSecretService stopMeSecretService;
    
    @Autowired
    DeploymentRepository deploymentRepository;
    
    @Autowired
    private MockMvc mockMvc;

    Deployment aDeployment;
    String reference = "TSI000000001";
    static final String SECRET = "secret";
    
    
    @Before
    public void before() {
        
        // I need to have at least one record in 'deployment'
        
        // Erasing all the records
        stopMeSecretRepository.deleteAll();  // Need to erase these first ( FK(deployment.id) )
        deploymentRepository.deleteAll();
        
        // Creating the one I need
        this.aDeployment = deploymentRepository.save(new Deployment(reference, null, null, "cloudProviderParametersReference", null, null));
    }
    
    @Test
    public void save() throws Exception 
    {
        assertFalse(stopMeSecretService.exists(reference, SECRET));
        
        stopMeSecretService.save(aDeployment, SECRET);
        
        assertTrue(stopMeSecretService.exists(reference, SECRET));
    }
    
    @Test
    public void stopMe_non_existent_deployment() throws Exception 
    {
        assertFalse(stopMeSecretService.exists(reference, SECRET));
        
        ResultActions r = callStopMe(reference, SECRET);
        assert404(r);
    }
    
    @Test
    public void stopMe_wrong_secret() throws Exception 
    {
        save();
        assertTrue(stopMeSecretService.exists(reference, SECRET));
        
        ResultActions r = callStopMe(reference, "wrongSecret");
        assert404(r);
    }

    ResultActions callStopMe(String reference, String secret) throws Exception {
        
        String content = obj( keyValue("secret", secret) );

        return mockMvc.perform(
                
                put(format("/deployment/%s/stopme", reference))
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
        );
    }
    
    void assert404(ResultActions r) throws Exception {
        
        r.andExpect(status().is(404))
        
        /*
         * 
         * [
         *     { "logref":     "error"
         *     , "message":    "Could not find deployment with reference 'TSI000000000001'."
         *     ,"links":       []
         *     }
         * ]
         * 
         */
        .andExpect(jsonPath("[0].message").value("Could not find deployment with reference 'TSI000000001'."))
        ;
    }
}
