package uk.ac.ebi.tsc;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerExit;
import com.spotify.docker.client.messages.ExecState;

public class DockerClientTest {

    DockerClient docker;
    
    @Before
    public void before() {
        
        docker = newDockerClient();
    }
    
    
    @Test
    public void ps() throws DockerException, InterruptedException {
        
        List<Container> containers = docker.listContainers();
        
        System.out.println(containers.size());
    }


    int numContainers() throws DockerException, InterruptedException {
        
        List<Container> containers = docker.listContainers(ListContainersParam.allContainers());
        
        return containers.size();
    }
    
    @Test
    public void ps_with_filter() throws DockerException, InterruptedException {
        
        List<Container> containers = listContainersByName("tfga");
        
        System.out.printf("Containers = %d\n", containers.size());
    }


    /**
     * docker ps -a --filter 'name={partOfName}'
     * 
     */
    List<Container> listContainersByName(String partOfName) throws DockerException, InterruptedException {
        
        List<Container> containers = docker.listContainers(
                
             ListContainersParam.allContainers()
            ,ListContainersParam.filter("name", partOfName)
        );
        
        return containers;
    }
    
    @Test
    public void hello_world() throws Exception {
        
        runDebug(newContainer("hello-world"));
    }
    
    public void runDebug(ContainerConfig containerConfig) throws Exception {
        
        int containersBefore = numContainers();
        
        // ------------------------------------------------------------------------------------------------------------
        
        ContainerExit exit = runAndRemove(containerConfig);
        
        // ------------------------------------------------------------------------------------------------------------
        
        int containersAfter = numContainers();
        
        System.out.printf("Status code = %d\n", exit.statusCode());
        System.out.printf("containersBefore = %d\n", containersBefore);
        System.out.printf("containersAfter  = %d\n", containersAfter);
        
        assertEquals(containersAfter, containersBefore);
    }
    
    ContainerConfig newContainer(String image, String... envs) {
        
        return ContainerConfig.builder()
                .image(image)
                .env(envs)
                .build()
                ;
    }
    
    ContainerCreation startContainer(ContainerConfig containerConfig) {
        
        ContainerCreation container;
        try 
        {
            container = docker.createContainer(containerConfig);
        } 
        catch (DockerException | InterruptedException e) 
        {
            throw new RuntimeException(e);
        }
        
        return container;
    }

    ContainerExit runAndRemove(ContainerConfig containerConfig) throws DockerException, InterruptedException {
        
        ContainerCreation container = startContainer(containerConfig);
        
        String id = container.id();
        
//        String logs;
//        try (LogStream stream = docker.logs(id, LogsParam.stdout(), LogsParam.stderr())) {
//            
//            logs = stream.readFully(); // NPE
//            
//            System.out.println("************************************************************");
//            System.out.println(logs);
//            System.out.println("************************************************************");
//        }
        
        docker.startContainer(id);
        
        ContainerExit exit = docker.waitContainer(id);
        
        docker.removeContainer(id);
        
        return exit;
    }
    
    @Test
    public void run_eriks_image() throws Exception {
        
        /*
         * Image's entry point:
         * 
         *      git clone "$PORTAL_APP_REPO_URL" "$PORTAL_APP_REPO_FOLDER"
         *      /bin/sh -c "$@"
         */
        
//        docker run -e "PORTAL_APP_REPO_URL=https://github.com/EMBL-EBI-TSI/cpa-instance.git" -e "PORTAL_APP_REPO_FOLDER=cpa-instance"   erikvdbergh/ecp-agent 'ls -la cpa-instance'

        runDebug(ContainerConfig.builder()
                        .image("erikvdbergh/ecp-agent")
                        .env( "PORTAL_APP_REPO_URL=https://github.com/EMBL-EBI-TSI/cpa-instance.git"
                            , "PORTAL_APP_REPO_FOLDER=cpa-instance")
                        .cmd("ls -la cpa-instance")
                        .build()
        );
    }
    
    DefaultDockerClient newDockerClient() {
        
        return new DefaultDockerClient("http://127.0.0.1:2375");
    }
}
