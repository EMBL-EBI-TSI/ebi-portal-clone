package uk.ac.ebi.tsc.portal.api.deployment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.tsc.portal.api.deployment.repo.Deployment;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentSecret;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentSecretRepository;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.SecretGenerator;

import java.util.Optional;

@Component
public class DeploymentSecretService {

    @Autowired
    SecretGenerator secretGenerator;
    
    @Autowired
    DeploymentSecretRepository stopMeSecretRepository;
    

    public String create(Deployment deployment) {
        
        String secret = secretGenerator.generate();
        
        save(deployment, secret);
        
        return secret;
    }

    void save(Deployment aDeployment, String secret) {
        
        DeploymentSecret sms = new DeploymentSecret(aDeployment, secret);
        
        stopMeSecretRepository.save(sms);
    }
    
    public boolean exists(String reference, String secret) {
        
        Optional<DeploymentSecret> r = stopMeSecretRepository.findByDeploymentReferenceAndSecret(reference, secret);
        
        return r.isPresent();
    }
}
