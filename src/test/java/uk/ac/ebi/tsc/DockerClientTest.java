package uk.ac.ebi.tsc;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerExit;

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
        
        int containersBefore = numContainers();
        
        // ------------------------------------------------------------------------------------------------------------
        
        ContainerCreation container = docker.createContainer(ContainerConfig.builder()
                                                                            .image("hello-world")
                                                                            .build()
                                                            );
        
        String id = container.id();
        
        docker.startContainer(id);
        
        ContainerExit exit = docker.waitContainer(id);
        
        docker.removeContainer(id);
        
        // ------------------------------------------------------------------------------------------------------------
        
        int containersAfter = numContainers();
        
        System.out.printf("Status code = %d\n", exit.statusCode());
        System.out.printf("containersBefore = %d\n", containersBefore);
        System.out.printf("containersAfter  = %d\n", containersAfter);
        
        assertEquals(containersAfter, containersBefore);
    }
    
    DefaultDockerClient newDockerClient() {
        
        return new DefaultDockerClient("http://127.0.0.1:2375");
    }
}
