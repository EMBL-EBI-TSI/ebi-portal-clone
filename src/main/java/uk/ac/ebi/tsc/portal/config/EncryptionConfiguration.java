package uk.ac.ebi.tsc.portal.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionServiceImpl;


@Configuration
@Import(EncryptionServiceImpl.class)
class EncryptionConfiguration{

	@Autowired
	EncryptionServiceImpl encryptionService;
	
    @Bean
    public EncryptionServiceImpl encryptionService() {
    	EncryptionServiceImpl es = encryptionService;
    	return es;
    }

}