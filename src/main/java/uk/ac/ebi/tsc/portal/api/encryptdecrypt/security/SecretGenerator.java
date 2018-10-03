package uk.ac.ebi.tsc.portal.api.encryptdecrypt.security;

import java.util.UUID;

import org.springframework.stereotype.Component;


@Component
public class SecretGenerator {

    public String generate() {
        
        return UUID.randomUUID().toString();
    }
}
