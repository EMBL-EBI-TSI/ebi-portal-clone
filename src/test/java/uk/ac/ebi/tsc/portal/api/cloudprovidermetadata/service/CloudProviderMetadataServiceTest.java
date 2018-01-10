package uk.ac.ebi.tsc.portal.api.cloudprovidermetadata.service;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.client.IOSClientBuilder;
import org.openstack4j.api.networking.NetworkService;
import org.openstack4j.api.networking.NetworkingService;
import org.openstack4j.model.common.Identifier;

import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.network.Network;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import uk.ac.ebi.tsc.portal.api.cloudprovidermetadata.controller.CloudProviderMetadataResource;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class CloudProviderMetadataServiceTest {
    @MockBean
    private CloudProviderMetadataResource cloudProviderMetadataResource;

    @MockBean
    private CloudProviderMetadataService cloudProviderMetadataService;

    @MockBean
    private IOSClientBuilder.V2 v2Builder;

    @MockBean
    private IOSClientBuilder.V3 v3Builder;

    List<Flavor> flavors;

    List<Network> networks;

    List<String> ippools;

    OSClient.OSClientV2 clientV2;

    OSClient.OSClientV3 clientV3;

    Identifier domainIdentifier;

    NetworkingService networkingService;

    NetworkService networkService;

    @Before
    public void setUp(){


        when(this.v2Builder.endpoint("http://www.api.com")).thenReturn(v2Builder);
        when(this.v2Builder.credentials("username", "password")).thenReturn(v2Builder);
        when(this.v2Builder.tenantName("tenant name")).thenReturn(v2Builder);
        when(this.v2Builder.authenticate()).thenReturn(clientV2);

        when(this.clientV2.networking()).thenReturn(networkingService);
        when(networkingService.network()).thenReturn(networkService);



        when(this.v3Builder.endpoint("http://www.api.com")).thenReturn(v3Builder);
        when(this.v3Builder.credentials("username", "password", domainIdentifier)).thenReturn(v3Builder);
        when(this.v3Builder.authenticate()).thenReturn(clientV3);



        when(this.cloudProviderMetadataResource.getDomainName()).thenReturn("default");
        when(this.cloudProviderMetadataResource.getEndpoint()).thenReturn("http://www.api.com");
        when(this.cloudProviderMetadataResource.getPassword()).thenReturn("password");
        when(this.cloudProviderMetadataResource.getTenantName()).thenReturn("tenant name");
        when(this.cloudProviderMetadataResource.getUsername()).thenReturn("username");
        when(this.cloudProviderMetadataResource.getVersion()).thenReturn("version");
    }

    /**
     * If user can get flavors
     */
    @Ignore
    @Test
    public void testGetFlavors() {

//        flavors = new LinkedList<>();
//        given(cloudProviderMetadataService.getFlavors(cloudProviderMetadataResource)).willReturn(flavors);
        assertEquals(cloudProviderMetadataService.getFlavors(cloudProviderMetadataResource), flavors);
    }

    /**
     * If user can get networks
     */
    @Ignore
    @Test
    public void testGetNetworks() {
//        given(cloudProviderMetadataService.getNetworks(cloudProviderMetadataResource)).willReturn(networks); ;
        assertEquals(cloudProviderMetadataService.getNetworks(cloudProviderMetadataResource), networks);
    }

    /**
     * If user can get ippools
     */
    @Ignore
    @Test
    public void testGetIppools() {
        given(cloudProviderMetadataService.getIPPools(cloudProviderMetadataResource)).willReturn(ippools); ;
        assertEquals(cloudProviderMetadataService.getNetworks(cloudProviderMetadataResource), ippools);
    }

}
