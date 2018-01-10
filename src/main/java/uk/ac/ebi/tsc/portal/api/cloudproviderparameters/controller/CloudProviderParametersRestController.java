package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;
import uk.ac.ebi.tsc.portal.api.account.service.AccountService;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParametersField;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParametersRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopy;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParametersNotFoundException;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParametersService;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParamsCopyNotFoundException;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParamsCopyService;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParametersRepository;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationRepository;
import uk.ac.ebi.tsc.portal.api.configuration.service.ConfigurationDeploymentParametersService;
import uk.ac.ebi.tsc.portal.api.configuration.service.ConfigurationService;
import uk.ac.ebi.tsc.portal.api.deployment.controller.DeploymentRestController;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentStatusRepository;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentService;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;
import uk.ac.ebi.tsc.portal.clouddeployment.utils.NamesPatternMatcher;
import uk.ac.ebi.tsc.portal.security.TokenHandler;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@RestController
@RequestMapping(value = "/cloudproviderparameters", produces = {MediaType.APPLICATION_JSON_VALUE})
public class CloudProviderParametersRestController {

	private static final Logger logger = LoggerFactory.getLogger(CloudProviderParametersRestController.class);

	private final CloudProviderParametersService cloudProviderParametersService;

	private final AccountService accountService;

	private final DeploymentService deploymentService;

	private TokenHandler tokenHandler;

	private final DeploymentRestController deploymentRestController;

	private final ConfigurationService configurationService;

	private final ConfigurationDeploymentParametersService cdpService;

	private final CloudProviderParamsCopyService cloudProviderParametersCopyService;

	@Autowired
	CloudProviderParametersRestController(
			CloudProviderParametersRepository cloudProviderParametersRepository,
			AccountRepository accountRepository,
			DomainService domainService,
			TokenHandler tokenHandler,
			DeploymentRepository deploymentRepository,
			DeploymentStatusRepository deploymentStatusRepository,
			DeploymentRestController deploymentRestController,
			ConfigurationRepository configurationRepository,
			ConfigurationDeploymentParametersRepository cdpRepository,
			CloudProviderParamsCopyRepository cloudProviderParametersCopyRepository,
			EncryptionService encryptionService,
			@Value("${ecp.security.salt}") final String salt, 
			@Value("${ecp.security.password}") final String password
			) {
		this.cloudProviderParametersCopyService = new CloudProviderParamsCopyService(cloudProviderParametersCopyRepository,
				encryptionService, salt, password);
		this.cloudProviderParametersService = new CloudProviderParametersService(cloudProviderParametersRepository, domainService,
				cloudProviderParametersCopyService, encryptionService, salt, password);
		this.accountService = new AccountService(accountRepository);
		deploymentService = new DeploymentService(deploymentRepository, deploymentStatusRepository);
		this.cdpService = new ConfigurationDeploymentParametersService(cdpRepository, domainService);
		this.tokenHandler = tokenHandler;
		this.deploymentRestController = deploymentRestController;
		this.configurationService = new ConfigurationService(
				configurationRepository, domainService, cloudProviderParametersService,
				cdpService, cloudProviderParametersCopyService, deploymentService);
	}


	@RequestMapping(method = {RequestMethod.GET})
	public Resources<CloudProviderParametersResource> getCurrentUserCredentials(Principal principal)
			throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IOException,
			BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException,
			InvalidKeySpecException {

		logger.info("Account " + principal.getName() + " cloud provider parameters requested");

		Account account = this.accountService.findByUsername(principal.getName());

		List<CloudProviderParametersResource> cloudProviderParametersResourceList;
		logger.info("--- Retrieving cloud provider parameters by username " + account.getUsername() + " for principal " + principal.getName());
		cloudProviderParametersResourceList =
				this.cloudProviderParametersService.findByAccountUsername(account.getUsername())
				.stream()
				.map(CloudProviderParametersResource::new)
				.collect(Collectors.toList());

		return new Resources<>(cloudProviderParametersResourceList);

	}

