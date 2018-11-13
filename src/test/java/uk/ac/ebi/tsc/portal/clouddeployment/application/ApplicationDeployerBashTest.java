package uk.ac.ebi.tsc.portal.clouddeployment.application;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;


public class ApplicationDeployerBashTest {
    
    @Test
    public void scriptPath() throws Exception {
        
        ApplicationDeployerBash deployer = newDeployer();
        
        assertEquals("/app/ostack/deploy.sh", deployer.scriptPath("ostack"));
    }

    @Test
    public void dockerCmd() throws Exception {
        
        ApplicationDeployerBash deployer = newDeployer();
        
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
    
    @Test
    public void envToOpts() throws Exception {
        
        ApplicationDeployerBash deployer = newDeployer();
        
        Map<String, String> env = new HashMap<String, String>();
        
        env.put("a", "1");
        env.put("b", "2");
        
        List<String> r = deployer.envToOpts(env);
        
        assertEquals( asList("-e", "a=1", "-e", "b=2")
                    , r
                    );
    }
    
    ApplicationDeployerBash newDeployer() {
        
        return new ApplicationDeployerBash(null, null, null, null, null, null, null);
    }
}
