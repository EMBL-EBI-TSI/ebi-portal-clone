package uk.ac.ebi.tsc.portal.clouddeployment.volume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDeployerException;
import uk.ac.ebi.tsc.portal.clouddeployment.model.StateFromTerraformOutput;
import uk.ac.ebi.tsc.portal.clouddeployment.utils.InputStreamLogger;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParameters;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParametersField;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstance;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceRepository;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceStatusEnum;
import uk.ac.ebi.tsc.portal.api.volumeinstance.repo.VolumeInstanceStatusRepository;
import uk.ac.ebi.tsc.portal.api.volumeinstance.service.VolumeInstanceService;
import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetupRepository;
import uk.ac.ebi.tsc.portal.api.volumesetup.service.VolumeSetupService;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@Component
public class VolumeDeployerBash {

    private static final Logger logger = LoggerFactory.getLogger(VolumeDeployerBash.class);

    private static final String BASH_COMMAND = "bash";
    private static final String OS_COMPUTE_INSTANCE_REGEX = "openstack_compute_instance_v2";
    private static final String OS_FLOATING_IP = "floating_ip";

    private final VolumeInstanceService volumeInstanceService;
    private final VolumeSetupService volumeSetupService;

    @Autowired
    public VolumeDeployerBash(VolumeInstanceRepository volumeInstanceRepository,
                              VolumeInstanceStatusRepository volumeInstanceStatusRepository,
                              VolumeSetupRepository volumeSetupRepository) {
        this.volumeInstanceService = new VolumeInstanceService(volumeInstanceRepository, volumeInstanceStatusRepository);
        this.volumeSetupService = new VolumeSetupService(volumeSetupRepository);
    }


    public void deploy(String repoPath,
                       String reference,
                       String deploymentsRoot,
                       CloudProviderParameters cloudProviderCredentials) throws IOException,
            ApplicationDeployerException {

        logger.info("Starting deployment of volume using bash from repo: " + repoPath);


        ProcessBuilder processBuilder = new ProcessBuilder(BASH_COMMAND, "deploy.sh");

        Map<String, String> env = processBuilder.environment();
        addGenericProviderCreds(env,cloudProviderCredentials);
        env.put("PORTAL_DEPLOYMENTS_ROOT", deploymentsRoot);
        env.put("PORTAL_DEPLOYMENT_REFERENCE", reference);
        env.put("PORTAL_APP_REPO_FOLDER", repoPath);
        env.put("TF_VAR_key_pair", "demo-key"); // TODO: change this to something less shameful...

        processBuilder.directory(new File(repoPath));

        Process p = processBuilder.start();

        Thread newThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    logger.error("There is an interruption while deploying volume from " + repoPath);
                    String errorOutput = outputError(p, reference, repoPath, deploymentsRoot);
                    // kill the process if alive?
                    p.destroy();
                    // Set the right deployment status
                    VolumeInstance theVolumeInstance = volumeInstanceService.findByReference(reference);
                    updateDeploymentStatus(theVolumeInstance.getReference(), VolumeInstanceStatusEnum.STARTING_FAILED);
                    // Throw application deployer exception
                    try {
                        throw new ApplicationDeployerException(errorOutput);
                    } catch (ApplicationDeployerException e1) {
                        e1.printStackTrace();
                    }
                }

                if (p.exitValue() != 0) {
                    logger.error("There is a non-zero exit code while deploying volume from " + repoPath);
                    String errorOutput = outputError(p, reference, repoPath, deploymentsRoot);
                    // kill the process if alive?
                    p.destroy();
                    // Set the right deployment status
                    VolumeInstance theVolumeInstance = volumeInstanceService.findByReference(reference);
                    updateDeploymentStatus(theVolumeInstance.getReference(), VolumeInstanceStatusEnum.STARTING_FAILED);
                    // Throw application deployer exception
                    try {
                        throw new ApplicationDeployerException(errorOutput);
                    } catch (ApplicationDeployerException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    logger.info("Successfully deployed volume from " + repoPath);
                    try {
                        logger.info(InputStreamLogger.logInputStream(p.getInputStream()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    VolumeInstance theVolumeInstance = volumeInstanceService.findByReference(reference);
                    updateDeploymentStatus(theVolumeInstance.getReference(), VolumeInstanceStatusEnum.RUNNING);
                    updateVolumeProviderId(theVolumeInstance.getReference(),VolumeInstanceStatusEnum.STARTING_FAILED,deploymentsRoot);
                }
            }
        });
        newThread.start();

    }

