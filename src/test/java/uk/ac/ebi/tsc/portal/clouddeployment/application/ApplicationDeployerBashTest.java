package uk.ac.ebi.tsc.portal.clouddeployment.application;

import static org.junit.Assert.*;

import org.junit.Test;

public class ApplicationDeployerBashTest {
    
    @Test
    public void scriptPath() throws Exception {
        
        ApplicationDeployerBash deployer = new ApplicationDeployerBash(null, null, null, null, null, null, null);
        
        assertEquals("/app/ostack/deploy.sh", deployer.scriptPath("ostack"));
    }
    
    @Test
    public void dockerCmd() throws Exception {
        
        ApplicationDeployerBash deployer = new ApplicationDeployerBash(null, null, null, null, null, null, null);
        
        String[] cmd = deployer.dockerCmd("/var/ecp/myapp", "/var/ecp/deployments", "ostack");
        
        assertArrayEquals(new String[] {
                
            "docker", "run", "-v", "/var/ecp/myapp:/app"
                           , "-v", "/var/ecp/deployments:/deployments"
                           , "--entrypoint", ""
                           , "erikvdbergh/ecp-agent"                                     
                           , "/app/ostack/deploy.sh"
        }
        , cmd);
    }
}
