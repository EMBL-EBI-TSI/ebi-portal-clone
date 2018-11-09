package uk.ac.ebi.tsc.portal.clouddeployment.application;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ApplicationDeployerBashTest {
    
    @Test
    public void scriptPath() throws Exception {
        
        ApplicationDeployerBash deployer = new ApplicationDeployerBash(null, null, null, null, null, null, null);
        
        assertEquals("/app/ostack/deploy.sh", deployer.scriptPath("ostack"));
    }
}