    protected String outputError(Process p, String reference, String repoPath, String deploymentsRoot) {
        logger.error("There is an error deploying volume from " + repoPath);
        String errorOutput = null;
        try {
            errorOutput = InputStreamLogger.logInputStream(p.getErrorStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.error(errorOutput);

        return errorOutput;
    }


    private void addGenericProviderCreds(Map<String, String> env, CloudProviderParameters cloudProviderCredentials) {
        logger.info("Setting generic cloudproviderparameters for " + cloudProviderCredentials.getName());

        for (CloudProviderParametersField cloudProviderParametersField : cloudProviderCredentials.getFields()) {
            logger.info("Setting " + cloudProviderParametersField.getKey());
            env.put(cloudProviderParametersField.getKey(), cloudProviderParametersField.getValue());
        }
    }

    public StateFromTerraformOutput state(String repoPath, String reference, String deploymentsRoot) throws IOException, ApplicationDeployerException {

        logger.info("Showing state of volume with reference: " + reference);

        ProcessBuilder processBuilder = new ProcessBuilder(BASH_COMMAND, "state.sh");

        Map<String, String> env = processBuilder.environment();
        env.put("PORTAL_DEPLOYMENTS_ROOT", deploymentsRoot);
        env.put("PORTAL_DEPLOYMENT_REFERENCE", reference);

        processBuilder.directory(new File(repoPath));

        Process p = processBuilder.start();

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            logger.error("There is an error showing volume " + reference);
            String errorOutput = InputStreamLogger.logInputStream(p.getErrorStream());
            logger.error(errorOutput);
            throw new ApplicationDeployerException(errorOutput);
        }

        if (p.exitValue() != 0) {
            logger.error("There is an error showing volume " + reference);
            String errorOutput = InputStreamLogger.logInputStream(p.getErrorStream());
            logger.error(errorOutput);
            throw new ApplicationDeployerException(errorOutput);
        } else {
            String output = InputStreamLogger.logInputStream(p.getInputStream());
            logger.info(output);
            return terraformStateFromString(output);
        }

    }



    public int destroy(String repoPath, String reference, String deploymentsRoot,
                       CloudProviderParameters cloudProviderCredentials) throws IOException, ApplicationDeployerException {

        logger.debug("Destroying instance of volume: " + reference);

        String path =  deploymentsRoot + File.separator + reference;

        ProcessBuilder processBuilder = new ProcessBuilder(BASH_COMMAND, "destroy.sh");

        Map<String, String> env = processBuilder.environment();
        addGenericProviderCreds(env, cloudProviderCredentials);
        env.put("PORTAL_DEPLOYMENTS_ROOT", deploymentsRoot);
        env.put("PORTAL_DEPLOYMENT_REFERENCE", reference);
        env.put("PORTAL_APP_REPO_FOLDER", repoPath);

        processBuilder.directory(new File(repoPath));

        logger.debug("Destroying instance of volume at: " + path);

        Process p = processBuilder.start();

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            logger.error("There is an error destroying volume " + reference);
            String errorOutput = InputStreamLogger.logInputStream(p.getErrorStream());
            logger.error(errorOutput);
            throw new ApplicationDeployerException(errorOutput);
        }

        if (p.exitValue() != 0) {
            logger.error("There is an error destroying volume " + reference);
            String errorOutput = InputStreamLogger.logInputStream(p.getErrorStream());
            logger.error(errorOutput);
            throw new ApplicationDeployerException(errorOutput);
        } else {
            logger.info("Successfully destroyed application deployment " + reference);
            logger.info(InputStreamLogger.logInputStream(p.getInputStream()));
        }

        return p.exitValue();
    }

    private StateFromTerraformOutput terraformStateFromString(String output) throws IOException {

        logger.debug("The whole terraform state for the volume instance is: " + output);
        
        StateFromTerraformOutput stateFromTerraformOutput = new StateFromTerraformOutput();

        String[] lines = output.split(System.getProperty("line.separator"));

        // Read Id, it should be the first ID (TODO improve this later)
        stateFromTerraformOutput.setId(lines[1].replaceAll(" ","").split("=")[1]);

        return stateFromTerraformOutput;
    }

    private void updateVolumeProviderId(String deploymentReference, VolumeInstanceStatusEnum failStatus, String deploymentsRoot) {
        VolumeInstance thVolumeInstance = this.volumeInstanceService.findByReference(deploymentReference);

        // get deployment state
        StateFromTerraformOutput stateFromTerraformOutput = null;
        try {
            stateFromTerraformOutput = this.state(
                    thVolumeInstance.getVolumeSetup().getRepoPath(),
                    thVolumeInstance.getReference(),
                    deploymentsRoot
            );


            // update the volume
            thVolumeInstance.setProviderId(stateFromTerraformOutput.getId()!=null ? stateFromTerraformOutput.getId():"");

        } catch (IOException | ApplicationDeployerException e) {
            e.printStackTrace();
            // update status
            thVolumeInstance.getVolumeInstanceStatus().setStatus(failStatus);
            // persist changes
            this.volumeInstanceService.save(thVolumeInstance);
        }
        // persist changes
        this.volumeInstanceService.save(thVolumeInstance);
    }

    private void updateDeploymentStatus(String deploymentReference, VolumeInstanceStatusEnum status) {
        VolumeInstance thVolumeInstance = this.volumeInstanceService.findByReference(deploymentReference);
        // update status
        thVolumeInstance.getVolumeInstanceStatus().setStatus(status);
        // persist changes
        this.volumeInstanceService.save(thVolumeInstance);
    }


}