	@RequestMapping(value = "/{name:.+}", method = {RequestMethod.GET})
	public CloudProviderParametersResource getByName(Principal principal, @PathVariable("name") String name)
			throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IOException,
			BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException,
			InvalidKeySpecException {

		logger.info("Account " + principal.getName() + " requested cloud provider parameters " + name);

		Account account = this.accountService.findByUsername(principal.getName());

		CloudProviderParametersResource cloudProviderParametersResource;

		cloudProviderParametersResource =
				new CloudProviderParametersResource(
						this.cloudProviderParametersService.findByNameAndAccountUsername(name, account.getUsername())
						);

		return cloudProviderParametersResource;
	}

	@RequestMapping(value = "/shared/{name:.+}", method = {RequestMethod.GET})
	public CloudProviderParametersResource getSharedByName(HttpServletRequest request, Principal principal, @PathVariable("name") String name)
			throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IOException,
			BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException,
			InvalidKeySpecException {

		logger.info("Account " + principal.getName() + " requested SHARED cloud provider parameters " + name);

		Account account = this.accountService.findByUsername(principal.getName());
		String token = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];

		CloudProviderParameters providers = this.cloudProviderParametersService.getSharedCppsByCppName(
				account, token, tokenHandler.parseUserFromToken(token), name);

