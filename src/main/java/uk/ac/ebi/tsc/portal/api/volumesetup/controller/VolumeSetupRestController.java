package uk.ac.ebi.tsc.portal.api.volumesetup.controller;

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
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.tsc.portal.clouddeployment.exceptions.ApplicationDownloaderException;
import uk.ac.ebi.tsc.portal.clouddeployment.volume.VolumeSetupDownloader;
import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetup;
import uk.ac.ebi.tsc.portal.api.volumesetup.repo.VolumeSetupRepository;
import uk.ac.ebi.tsc.portal.api.volumesetup.service.VolumeSetupNotFoundException;
import uk.ac.ebi.tsc.portal.api.volumesetup.service.VolumeSetupService;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@RestController
@RequestMapping(value = "/volumesetup", produces = {MediaType.APPLICATION_JSON_VALUE})
public class VolumeSetupRestController {

    private static final Logger logger = LoggerFactory.getLogger(VolumeSetupRestController.class);

    @Value("${be.volume.setup.root}")
    private String applicationsRoot;

    private final VolumeSetupService volumeSetupService;

    private VolumeSetupDownloader volumeSetupDownloader;

    @Autowired
    VolumeSetupRestController(VolumeSetupService volumeSetupService, VolumeSetupDownloader volumeSetupDownloader) {
        this.volumeSetupService = volumeSetupService;
        this.volumeSetupDownloader = volumeSetupDownloader;
    }

    
    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(Principal principal, @RequestBody VolumeSetupResource input) throws IOException, ApplicationDownloaderException {

        logger.info("Adding new volume setup for URI " + input.getRepoUri());

        try {
            this.volumeSetupService.findByAccountUsernameAndName(principal.getName(), input.getName());
            throw new ApplicationDownloaderException("Volume setup from " + input.getRepoUri() + " already exists");
        } catch (VolumeSetupNotFoundException vsne) {

            VolumeSetup volumeSetup = this.volumeSetupDownloader.downloadVolumeSetup(
                    this.applicationsRoot,
                    input.getRepoUri(),
                    principal.getName()
            );

            logger.info("URI : " + volumeSetup.getRepoUri());

            VolumeSetup newVolumeSetup = this.volumeSetupService.save(
                    volumeSetup
            );

            // Prepare response
            HttpHeaders httpHeaders = new HttpHeaders();

            Link forOneApplication = new VolumeSetupResource(newVolumeSetup).getLink("self");
            httpHeaders.setLocation(URI.create(forOneApplication.getHref()));

            VolumeSetupResource volumeSetupResource = new VolumeSetupResource(newVolumeSetup);

            return new ResponseEntity<>(volumeSetupResource, httpHeaders, HttpStatus.CREATED);
        }


    }

    @RequestMapping(method = RequestMethod.GET)
    public Resources<VolumeSetupResource> getAllVolumeSetups(Principal principal) {
        logger.info("Volume setup list requested");

        List<VolumeSetupResource> applicationResourceList =
                this.volumeSetupService.findByAccountUsername(principal.getName())
                        .stream()
                        .map(VolumeSetupResource::new)
                        .collect(Collectors.toList());

        return new Resources<>(applicationResourceList);

    }

    @RequestMapping(value = "/{setupName}", method = RequestMethod.GET)
    public VolumeSetupResource getVolumeSetupByAccountUsernameAndName(Principal principal, @PathVariable("setupName") String setupName) {
        logger.info("Volume setup " + setupName + " requested");

        return new VolumeSetupResource(this.volumeSetupService.findByAccountUsernameAndName(principal.getName(),setupName));
    }

    @RequestMapping(value = "/{setupName}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteVolumeSetupByAccountUsernameAndName(Principal principal, @PathVariable("setupName") String setupName) throws IOException, ApplicationDownloaderException {
        logger.info("Volume setup " + setupName + " deletion requested");

        // delete from DB
        VolumeSetup volumeSetup = this.volumeSetupService.findByAccountUsernameAndName(principal.getName(), setupName);
        this.volumeSetupService.delete(volumeSetup.getId());

        // delete git repo
        this.volumeSetupDownloader.removeVolumeSetup(volumeSetup);
        // Prepare response
        HttpHeaders httpHeaders = new HttpHeaders();

        return new ResponseEntity<>(null, httpHeaders, HttpStatus.OK);

    }



    /* useful to inject values without involving spring - i.e. tests */
    void setProperties(Properties properties) {
        this.applicationsRoot = properties.getProperty("be.applications.root");
    }

}
