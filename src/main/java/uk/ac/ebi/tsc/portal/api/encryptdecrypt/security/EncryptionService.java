package uk.ac.ebi.tsc.portal.api.encryptdecrypt.security;

import java.util.Collection;
import java.util.Map;

public interface EncryptionService {
	Map<String, String> encrypt(Map<String, String> params, Object ...values);
	Map<String, String> decryptOne(Map<String, String> params, Object ...values);
	Collection<Map<String, String>> decryptAll(Collection<Map<String, String>> fields, Object ...values);
}
