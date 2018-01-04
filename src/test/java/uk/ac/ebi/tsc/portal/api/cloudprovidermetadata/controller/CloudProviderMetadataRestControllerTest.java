package uk.ac.ebi.tsc.portal.api.cloudprovidermetadata.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import uk.ac.ebi.tsc.portal.api.cloudprovidermetadata.service.CloudProviderMetadataService;

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class CloudProviderMetadataRestControllerTest {

    @MockBean
    private CloudProviderMetadataRestController subject;

    @MockBean
    private CloudProviderMetadataService service;

    @MockBean
    private CloudProviderMetadataResource input;

    @Before
    public void setUp(){
        ReflectionTestUtils.setField(subject, "cloudProviderMetadataService", service);

        when(this.input.getDomainName()).thenReturn("default");
        when(this.input.getEndpoint()).thenReturn("http://www.api.com");
        when(this.input.getPassword()).thenReturn("password");
        when(this.input.getTenantName()).thenReturn("tenant name");
        when(this.input.getUsername()).thenReturn("username");
        when(this.input.getVersion()).thenReturn("version");
    }

    /**
     * test if service can get flavors
     */
    @Test
    public void testGetFlavors(){
        given(subject.getFlavors(input)).willCallRealMethod();
        assertTrue(subject.getFlavors(input) != null );
    }

    /**
     * test if service can get networks
     */
    @Test
    public void testGetNetworks(){
        given(subject.getNetworks(input)).willCallRealMethod();
        assertTrue(subject.getNetworks(input) != null );
    }

    /**
     * test if service can get ippools
     */
    @Test
    public void testGetIPPools(){
        given(subject.getIPPools(input)).willCallRealMethod();
        assertTrue(subject.getIPPools(input) != null );
    }
}
