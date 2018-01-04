package uk.ac.ebi.tsc.portal.api.cloudprovidermetadata.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.network.Network;
import uk.ac.ebi.tsc.portal.api.cloudprovidermetadata.service.CloudProviderMetadataService;

import java.util.List;

/**
 * @author Sijin J. He <sijin@ebi.ac.uk>
 * @since v0.0.1
 */
@RestController
@RequestMapping(value = "/cloudprovidermetadata", produces = {MediaType.APPLICATION_JSON_VALUE})
public class CloudProviderMetadataRestController {

    private static final Logger logger = LoggerFactory.getLogger(CloudProviderMetadataRestController.class);

    private final CloudProviderMetadataService cloudProviderMetadataService;

    CloudProviderMetadataRestController() {
        this.cloudProviderMetadataService = new CloudProviderMetadataService();
    }

    @RequestMapping(value = "/flavors",method = {RequestMethod.POST})
    public List<? extends Flavor> getFlavors(@RequestBody CloudProviderMetadataResource input) {

        logger.info("Cloud provider metadata (flavors) requested");

        return cloudProviderMetadataService.getFlavors(input);
    }

    @RequestMapping(value = "/networks",method = {RequestMethod.POST})
    public List<? extends Network> getNetworks(@RequestBody CloudProviderMetadataResource input) {

        logger.info("Cloud provider metadata (networks) requested");

        return cloudProviderMetadataService.getNetworks(input);
    }

    @RequestMapping(value = "/ippools",method = {RequestMethod.POST})
    public List<String> getIPPools(@RequestBody CloudProviderMetadataResource input) {

        logger.info("Cloud provider metadata (ippools) requested");

        return cloudProviderMetadataService.getIPPools(input);
    }

}