		if (providers!=null) {
			logger.info("--- Retrieving SHARED cloud provider parameters " + name + " by username " + providers.getAccount().getUsername() + " for principal " + principal.getName());
			return new CloudProviderParametersResource(
					providers
					);
		} else {
			throw new CloudProviderParametersNotFoundException(principal.getName(),name);
		}

	}


	@RequestMapping(method = {RequestMethod.POST})
	public ResponseEntity<?> add(Principal principal, @RequestBody CloudProviderParametersResource input) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

		logger.info("Account " + principal.getName() + " cloud provider parameters " + input.getName() + " addition requested");

		// Check user exists
		Account account = this.accountService.findByUsername(principal.getName());
		// Check credential name isn't empty
		if (input.getName() == null || input.getName().length() < 1) {
			throw new InvalidCloudProviderParametersInputException(account.getUsername(), input.getName());
		}

		if(!NamesPatternMatcher.nameMatchesPattern(input.getName())){
			logger.error("Input credential name does not match required name pattern");
			throw new InvalidCloudProviderParametersInputException(input.getName());
		}


		// Create model entity
		CloudProviderParameters cloudProviderParameters = new CloudProviderParameters(input.getName(), input.getCloudProvider(), account);
		// Add fields
		cloudProviderParameters.getFields().addAll(input.getFields().stream().map(
				f -> new CloudProviderParametersField(f.getKey(), f.getValue(), cloudProviderParameters)
				).collect(Collectors.toList()));

		//set unique reference
		UUID reference = UUID.randomUUID();
		cloudProviderParameters.setReference(reference.toString());

		// Persist
		CloudProviderParameters savedCloudProviderParameters = this.cloudProviderParametersService.save(cloudProviderParameters);


		//create a copy of the cpp
		CloudProviderParamsCopy cppCopy = this.cloudProviderParametersCopyService.createCloudProviderParameterCopy(cloudProviderParameters);
		this.cloudProviderParametersCopyService.saveWithoutEncryption(cppCopy);

		// Prepare response
		HttpHeaders httpHeaders = new HttpHeaders();

		return new ResponseEntity<>(new CloudProviderParametersResource(cloudProviderParameters), httpHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/{name:.+}", method = {RequestMethod.PUT})
	public ResponseEntity<?> update(Principal principal, @RequestBody CloudProviderParametersResource input, @PathVariable("name")  String name) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

		logger.info("Account " + principal.getName() + " cloud provider parameters " + input.getName() + " update requested");

		// Check user exists
		Account account = this.accountService.findByUsername(principal.getName());
		// Check credential name isn't empty
		if (input.getName() == null || input.getName().length() < 1) {
			throw new InvalidCloudProviderParametersInputException(account.getUsername(), input.getName());
		}

		CloudProviderParameters cloudProviderParameters = this.cloudProviderParametersService.findByNameAndAccountUsername(name, principal.getName());
		CloudProviderParamsCopy cloudProviderParametersCopy =
				this.cloudProviderParametersCopyService.findByCloudProviderParametersReference(cloudProviderParameters.getReference());
		//update cloud provider
		if(!cloudProviderParameters.getCloudProvider().equals(input.getCloudProvider())){
			logger.info("Updating cloud provider from " + cloudProviderParameters.getCloudProvider() + " to " + input.getCloudProvider());
			cloudProviderParameters.setCloudProvider(input.getCloudProvider());

			//update the copy also
			logger.info("Updating cloud provider copy from " + cloudProviderParameters.getCloudProvider() + " to " + input.getCloudProvider());
			cloudProviderParametersCopy.setCloudProvider(input.getCloudProvider());
		}

		// Update modified fields
		cloudProviderParameters = this.cloudProviderParametersService.updateFields(cloudProviderParameters, cloudProviderParametersCopy, input, principal.getName());

		// Prepare response
		HttpHeaders httpHeaders = new HttpHeaders();

		return new ResponseEntity<>(new CloudProviderParametersResource(cloudProviderParameters), httpHeaders, HttpStatus.OK);
	}


	@RequestMapping(value = "/{name:.+}", method = {RequestMethod.DELETE})
	public ResponseEntity<?> delete(Principal principal, @PathVariable("name") String name) throws Exception{

		logger.info("Account " + principal.getName() + " cloud provider parameters " + name + " deletion requested");

		try{
			CloudProviderParameters cloudParameters = this.cloudProviderParametersService.findByNameAndAccountUsername(name, principal.getName());
			this.cloudProviderParametersService.stopDeploymentsOnDelete(principal, name, deploymentRestController, deploymentService, configurationService, cloudParameters);
			CloudProviderParamsCopy cppCopy = this.cloudProviderParametersCopyService.findByCloudProviderParametersReference(cloudParameters.getReference());
			this.cloudProviderParametersService.delete(cloudParameters);
			this.cloudProviderParametersCopyService.checkAndDeleteCPPCopy(cppCopy, this.deploymentService, this.configurationService);
		}catch(CloudProviderParametersNotFoundException e){
			logger.error("The cloud credential to delete is not found for the user");
			throw e;
		}catch(CloudProviderParamsCopyNotFoundException e){
			logger.error("The cloud credential copy to delete is not found for the user");
			throw e;
		}

		// Prepare response
		HttpHeaders httpHeaders = new HttpHeaders();

		return new ResponseEntity<>(null, httpHeaders, HttpStatus.OK);
	}


	@RequestMapping(value = "/shared", method = {RequestMethod.GET})
	public Resources<CloudProviderParametersResource> getSharedCredentialsByAccount(HttpServletRequest request, Principal principal) {

		logger.info("List of shared credentials of account requested " + principal.getName() + "  requested");
		Account account = this.accountService.findByUsername(principal.getName());
		String token = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];
		List<CloudProviderParametersResource> cloudProviderParametersResourceList = 
				cloudProviderParametersService.getSharedCppsByAccount(account, token, tokenHandler.parseUserFromToken(token))
				.stream()
				.map(CloudProviderParametersResource::new)
				.collect(Collectors.toList());

		return new Resources<>(cloudProviderParametersResourceList);

	}

	@RequestMapping(value = "/ownedandshared", method = {RequestMethod.GET})
	public Resources<CloudProviderParametersResource> getOwnedAndSharedCredentials(HttpServletRequest request, Principal principal) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException, IOException{

		logger.info("List of own and shared credentials of account requested " + principal.getName() + "  requested");

		Account account = this.accountService.findByUsername(principal.getName());

		List<CloudProviderParametersResource> cloudProviderParametersResourceList;
		String token = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1];

		logger.info("--- Retrieving owned cloud provider parameters by username " + account.getUsername() + " for principal " + principal.getName());
		cloudProviderParametersResourceList = 
				this.cloudProviderParametersService.findByAccountUsername(account.getUsername())
				.stream()
				.map(CloudProviderParametersResource::new)
				.collect(Collectors.toList());
		logger.info("Retrieved owned credentials " + cloudProviderParametersResourceList.size());
		logger.info("--- Retrieving shared cloud provider parameters by username " + account.getUsername() + " for principal " + principal.getName());
		cloudProviderParametersResourceList.addAll(
				cloudProviderParametersService.getSharedCppsByAccount(account, token, tokenHandler.parseUserFromToken(token))
				.stream()
				.map(CloudProviderParametersResource::new)
				.collect(Collectors.toList()));
		logger.info("Retrieved owned and shared credentials " + cloudProviderParametersResourceList.size());
		return new Resources<>(cloudProviderParametersResourceList);
	}

}
