package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParametersField;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopy;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyField;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.configuration.repo.Configuration;
import uk.ac.ebi.tsc.portal.api.configuration.service.ConfigurationService;
import uk.ac.ebi.tsc.portal.api.deployment.repo.Deployment;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentService;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;

public class CloudProviderParamsCopyService {

	private static final Logger logger = LoggerFactory.getLogger(CloudProviderParamsCopyService.class);

	private final CloudProviderParamsCopyRepository cloudProviderParametersCopyRepository;
	
	private final EncryptionService encryptionService;

	private final String salt, password;
	
	@Autowired
	public CloudProviderParamsCopyService(CloudProviderParamsCopyRepository cloudProviderParametersCopyRepository,
			EncryptionService encryptionService,
			String salt,
			String password) {
		this.cloudProviderParametersCopyRepository = cloudProviderParametersCopyRepository;
		this.encryptionService = encryptionService;
		this.salt = salt;
		this.password = password;
	}

	public CloudProviderParamsCopy createCloudProviderParameterCopy(CloudProviderParameters cloudProvider){

		CloudProviderParamsCopy  cloudProviderParametersCopy =
				new  CloudProviderParamsCopy(cloudProvider.getName(),
						cloudProvider.getCloudProvider(), cloudProvider.getAccount());

		Collection<CloudProviderParametersField> field = cloudProvider.getFields();
		List<CloudProviderParamsCopyField> copyFields = new ArrayList<>();
		field.forEach(cpf -> {
			CloudProviderParamsCopyField dcpField = new CloudProviderParamsCopyField(
					cpf.getKey(), cpf.getValue(), cloudProviderParametersCopy);
			copyFields.add(dcpField);
		});
		cloudProviderParametersCopy.setFields(copyFields);
		cloudProviderParametersCopy.setCloudProviderParametersReference(cloudProvider.getReference());
		return cloudProviderParametersCopy;

	}

	public Collection<CloudProviderParamsCopy> findByAccountUsername(String username) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, BadPaddingException, IllegalBlockSizeException {
		Collection<CloudProviderParamsCopy> encryptedRes = this.cloudProviderParametersCopyRepository.findByAccountUsername(username);
		return decryptAll(encryptedRes);
	}

	public CloudProviderParamsCopy findById(Long id){
		CloudProviderParamsCopy encryptedRes = this.cloudProviderParametersCopyRepository.findById(id).orElseThrow(
				() -> new CloudProviderParamsCopyNotFoundException(id));
		return decryptOne(encryptedRes);
	}


	public void delete(CloudProviderParamsCopy cloudParametersCopy) {
		this.cloudProviderParametersCopyRepository.delete(cloudParametersCopy);
	}

	public CloudProviderParamsCopy save(CloudProviderParamsCopy cloudProviderParametersCopy) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException {

		Map<String, String> paramValues = new HashMap<>();
		cloudProviderParametersCopy.getFields().forEach(field -> {
			paramValues.put(field.getKey(), field.getValue());
		});

		Map<String, String> encyptedValues = encryptionService.encrypt(paramValues, salt, toHex(password));
		for (CloudProviderParamsCopyField cloudProviderParametersField : cloudProviderParametersCopy.getFields()) {
			cloudProviderParametersField.setValue(
					encyptedValues.get(cloudProviderParametersField.getKey()));
		}
		return this.cloudProviderParametersCopyRepository.save(cloudProviderParametersCopy);
	}

	public CloudProviderParamsCopy findByCloudProviderParametersReference(String reference) {
		CloudProviderParamsCopy encryptedRes = this.cloudProviderParametersCopyRepository.findByCloudProviderParametersReference(reference).
				orElseThrow(
						() -> new CloudProviderParamsCopyNotFoundException(reference)
						);
		return decryptOne(encryptedRes);

	}

	private Collection<CloudProviderParamsCopy> decryptAll(Collection<CloudProviderParamsCopy> encryptedAll) {

		Collection<CloudProviderParamsCopy> decryptedRes = new LinkedList<>();

		encryptedAll.forEach(encryptedCloudProviderParametersCopy -> {
			decryptedRes.add(decryptOne(encryptedCloudProviderParametersCopy));
		});

		return decryptedRes;
	}

	private CloudProviderParamsCopy decryptOne(CloudProviderParamsCopy encryptedCloudProviderParametersCopy) {

		Map<String, String> paramValues = new HashMap<>();
		encryptedCloudProviderParametersCopy.getFields().forEach(field -> {
			paramValues.put(field.getKey(), field.getValue());
		});

		Map<String, String> decryptedValues = encryptionService.decryptOne(paramValues, salt, toHex(password));


		CloudProviderParamsCopy decryptedCloudProviderParametersCopy =
				new CloudProviderParamsCopy(
						encryptedCloudProviderParametersCopy.getName(),
						encryptedCloudProviderParametersCopy.getCloudProvider(),
						encryptedCloudProviderParametersCopy.getAccount()
						);

		decryptedCloudProviderParametersCopy.setCloudProviderParametersReference(encryptedCloudProviderParametersCopy.getCloudProviderParametersReference());
		for (CloudProviderParamsCopyField encryptedCloudProviderParametersCopyField : encryptedCloudProviderParametersCopy.getFields()) {
			CloudProviderParamsCopyField decryptedCloudProviderParametersCopyField =
					new CloudProviderParamsCopyField(
							encryptedCloudProviderParametersCopyField.getKey(),
							decryptedValues.get(encryptedCloudProviderParametersCopyField.getKey()),
							decryptedCloudProviderParametersCopy);
			decryptedCloudProviderParametersCopyField.setId(encryptedCloudProviderParametersCopyField.getId());
			decryptedCloudProviderParametersCopy.getFields().add(decryptedCloudProviderParametersCopyField);
		}
		decryptedCloudProviderParametersCopy.setId(encryptedCloudProviderParametersCopy.getId());
		return decryptedCloudProviderParametersCopy;
	}
	
	public void checkAndDeleteCPPCopy(CloudProviderParamsCopy cppCopy, DeploymentService deploymentService, ConfigurationService configurationService) {
		logger.info("Checking if any deployments refer to the cloud credential copy");
		Deployment deploymentFound = deploymentService.findAll().stream().
				filter(deployment -> 
				deployment.getCloudProviderParametersReference().equals(cppCopy.getCloudProviderParametersReference())
						)
				.findAny().orElse(null);

		logger.info("Checking if any configurations refer to the cloud credential copy");
		Configuration configurationFound = configurationService.findAll().stream().
				filter(configuration -> 
				configuration.getCloudProviderParametersReference().equals(cppCopy.getCloudProviderParametersReference())
						)
				.findAny().orElse(null);

		if(deploymentFound == null && configurationFound == null){
			logger.info("CPP copy is not referenced by any deployment or configuration, so deleting it");
			this.cloudProviderParametersCopyRepository.delete(cppCopy);
		}
	}

	public void saveWithoutEncryption(CloudProviderParamsCopy cppCopy) {
		this.cloudProviderParametersCopyRepository.save(cppCopy);
	}
	
	private static String toHex(String arg) {
		return String.format("%040x", new BigInteger(1, arg.getBytes()));
	}
}
