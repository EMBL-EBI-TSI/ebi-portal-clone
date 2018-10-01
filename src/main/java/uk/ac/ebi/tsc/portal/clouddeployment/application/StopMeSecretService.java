package uk.ac.ebi.tsc.portal.clouddeployment.application;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.ebi.tsc.portal.api.deployment.repo.Deployment;
import uk.ac.ebi.tsc.portal.api.deployment.repo.StopMeSecret;
import uk.ac.ebi.tsc.portal.api.deployment.repo.StopMeSecretRepository;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.SecretGenerator;

@Component
public class StopMeSecretService {

    @Autowired
    SecretGenerator secretGenerator;
    
    @Autowired
    StopMeSecretRepository stopMeSecretRepository;
    

    public String create(Deployment deployment) {
        
        String secret = secretGenerator.generate();
        
        save(deployment, secret);
        
        return secret;
    }

    void save(Deployment aDeployment, String secret) {
        
        StopMeSecret sms = new StopMeSecret(aDeployment, secret);
        
        stopMeSecretRepository.save(sms);
    }
    
    public boolean exists(String reference, String secret) {
        
        Optional<StopMeSecret> r = stopMeSecretRepository.findByDeploymentReferenceAndSecret(reference, secret);
        
        return r.isPresent();
    }
}
