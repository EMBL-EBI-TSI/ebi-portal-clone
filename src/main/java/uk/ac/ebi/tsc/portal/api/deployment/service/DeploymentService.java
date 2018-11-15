package uk.ac.ebi.tsc.portal.api.deployment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import uk.ac.ebi.tsc.portal.api.deployment.repo.*;

import java.util.Collection;
import java.util.List;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Component
public class DeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentService.class);

    private final DeploymentRepository deploymentRepository;
    private final DeploymentStatusRepository deploymentStatusRepository;

    public DeploymentService(DeploymentRepository deploymentRepository,
                             DeploymentStatusRepository deploymentStatusRepository) {
        this.deploymentRepository = deploymentRepository;
        this.deploymentStatusRepository = deploymentStatusRepository;
    }

    public Collection<Deployment> findByAccountUsername(String username) {
        return deploymentRepository.findByAccountUsername(username);
    }

    public Collection<Deployment> findByDeploymentStatusStatus(DeploymentStatusEnum status) {
        return deploymentRepository.findByDeploymentStatusStatus(status);
    }

    public Deployment findByAccountUsernameAndId(String username, Long id) {
        return this.deploymentRepository.findByAccountUsernameAndId(username, id).orElseThrow(
                () -> new DeploymentNotFoundException(username, id));
    }

    public Deployment save(Deployment deployment) {
        return this.deploymentRepository.save(deployment);
    }

    public void delete(Long deploymentId) {
        this.deploymentRepository.delete(deploymentId);
    }


    public DeploymentStatus findStatusByDeploymentId(Long deploymentId) {
        return this.deploymentStatusRepository.findByDeploymentId(deploymentId).orElseThrow(
                () -> new DeploymentStatusNotFoundException(deploymentId));
    }

    public Deployment findByReference(String reference) {
        return this.deploymentRepository.findByReference(reference).orElseThrow(
                () -> new DeploymentNotFoundException(reference));
    }

    public Deployment findByAccessIp(String ip) {
        return this.deploymentRepository.findByAccessIp(ip).orElseThrow(
                () -> new DeploymentNotFoundException(ip));
    }
    
    public List<Deployment> findByDeploymentApplicationId(Long id){
    	return this.deploymentRepository.findByDeploymentApplicationId(id);
    }
    
    public List<Deployment> findAll(){
    	return this.deploymentRepository.findAll();
    }

    public Collection<Deployment> findByConfigurationReference(String reference) {
        return this.deploymentRepository.findByDeploymentConfigurationConfigurationReference(reference);
    }
}
