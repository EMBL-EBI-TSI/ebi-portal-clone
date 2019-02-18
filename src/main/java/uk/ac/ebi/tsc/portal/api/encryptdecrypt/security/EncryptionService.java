package uk.ac.ebi.tsc.portal.api.encryptdecrypt.security;

import java.util.Map;

import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopy;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public interface EncryptionService {
	Map<String, String> encrypt(CloudProviderParameters cloudProviderParameters);
	Map<String, String> decryptOne(CloudProviderParameters cloudProviderParameters);
	Map<String, String> encrypt(CloudProviderParamsCopy cloudProviderParamsCopy);
	Map<String, String> decryptOne(CloudProviderParamsCopy encryptedCloudProviderParamsCopy);
}
