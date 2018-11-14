package uk.ac.ebi.tsc;


import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;


public class DockerJavaTest {

    @Test
    public void hello_world() throws Exception {
        
        DockerClient dockerClient = newDockerClient();
        
        CreateContainerResponse container = dockerClient.createContainerCmd("hello-world")
                .withAttachStdout(true)
                .exec()
                ;
        
        String containerId = container.getId();
        
        dockerClient.startContainerCmd(containerId).exec();
        
        ContainerState state = getState(dockerClient, containerId);
        Assert.assertEquals("running" , state.getStatus());
        
        dockerClient.waitContainerCmd(containerId).exec(new WaitContainerResultCallback() {

            @Override
            public void onComplete() {

                System.out.println("Completed!!!");
                
                ContainerState state = getState(dockerClient, containerId);
                
                System.out.printf("status    = '%s'\n", state.getStatus());
                System.out.printf("exit code = '%d'\n", state.getExitCode());
                
                super.onComplete();
            }
        });
        
//        state.getStatus();
//        state.getExitCode();
//        
//        Assert.assertEquals(0  , state.getExitCode().intValue());
//        Assert.assertEquals("" , state.getStatus());
        
        
//        dockerClient.stopContainerCmd(containerId).exec();
        
        dockerClient.removeContainerCmd(containerId).exec();
        
//        dockerClient.close();
    }

    ContainerState getState(DockerClient dockerClient, String containerId) {
        return dockerClient.inspectContainerCmd(containerId).exec()
                               .getState();
    }

    @Test
    public void ps() {
        
        DockerClient dockerClient = newDockerClient();
        
        List<Container> containers = dockerClient.listContainersCmd().exec();
        
        System.out.println(containers);
    }

    DockerClient newDockerClient() {
        
        String dockerDeamonUrl = "tcp://127.0.0.1:2375"; 
        /*                        ^^^^^^^^^^^^^^^^^^^^
         * You have to configure the docker deamon to listen on this port.
         * 
         * Create this file:
         * 
         *     /etc/systemd/system/docker.service.d/docker.conf
         *     
         * with this content:
         * 
         *     [Service]
         *     ExecStart=
         *     ExecStart=/usr/bin/dockerd -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock
         *
         * Then run:
         * 
         *     systemctl daemon-reload              # reload configuration
         *     systemctl restart docker.service     # restart the deamon
         *     
         * To test:
         * 
         *     $ docker -H 'tcp://127.0.0.1:2375' ps
         *     CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
         *     
         * This can also be useful:
         * 
         *      $ systemctl status docker.service
         *      ● docker.service - Docker Application Container Engine
         *         Loaded: loaded (/lib/systemd/system/docker.service; disabled; vendor preset: enabled)
         *        Drop-In: /etc/systemd/system/docker.service.d
         *                 └─docker.conf
         *         Active: active (running) since Mon 2018-10-29 12:01:24 GMT; 20min ago
         *           Docs: https://docs.docker.com
         *       Main PID: 27483 (dockerd)
         *          Tasks: 61
         *         CGroup: /system.slice/docker.service
         *                 ├─27483 /usr/bin/dockerd -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock        # <== HERE
         *                 └─27506 docker-containerd --config /var/run/docker/containerd/containerd.toml
         *      
         *      Oct 29 12:01:21 dockerd[27483]: time="2018-10-29T12:01:21.313266833Z" level=warning msg="Your kernel does not support cgroup rt period"
         *      Oct 29 12:01:21 dockerd[27483]: time="2018-10-29T12:01:21.313280840Z" level=warning msg="Your kernel does not support cgroup rt runtime"
         *      Oct 29 12:01:21 dockerd[27483]: time="2018-10-29T12:01:21.313959624Z" level=info msg="Loading containers: start."
         *      Oct 29 12:01:23 dockerd[27483]: time="2018-10-29T12:01:23.721862885Z" level=info msg="Default bridge (docker0) is assigned with an IP address 172.17.0.0/16. Daemon option --bip can be used to set a preferre
         *      Oct 29 12:01:24 dockerd[27483]: time="2018-10-29T12:01:24.568336955Z" level=info msg="Loading containers: done."
         *      Oct 29 12:01:24 dockerd[27483]: time="2018-10-29T12:01:24.647719607Z" level=info msg="Docker daemon" commit=7390fc6 graphdriver(s)=overlay2 version=17.12.1-ce
         *      Oct 29 12:01:24 dockerd[27483]: time="2018-10-29T12:01:24.647904252Z" level=info msg="Daemon has completed initialization"
         *      Oct 29 12:01:24 dockerd[27483]: time="2018-10-29T12:01:24.651493757Z" level=info msg="API listen on /var/run/docker.sock"
         *      Oct 29 12:01:24 dockerd[27483]: time="2018-10-29T12:01:24.651547643Z" level=info msg="API listen on [::]:2375"                  # <== AND HERE
         *      Oct 29 12:01:24 systemd[1]: Started Docker Application Container Engine.
         *
         *     
         * 
         * [https://stackoverflow.com/a/42204921/1553043]
         */
        
        return DockerClientBuilder.getInstance(dockerDeamonUrl)
               .build()
               ;
    }
}
