package uk.ac.ebi.tsc.portal.api.cloudprovidermetadata.service;

import org.openstack4j.api.OSClient.OSClientV2;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.client.IOSClientBuilder;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.network.Network;
import org.openstack4j.openstack.OSFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.tsc.portal.api.cloudprovidermetadata.controller.CloudProviderMetadataResource;

import java.util.List;


/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class CloudProviderMetadataService {

	private static final Logger logger = LoggerFactory.getLogger(CloudProviderMetadataService.class);


	public List<? extends Flavor> getFlavors(CloudProviderMetadataResource input) {

		logger.info("Getting flavor information for ");
		logger.info("- tenantName: " + input.getTenantName());
		logger.info("- domainName: " + input.getDomainName());
		logger.info("- endpoint: " + input.getEndpoint());
		logger.info("- version: " + input.getVersion());

		List<? extends Flavor> list = null;

		switch (input.getVersion()) {
			case "2":
				list = getFlavorsV2(input);
				break;
			case "3":
				list = getFlavorsV3(input);
				break;
		}

		logger.info("Result: ");
		list.stream().forEach(
				f -> {
					logger.info("- Flavor ID: " + f.getId());
					logger.info("- Flavor name: " + f.getName());
				}
		);

		return list;
	}

	public List<? extends Network> getNetworks(CloudProviderMetadataResource input) {

		logger.info("Getting network information for ");
		logger.info("- tenantName: " + input.getTenantName());
		logger.info("- domainName: " + input.getDomainName());
		logger.info("- endpoint: " + input.getEndpoint());
		logger.info("- version: " + input.getVersion());

		List<? extends Network> list = null;

		switch (input.getVersion()) {
			case "2":
				list = getNetworksV2(input);
				break;
			case "3":
				list = getNetworksV3(input);
				break;
		}

		logger.info("Result: ");
		list.stream().forEach(
				n -> {
					logger.info("- Network ID: " + n.getId());
					logger.info("- Network name: " + n.getName());
				}
		);

		return list;
	}

	public List<String> getIPPools(CloudProviderMetadataResource input) {

		logger.info("Getting IP pools information ");
		logger.info("- tenantName: " + input.getTenantName());
		logger.info("- domainName: " + input.getDomainName());
		logger.info("- endpoint: " + input.getEndpoint());
		logger.info("- version: " + input.getVersion());

		List<String> list = null;

		switch (input.getVersion()) {
			case "2":
				list = getIPPoolsV2(input);
				break;
			case "3":
				list = getIPPoolsV3(input);
				break;
		}

        logger.info("Result: ");
        list.stream().forEach(
                ipp -> {
                    logger.info("- IP Pool: " + ipp);
                }
        );

		return list;
	}

	private OSClientV2 authenticateV2(CloudProviderMetadataResource input) {
		return OSFactory.builderV2()
				.endpoint(input.getEndpoint())
				.credentials(input.getUsername(), input.getPassword())
				.tenantName(input.getTenantName())
				.authenticate();
	}

	private OSClientV3 authenticateV3(CloudProviderMetadataResource input) {
		Identifier domainIdentifier = Identifier.byName(input.getDomainName());
		IOSClientBuilder.V3 endpoint = OSFactory.builderV3().endpoint(input.getEndpoint());
		if(input.getTenantName()!=null && !input.getTenantName().isEmpty())
			endpoint.scopeToProject(Identifier.byName(input.getTenantName()), domainIdentifier);
		return endpoint.credentials(input.getUsername(), input.getPassword(), domainIdentifier).authenticate();
	}

	private List<String> getIPPoolsV2(CloudProviderMetadataResource input) {
		OSClientV2 os = authenticateV2(input);
		return os.compute().floatingIps().getPoolNames();
	}

	private List<String> getIPPoolsV3(CloudProviderMetadataResource input) {
		OSClientV3 os = authenticateV3(input);
		return os.compute().floatingIps().getPoolNames();
	}


	private List<? extends Network> getNetworksV2(CloudProviderMetadataResource input) {
		OSClientV2 os = authenticateV2(input);
		return os.networking().network().list();
	}

	private List<? extends Network> getNetworksV3(CloudProviderMetadataResource input) {
		OSClientV3 os = authenticateV3(input);
		return os.networking().network().list();
	}

	private List<? extends Flavor> getFlavorsV2(CloudProviderMetadataResource input) {
		OSClientV2 os = authenticateV2(input);
		return os.compute().flavors().list();
	}

	private List<? extends Flavor> getFlavorsV3(CloudProviderMetadataResource input) {
		OSClientV3 os = authenticateV3(input);
		return os.compute().flavors().list();
	}

}