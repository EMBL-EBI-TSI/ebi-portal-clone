package uk.ac.ebi.tsc.portal.api.encryptdecrypt.security;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopy;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Service
public class EncryptionServiceImpl implements EncryptionService{

	private final String salt, password;
	
	@Autowired
	public EncryptionServiceImpl(@Value("${ecp.security.salt}") final String salt, 
			@Value("${ecp.security.password}") final String password){
		this.salt = salt;
		this.password = password;
	}

	@Override
	public Map<String, String> encrypt(CloudProviderParameters cloudProviderParameters) {

		Map<String, String> encryptedValues = new HashMap<>();

		TextEncryptor encryptor = Encryptors.text(salt, toHex(password));
		
		cloudProviderParameters.getFields().stream()
		.forEach(f -> encryptedValues.put(f.getKey(), encryptor.encrypt(f.getValue())));
		
		return encryptedValues;
	}
	
	@Override
	public Map<String, String> decryptOne(CloudProviderParameters encryptedCloudProviderParameters) {

		Map<String, String> decryptedValues = new HashMap<>();

		TextEncryptor encryptor = Encryptors.text(salt, toHex(password));
		
		encryptedCloudProviderParameters.getFields().stream()
		.forEach(f -> decryptedValues.put(f.getKey(), encryptor.decrypt(f.getValue())));
		
		return decryptedValues;
	}

	@Override
	public Map<String, String> encrypt(CloudProviderParamsCopy cloudProviderParamsCopy) {
		
		Map<String, String> encryptedValues = new HashMap<>();

		TextEncryptor encryptor = Encryptors.text(salt, toHex(password));
		
		cloudProviderParamsCopy.getFields().stream()
		.forEach(f -> encryptedValues.put(f.getKey(), encryptor.encrypt(f.getValue())));
		
		return encryptedValues;
	}

	@Override
	public Map<String, String> decryptOne(CloudProviderParamsCopy encryptedCloudProviderParamsCopy) {
		
		Map<String, String> decryptedValues = new HashMap<>();

		TextEncryptor encryptor = Encryptors.text(salt, toHex(password));
		
		encryptedCloudProviderParamsCopy.getFields().stream()
		.forEach(f -> decryptedValues.put(f.getKey(), encryptor.decrypt(f.getValue())));
		
		return decryptedValues;
	}
	
	public String toHex(String arg) {
		return String.format("%040x", new BigInteger(1, arg.getBytes()));
	}
}
