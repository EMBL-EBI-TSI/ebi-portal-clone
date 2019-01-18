package uk.ac.ebi.tsc.portal.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Configuration
class EncryptionConfiguration{

	@Autowired
	EncryptionService encryptionService;
	
    @Bean
    public EncryptionService encryptionService() {
    	EncryptionService es = encryptionService;
    	return es;
    }

}