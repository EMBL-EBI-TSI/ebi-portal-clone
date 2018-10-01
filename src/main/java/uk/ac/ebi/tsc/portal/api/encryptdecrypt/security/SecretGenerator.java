package uk.ac.ebi.tsc.portal.api.encryptdecrypt.security;

import org.springframework.stereotype.Component;


@Component
public class SecretGenerator {

    public String generate() {
        
        return "12345";         // TODO generate random number / UUID?
    }
}
