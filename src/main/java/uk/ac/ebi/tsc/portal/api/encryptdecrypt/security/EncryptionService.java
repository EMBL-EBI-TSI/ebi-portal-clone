package uk.ac.ebi.tsc.portal.api.encryptdecrypt.security;

import java.util.Collection;
import java.util.Map;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public interface EncryptionService {
	Map<String, String> encrypt(Map<String, String> params, Object ...values);
	Map<String, String> decryptOne(Map<String, String> params, Object ...values);
	Collection<Map<String, String>> decryptAll(Collection<Map<String, String>> fields, Object ...values);
}
