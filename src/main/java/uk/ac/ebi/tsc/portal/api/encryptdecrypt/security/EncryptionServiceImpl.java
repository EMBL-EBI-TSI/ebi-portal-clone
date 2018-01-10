package uk.ac.ebi.tsc.portal.api.encryptdecrypt.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class EncryptionServiceImpl implements EncryptionService{
	
	@Override
	public Map<String, String> encrypt(Map<String, String> params, Object ...values) {

		Map<String, String> encryptedValues = new HashMap<>();

		TextEncryptor encryptor = Encryptors.text(values[0].toString(), values[1].toString());

		for(Map.Entry<String, String> entry : params.entrySet()){
			String key = entry.getKey();
			String value = entry.getValue();
			encryptedValues.put(key, encryptor.encrypt(value));
		}
		
		return encryptedValues;
	}

	@Override
	public Map<String, String> decryptOne(Map<String, String> params, Object ...values) {

		Map<String, String> decryptedValues = new HashMap<>();

		TextEncryptor encryptor = Encryptors.text(values[0].toString(), values[1].toString());
		
		for(Map.Entry<String, String> entry : params.entrySet()){
			String key = entry.getKey();
			String value = entry.getValue();
			decryptedValues.put(key, encryptor.decrypt(value));
		}
		
		return decryptedValues;
	}

	@Override
	public Collection<Map<String, String>> decryptAll(Collection<Map<String, String>> encryptedAll, Object ...values) {

		Collection<Map<String, String>> decryptedRes = new LinkedList<>();

		for (Map<String, String> encryptedValues : encryptedAll) {
			decryptedRes.add(decryptOne(encryptedValues));
		}

		return decryptedRes;

	}
}
