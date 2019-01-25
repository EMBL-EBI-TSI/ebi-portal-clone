package uk.ac.ebi.tsc.portal.clouddeployment.volume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDownloaderException;
import uk.ac.ebi.tsc.portal.clouddeployment.model.ApplicationManifest;
import uk.ac.ebi.tsc.portal.clouddeployment.utils.InputStreamLogger;
import uk.ac.ebi.tsc.portal.clouddeployment.utils.ManifestParser;
import uk.ac.ebi.tsc.portal.api.account.repo.Account;
import uk.ac.ebi.tsc.portal.api.account.repo.AccountRepository;
import uk.ac.ebi.tsc.portal.api.account.service.AccountService;
import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetup;
import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetupCloudProvider;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Component
public class VolumeSetupDownloader {

    private static final Logger logger = LoggerFactory.getLogger(VolumeSetupDownloader.class);

    private static final String GIT_COMMAND = "git";
    private static final String RM_COMMAND = "rm";

    private final AccountService accountService;

    @Autowired
    public VolumeSetupDownloader(AccountService accountService) {
        this.accountService = accountService;
    }

    public VolumeSetup downloadVolumeSetup(String applicationsRoot, String repoUri, String username) throws IOException, ApplicationDownloaderException {

        logger.debug("Downloading volume setup from: " + repoUri);

        Account theAccount = this.accountService.findByUsername(username);

        if (theAccount != null) {
            String[] uriParts = repoUri.split("/");
            String repoName = uriParts[uriParts.length-1];

            String path =  applicationsRoot + File.separator + username.replaceAll(" ","_") + File.separator + repoName;

            logger.debug("Downloading volume setup to " + path);

            ProcessBuilder processBuilder = new ProcessBuilder(GIT_COMMAND, "clone", repoUri, path);
            Process p = processBuilder.start();


            try {
                p.waitFor();
            } catch (InterruptedException e) {
                logger.error("There is an error downloading volume setup from " + repoUri);
                String errorOutput = InputStreamLogger.logInputStream(p.getErrorStream());
                logger.error(errorOutput);

                throw new ApplicationDownloaderException(errorOutput);
            }

            if ( p.exitValue() == 0 ) { // OK
                logger.info("Successfully downloaded volume setup to " + path);
                logger.info(InputStreamLogger.logInputStream(p.getInputStream()));

                String manifestPath = path + File.separator + "manifest.json";
                ApplicationManifest applicationManifest = ManifestParser.parseApplicationManifest(manifestPath);

                logger.debug("Parsed manifest for application " + applicationManifest.applicationName);

                VolumeSetup volumeSetup = new VolumeSetup(
                        repoUri, path, applicationManifest.applicationName, this.accountService.findByUsername(username)
                );
                if (applicationManifest.cloudProviders != null) {
                    volumeSetup.getCloudProviders().addAll(
                        applicationManifest.cloudProviders.stream().map(
                            provider -> new VolumeSetupCloudProvider(provider.cloudProvider.toString(), provider.path, volumeSetup)
                        ).collect(Collectors.toList())
                    );
                }
                return volumeSetup;
            } else if (p.exitValue() == 128 ){
                logger.error("There is an error [" + p.exitValue() + "] downloading volume setup from " + repoUri);
                String errorOutput = InputStreamLogger.logInputStream(p.getErrorStream());
                logger.error(errorOutput);

                logger.info("Pulling from master instead...");
                return this.updateVolumeSetup(repoUri,path,username);
            } else {
                logger.error("There is an error [" + p.exitValue() + "] downloading volume setup from " + repoUri);
                String errorOutput = InputStreamLogger.logInputStream(p.getErrorStream());
                logger.error(errorOutput);

                throw new ApplicationDownloaderException(errorOutput);
            }
        } else {
            throw new ApplicationDownloaderException("Cannot find account for user " + username);
        }

    }

    public VolumeSetup updateVolumeSetup(String repoUri, String path, String username) throws ApplicationDownloaderException, IOException {
        logger.info("Updating volume setup from: " + repoUri);
        Account theAccount = this.accountService.findByUsername(username);

        if (theAccount != null) {

            ProcessBuilder processBuilder = new ProcessBuilder(GIT_COMMAND, "pull", "origin", "master");
            processBuilder.directory(new File(path));
            Process p = processBuilder.start();

            try {
                p.waitFor();
            } catch (InterruptedException e) {
                logger.error("There is an error updating volume from " + repoUri);
                String errorOutput = InputStreamLogger.logInputStream(p.getErrorStream());
                logger.error(errorOutput);

                throw new ApplicationDownloaderException(errorOutput);
            }

            if (p.exitValue() == 0) { // OK
                logger.info("Successfully updated setup to " + path);
                logger.info(InputStreamLogger.logInputStream(p.getInputStream()));

                String manifestPath = path + File.separator + "manifest.json";
                ApplicationManifest applicationManifest = ManifestParser.parseApplicationManifest(manifestPath);

                logger.debug("Parsed manifest for application " + applicationManifest.applicationName);

                VolumeSetup volume = new VolumeSetup(
                        repoUri, path, applicationManifest.applicationName,this.accountService.findByUsername(username)
                        );
                if (applicationManifest.cloudProviders != null) {
                    volume.getCloudProviders().addAll(
                            applicationManifest.cloudProviders.stream().map(
                                    provider -> new VolumeSetupCloudProvider(provider.cloudProvider.toString(), provider.path, volume)
                            ).collect(Collectors.toList())
                    );
                }

                return volume;
            } else if (p.exitValue() == 128) {
                logger.error("There is an error [" + p.exitValue() + "] updating volume setup from " + repoUri);
                String errorOutput = InputStreamLogger.logInputStream(p.getErrorStream());
                logger.error(errorOutput);

                throw new ApplicationDownloaderException("Repository already exists, but we shouldn't get this error...");
            } else {
                logger.error("There is an error [" + p.exitValue() + "] updating volume setup from " + repoUri);
                String errorOutput = InputStreamLogger.logInputStream(p.getErrorStream());
                logger.error(errorOutput);

                throw new ApplicationDownloaderException(errorOutput);
            }
        } else {
            throw new ApplicationDownloaderException("Cannot find account for user " + username);
        }

    }


    public int removeVolumeSetup(VolumeSetup volumeSetup) throws IOException, ApplicationDownloaderException {

        String path = volumeSetup.getRepoPath();

        logger.debug("Removing volume setup from " + path);

        ProcessBuilder processBuilder = new ProcessBuilder(RM_COMMAND, "-r", path);
        Process p = processBuilder.start();

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            logger.error("There is an error removing volume setup from " + volumeSetup.getRepoUri());
            String errorOutput = InputStreamLogger.logInputStream(p.getErrorStream());
            logger.error(errorOutput);

            throw new ApplicationDownloaderException(errorOutput);
        }

        if (p.exitValue() != 0) {
            logger.error("There is an error removing volume setup from " + volumeSetup.getRepoUri());
            String errorOutput = InputStreamLogger.logInputStream(p.getErrorStream());
            logger.error(errorOutput);

            throw new ApplicationDownloaderException(errorOutput);
        }  else {
            logger.info("Successfully removed volume setup from " + path);
            logger.info( InputStreamLogger.logInputStream(p.getInputStream()) );
        }

        return p.exitValue();
    }


}
