package uk.ac.ebi.tsc.portal.api.configuration.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParameter;
import uk.ac.ebi.tsc.portal.api.configuration.repo.ConfigurationDeploymentParameterRepository;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class ConfigurationDeploymentParameterServiceTest {
	
	@MockBean
	private ConfigurationDeploymentParameterService cdpService;

	@MockBean
	private ConfigurationDeploymentParameterRepository cdpRepository;

	@MockBean
	private ConfigurationDeploymentParameter cdp;

	@Before
	public void setUp(){
		cdpRepository = mock(ConfigurationDeploymentParameterRepository.class);
		ReflectionTestUtils.setField(cdpService, "configurationDeploymentParameterRepository", cdpRepository);
	}
	
	/**
	 * test if service returns all of the existing 
	 * deploymentparameters
	 */
	@Test
	public void testFindAll(){
		List<ConfigurationDeploymentParameter> cdpSet = new ArrayList<>();
		cdpSet.add(cdp);
		given(cdpRepository.findAll()).willReturn(cdpSet);
		given(cdpService.findAll()).willCallRealMethod();
		assertEquals(cdpService.findAll(), cdpSet);
	}
	
	/**
	 * test if service saves the deployment parameters
	 */
	@Test
	public void testSave(){
		given(cdpRepository.save(cdp)).willReturn(cdp);
		given(cdpService.save(cdp)).willCallRealMethod();
		assertEquals(cdpService.save(cdp), cdp );
	}

}
