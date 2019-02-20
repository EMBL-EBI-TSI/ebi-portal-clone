package uk.ac.ebi.tsi.portal.api.deployment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ebi.tsc.portal.BePortalApiApplication;
import uk.ac.ebi.tsc.portal.config.WebConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {WebConfiguration.class, BePortalApiApplication.class})
@TestPropertySource("classpath:integrationTest.properties")
@AutoConfigureMockMvc
public class DeploymentRestControllerIT {

	private static final Logger logger = Logger.getLogger(DeploymentRestControllerIT.class);

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private MockMvc mockMvc;

	@Value("${aapUserName}") 
	private String aapUserName;
	
	@Value("${aapPassword}")
	private String aapPassword;
	
	@Value("${aapUrl}")
	private String aapUrl;

	private String token;

	@Before
	public void setup() throws Exception{
		//get jwt token
		ResponseEntity<String> response = restTemplate.withBasicAuth(aapUserName, aapPassword)
				.getForEntity(aapUrl, String.class);
		token = response.getBody();
	}


	@Test
	public void testIfApplicationsHavingDeploymentsAreDeleted() throws Exception{ 

		//add cloud provider
		String cloudProviderJson = "{\n  \"name\" : \"Embassy OpenStack\",\n  \"cloudProvider\" : \"OSTACK\",\n "
				+ " \"fields\" : [\n    {\n      \"key\" : \"OS_USERNAME\",\n      \"value\" : \"navis\"\n    },"
				+ "\n    {\n      \"key\" : \"OS_TENANT_NAME\",\n      \"value\" : \"EBI-TSI\"\n    },\n  "
				+ "  {\n      \"key\" : \"OS_AUTH_URL\",\n      \"value\" : \"https://extcloud03-keystone.ebi.ac.uk:5000/v2.0\"\n    },\n   "
				+ " {\n      \"key\" : \"OS_PASSWORD\",\n      \"value\" : \"alleluia!9\"\n    },\n    {\n      \"key\" : \"TF_VAR_floatingip_pool\",\n   "
				+ "   \"value\" : \"net_external\"\n    },\n    {\n      \"key\" : \"TF_VAR_key_name\",\n      \"value\" : \"navis\"\n    },\n    {\n   "
				+ "   \"key\" : \"TF_VAR_machine_type\",\n      \"value\" : \"s1.huge\"\n    },\n   "
				+ " {\n      \"key\" : \"TF_VAR_disk_image\",\n      \"value\" : \"45938a1d-ade8-4634-bc10-d7096aa4b455\"\n    }\n  ]\n}\n";

		mockMvc.perform(
				post("/cloudproviderparameters")
				.headers(createHeaders(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content(cloudProviderJson)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(jsonPath("name").value("Embassy OpenStack"))
		.andReturn();

		//add deployment parameters
		String deploymentParametersJson = "{\n\"name\":\"Pass\","
				+ "\n\"fields\":[{\n\"key\":\"floatingip_pool\",\n\"value\":\"net-external\"\n},\n{\n\"key\":\"machine_type\",\n\"value\":\"s1.small\"\n}\n]\n}";
		mockMvc.perform(
				post("/configuration/deploymentparameters")
				.headers(createHeaders(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content(deploymentParametersJson)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("name").value("Pass"))
				.andReturn();

		/*add configuration
		String configurationJson = "{\n\"name\":\"Pass\",\n\"cloudProviderParametersName\":\"Embassy OpenStack\",\n\"deploymentParametersName\":"
				+ "\"Pass\",\n\"sshKey\":\"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDZVcFCW"
				+ "LE6uBP0UMpEtTi8TpLJiQZ2UgumitODfsN46WSGnebs481fxGa/YX2nuY5teRIRaqizJOcqoXi9"
				+ "XC/xTJKShjeFrrQ03KMAM4nrxpNr8stCnV5+hRaKiugTWeGWXB01fAoQJtSq2FXmCObJcF0rWC6"
				+ "b+YlcBmWaW7n0E8+c9X7WxwM8EMoX17T+EfsUlfFwahGXGmY5zBmtdguBOuAKCTp9Sm7KH24Q"
				+ "JdUVV64w/0AGHFd8nkIM/4BYYr8cij2sOwksju9iHDBuBC/mWeQHVYMrFSOIcvn59yabIxIdo+F2X"
				+ "LOdeZu4Ipig+/TFdV7qloxn0k7BJZRVHO/6wKguIEdyYcQioW/pJdWHlMMYyqO2NdVQ7W8FkWfMRHMe2hre9jDV1XD"
				+ "PNI9l1BDrRFa43nqcfXA8A7ZpTV3no6idgHrgKOPHMNvbTw9ZwrUiPv8ZVOGi8BpO07JNUuSy76wt8zmyHC8vaBUIx4WCjHx"
				+ "6DYs9lfRL/BEmgPzNbtKRhi668IAyV4H2rZyLyIt8syXrCelLUf2EuIOtNzVErwfKCN9VmnK33vDTZifLIkVIK4nwCWh9X5nCOUD8y+cKW"
				+ "BpgFLpZHcozenAkK0sQmPySJyEJJ7XVEkrCSnB9AipvRBPwL+lHXPi4VlElNfQOfUB1+pYBA8+0t2m2I76LAQ== navis@ebi.ac.uk\"\n}";
		mockMvc.perform(
				post("/configuration")
				.headers(createHeaders(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content(configurationJson)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("name").value("Pass"))
				.andReturn();*/
	}


	protected HttpHeaders createHeaders(String token) {
		return new HttpHeaders() {
			private static final long serialVersionUID = 1L;
			{
				String authHeader = "Bearer " + token;
				set("Authorization", authHeader);
				set("Content-Type", "application/json");
				set("Accept", "application/hal+json");
			}};
	}


}
