package uk.ac.ebi.tsc.portal.clouddeployment.application;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.underscore.U;

public class ApplicationDeployerBashTest {
    
    @Test
    public void scriptPath() throws Exception {
        
        ApplicationDeployerBash deployer = newDeployer();
        
        assertEquals("/app/ostack/deploy.sh", deployer.scriptPath("ostack", "deploy.sh"));
    }

    @Test
    public void dockerCmd() throws Exception {
        
        ApplicationDeployerBash deployer = newDeployer();
        
        Map<String, String> env = new HashMap<String, String>();
        
        env.put("a", "1");
        env.put("b", "2");
        
        List<String> cmd = deployer.dockerCmd("/var/ecp/myapp", "/var/ecp/deployments", "ostack", "deploy.sh", env);
        
        assertEquals(asList(
                
            "docker", "run", "-v", "/var/ecp/myapp:/app"
                           , "-v", "/var/ecp/deployments:/deployments"
                           , "-e", "a=1"
                           , "-e", "b=2"
                           , "--entrypoint", ""
                           , "erikvdbergh/ecp-agent"                                     
                           , "/app/ostack/deploy.sh"
        )
        , cmd);
    }
    
    @Test
    public void dockerCmd_without_appFolder() throws Exception {
            
        ApplicationDeployerBash deployer = newDeployer();
        
        List<String> cmd = deployer.dockerCmd("/var/ecp/deployments", "ostack", "deploy.sh", new HashMap<String, String>());
        
        assertEquals(asList(
                
            "docker", "run", "-v", "/var/ecp/deployments:/deployments"
                           , "--entrypoint", ""
                           , "erikvdbergh/ecp-agent"                                     
                           , "/app/ostack/deploy.sh"
        )
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
    
    @Test
    public void volume() throws Exception {
        
        ApplicationDeployerBash deployer = newDeployer();
        
        assertEquals(asList("-v", "/var/ecp/myapp:/app")    , deployer.volume("/var/ecp/myapp" , "/app"));
        assertEquals(asList()                               , deployer.volume(null             , "/app"));
    }
    
    ApplicationDeployerBash newDeployer() {
        
        return new ApplicationDeployerBash(null, null, null, null, null, null, null);
    }
}
