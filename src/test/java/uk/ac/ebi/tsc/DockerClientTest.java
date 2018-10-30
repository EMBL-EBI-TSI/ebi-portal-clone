package uk.ac.ebi.tsc;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
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
    
    @Test
    public void hello_world() throws Exception {
        
        ContainerCreation container = docker.createContainer(ContainerConfig.builder()
                                                                            .image("hello-world")
                                                                            .build()
                                                            );
        
        String id = container.id();
        
        docker.startContainer(id);
        
        ContainerExit exit = docker.waitContainer(id);
        
        System.out.printf("Status code = %d\n", exit.statusCode());
    }
    
    DefaultDockerClient newDockerClient() {
        
        return new DefaultDockerClient("http://127.0.0.1:2375");
    }
}
