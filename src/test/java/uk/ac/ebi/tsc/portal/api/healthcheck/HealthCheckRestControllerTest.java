package uk.ac.ebi.tsc.portal.api.healthcheck;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.ac.ebi.tsc.portal.api.healthcheck.controller.HealthCheckRestController;

/**
 * 
 * @author navis 
 * @email <navis@ebi.ac.uk>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class HealthCheckRestControllerTest {
	
	private HealthCheckRestController testCandidate =  new HealthCheckRestController();
	
	/**
	 * Test to check if portal application is up and running
	 */
	@Test
	public void setUp(){
		assertEquals(testCandidate.ping(),"pong");
	}
}
