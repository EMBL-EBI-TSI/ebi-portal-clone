package uk.ac.ebi.tsc.portal.clouddeployment.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.ebi.tsc.aap.client.repo.DomainService;
import uk.ac.ebi.tsc.portal.api.application.repo.Application;
import uk.ac.ebi.tsc.portal.api.application.repo.ApplicationRepository;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopy;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigDeploymentParamsCopyRepository;
import uk.ac.ebi.tsc.portal.api.configuration.repo.Configuration;
import uk.ac.ebi.tsc.portal.api.deployment.repo.*;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentSecretService;
import uk.ac.ebi.tsc.portal.api.deployment.service.DeploymentService;
import uk.ac.ebi.tsc.portal.api.encryptdecrypt.security.EncryptionService;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDeployerException;
import uk.ac.ebi.tsc.portal.clouddeployment.model.StateFromTerraformOutput;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collection;
import java.util.Map;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@Component
public class ApplicationDeployerDocker extends AbstractApplicationDeployer {

    @Value("${be.deployments.root}")
    private String deploymentsRoot;

    @Value("${elasticsearch.url}")
    private String elasticSearchUrl;

    @Value("${elasticsearch.index}")
    private String elasticSearchIndex;

    @Value("${elasticsearch.username}")
    private String elasticSearchUsername;

    @Value("${elasticsearch.password}")
    private String elasticSearchPassword;

    @Autowired
    public ApplicationDeployerDocker(DeploymentService deploymentService,
                                     ApplicationRepository applicationRepository,
                                     DomainService domainService,
                                     CloudProviderParamsCopyRepository cloudProviderParametersRepository,
                                     ConfigDeploymentParamsCopyRepository configDeploymentParamsCopyRepository,
                                     EncryptionService encryptionService,
                                     DeploymentSecretService secretService) {
        super(  deploymentService,
                applicationRepository,
                domainService,
                cloudProviderParametersRepository,
                configDeploymentParamsCopyRepository,
                encryptionService,
                secretService
        );

    }

    public void deploy(String userEmail,
                       Application theApplication,
                       String reference,
                       String cloudProviderPath,
                       Map<String, String> inputAssignments,
                       Map<String, String> parameterAssignments,
                       Map<String, String> volumeAttachments,
                       Map<String, String> configurationParameters,
                       CloudProviderParamsCopy cloudProviderParametersCopy,
                       Configuration configuration,
                       java.sql.Timestamp startTime,
                       String userSshKey,
                       String baseUrl)  throws IOException,
            ApplicationDeployerException, NoSuchAlgorithmException, NoSuchProviderException {

        throw new NotImplementedException();
    }

    public StateFromTerraformOutput state(String repoPath,
                                          String reference,
                                          String cloudProviderPath,
                                          Map<String, String> outputs,
                                          Configuration configuration,
                                          String userSshKey) {

        throw new NotImplementedException();
    }

    public void destroy(String repoPath, String reference,
                        String cloudProviderPath,
                        Collection<DeploymentAssignedInput> inputAssignments,
                        Collection<DeploymentAssignedParameter> parameterAssignments,
                        Collection<DeploymentAttachedVolume> volumeAttachments,
                        DeploymentConfiguration deploymentConfiguration,
                        CloudProviderParamsCopy cloudProviderParametersCopy) {
        throw new NotImplementedException();
    }

}
