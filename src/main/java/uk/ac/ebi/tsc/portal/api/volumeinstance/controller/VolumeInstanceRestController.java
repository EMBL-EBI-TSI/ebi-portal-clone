package uk.ac.ebi.tsc.portal.api.volumeinstance.controller;

import java.io.IOException;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Link;
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
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParametersRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParametersService;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.service.CloudProviderParamsCopyService;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstance;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceRepository;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceStatus;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceStatusRepository;
import uk.ac.ebi.tsc.portal.api.volumeinstance.service.VolumeInstanceService;
import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetup;
import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetupRepository;
import uk.ac.ebi.tsc.portal.api.volumesetup.service.VolumeSetupService;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDeployerException;
import uk.ac.ebi.tsc.portal.clouddeployment.volume.VolumeDeployerBash;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@RestController
@RequestMapping(value = "/volumeinstance", produces = {MediaType.APPLICATION_JSON_VALUE})
public class VolumeInstanceRestController {

    private static final Logger logger = LoggerFactory.getLogger(VolumeInstanceRestController.class);
    private static final Pattern IS_IP_ADDRESS = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}");

    @Value("${be.applications.root}")
    private String applicationsRoot;

    @Value("${be.deployments.root}")
    private String deploymentsRoot; 

    private final VolumeInstanceService volumeInstanceService;

    private final AccountService accountService;

    private final VolumeSetupService volumeSetupService;

    private final CloudProviderParametersService cloudProviderParametersService;

    private final VolumeDeployerBash volumeDeployerBash;
    
    private final CloudProviderParamsCopyService cloudProviderParametersCopyService;

    @Autowired
    VolumeInstanceRestController(VolumeInstanceRepository volumeInstanceRepository,
                                 VolumeInstanceStatusRepository volumeInstanceStatusRepository,
                                 AccountRepository accountRepository,
                                 VolumeSetupRepository volumeSetupRepository,
                                 CloudProviderParametersRepository cloudProviderParametersRepository,
                                 VolumeDeployerBash volumeDeployerBash,
                                 DomainService domainService,
                                 CloudProviderParamsCopyRepository cloudProviderParametersCopyRepository,
                                 EncryptionService encryptionService) {
        this.volumeInstanceService = new VolumeInstanceService(
                volumeInstanceRepository, volumeInstanceStatusRepository);
        this.accountService = new AccountService(accountRepository);
        this.volumeSetupService = new VolumeSetupService(volumeSetupRepository);
        this.cloudProviderParametersCopyService = new CloudProviderParamsCopyService(cloudProviderParametersCopyRepository, encryptionService);
        this.cloudProviderParametersService = new CloudProviderParametersService(cloudProviderParametersRepository, domainService,
        		cloudProviderParametersCopyService, encryptionService);
        this.volumeDeployerBash = volumeDeployerBash;
    }

    /* useful to inject values without involving spring - i.e. tests */
    void setProperties(Properties properties) {
        this.applicationsRoot = properties.getProperty("be.applications.root");
        this.deploymentsRoot = properties.getProperty("be.deployments.root");

    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> addVolumeInstance(Principal principal, @RequestBody VolumeInstanceResource input)
            throws IOException, ApplicationDeployerException, NoSuchPaddingException, InvalidKeyException,
                    NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
                    InvalidAlgorithmParameterException, InvalidKeySpecException {

        logger.info("Adding new volume instance "
                + input.getSetupName() + " to provider "
                + input.getCloudProviderParameters().getName()
                + " by user " + principal.getName());

        String userId = principal.getName();

        Account account = this.accountService.findByUsername(userId);

        VolumeSetup theSetup = this.volumeSetupService.findByAccountUsernameAndName(principal.getName(), input.getSetupName());


        // Trigger application deployment
        String theReference = "TSI" + System.currentTimeMillis();


        // TODO: right now we are selecting the first cloudproviderparameters matching username and provider name
        CloudProviderParameters selectedCloudProviderParameters;

        selectedCloudProviderParameters = this.cloudProviderParametersService.findByNameAndAccountUsername(
                input.getCloudProviderParameters().getName(), account.getUsername());


        this.volumeDeployerBash.deploy(
            theSetup.getRepoPath(),
            theReference,
            deploymentsRoot,
            selectedCloudProviderParameters
        );

        // Create and persist deployment object
        VolumeInstance volumeInstance = this.volumeInstanceService.save(
            new VolumeInstance(
                theReference,
                account, theSetup, selectedCloudProviderParameters
            )
        );

        // Prepare response
        HttpHeaders httpHeaders = new HttpHeaders();

        VolumeInstanceResource volumeInstanceResource = new VolumeInstanceResource(volumeInstance);
        Link forOneDeployment = volumeInstanceResource.getLink("self");
        httpHeaders.setLocation(URI.create(forOneDeployment.getHref()));

        return new ResponseEntity<>(volumeInstanceResource, httpHeaders, HttpStatus.CREATED);


    }


    @RequestMapping(method = RequestMethod.GET)
    public Resources<VolumeInstanceResource> getAllVolumeInstancesByUserId(Principal principal) {
        String userId = principal.getName();

        logger.info("User '" + userId + "' volume instance list requested");

        this.accountService.findByUsername(userId);

        List<VolumeInstanceResource> volumeInstanceResourceList =
                this.volumeInstanceService.findByAccountUsername(userId)
                        .stream()
                        .map(VolumeInstanceResource::new)
                        .collect(Collectors.toList());

        return new Resources<>(volumeInstanceResourceList);
    }

    @RequestMapping(value = "/{volumeInstanceReference}", method = RequestMethod.GET)
    public VolumeInstanceResource getVolumeInstanceByReference(Principal principal, @PathVariable("volumeInstanceReference") String reference) {
        String userId = principal.getName();

        logger.info("Volume instance " + reference + " for user " + userId + " requested");
        this.accountService.findByUsername(userId);

        VolumeInstance theVolumeInstance = this.volumeInstanceService.findByReference(reference);

//        // Trigger application status show
//        try {
//            ApplicationDeployer applicationDeployer = new ApplicationDeployer(
//                    applicationsRoot,
//                    osUserName,
//                    osTenancyName,
//                    osAuthUrl,
//                    osPassword
//            );
//            StateFromTerraformOutput terraformState = applicationDeployer.show(theDeployment.getApplication().getName());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return new VolumeInstanceResource(theVolumeInstance);
    }

    @RequestMapping(value = "/{volumeInstanceReference}/status", method = RequestMethod.GET)
    public VolumeInstanceStatusResource getVolumeInstanceStatusByReference(Principal principal, @PathVariable("volumeInstanceReference") String reference) {

        logger.debug("Volume instance '" + reference + "' status requested by user'" + principal.getName() + "'");

        String userId = principal.getName();
        this.accountService.findByUsername(userId);

        Long volumeInstanceId = this.volumeInstanceService.findByReference(reference).getId();

        VolumeInstanceStatus deploymentStatus = this.volumeInstanceService.findStatusByVolumeIsntanceId(volumeInstanceId);

        return new VolumeInstanceStatusResource(deploymentStatus);
    }

    @RequestMapping(value = "/{volumeInstanceReference}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeVolumeInstanceByReference(@PathVariable("volumeInstanceReference") String reference) throws IOException, ApplicationDeployerException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {

        logger.info("Volume instance '" + reference + "' deletion requested");

        VolumeInstance theVolumeInstance = this.volumeInstanceService.findByReference(reference);

        CloudProviderParameters selectedCloudProviderParameters;

        selectedCloudProviderParameters = this.cloudProviderParametersService.findByNameAndAccountUsername(
                theVolumeInstance.getCloudProviderParameters().getName(), theVolumeInstance.getAccount().getUsername());

        this.volumeDeployerBash.destroy(
                theVolumeInstance.getVolumeSetup().getRepoPath(),
                theVolumeInstance.getReference(),
                deploymentsRoot,
                selectedCloudProviderParameters
        );


        this.volumeInstanceService.delete(theVolumeInstance.getId());

        // Prepare response
        HttpHeaders httpHeaders = new HttpHeaders();

        return new ResponseEntity<>(null, httpHeaders, HttpStatus.OK);
    }


}
