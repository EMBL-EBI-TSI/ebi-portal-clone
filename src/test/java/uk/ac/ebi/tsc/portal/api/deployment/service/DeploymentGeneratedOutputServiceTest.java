package uk.ac.ebi.tsc.portal.api.deployment.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ebi.tsc.portal.api.deployment.controller.DeploymentGeneratedOutputResource;
import uk.ac.ebi.tsc.portal.api.deployment.repo.Deployment;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentGeneratedOutput;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentRepository;
import uk.ac.ebi.tsc.portal.api.deployment.repo.DeploymentSecret;
import uk.ac.ebi.tsc.portal.api.error.ErrorMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * @author Felix Xavier <famaladoss@ebi.ac.uk>
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class DeploymentGeneratedOutputServiceTest {

    private DeploymentRepository deploymentRepositoryMock;
    private DeploymentGeneratedOutputService testCandidate;
    Deployment theDeployment;
    DeploymentSecret deploymentSecret;
    DeploymentGeneratedOutput deploymentGeneratedOutput1;
    DeploymentGeneratedOutput deploymentGeneratedOutput2;
    List<DeploymentGeneratedOutputResource> payLoadGeneratedOutputList = new ArrayList<>();
    List<DeploymentGeneratedOutput> depGenlist = new ArrayList<>();
    String deploymentReference = "TSI000001";
    String secret = "SECRET";

    @Before
    public void setUp() {
        deploymentRepositoryMock = mock(DeploymentRepository.class);
        theDeployment = mock(Deployment.class);
        deploymentSecret = mock(DeploymentSecret.class);
        testCandidate = new DeploymentGeneratedOutputService(deploymentRepositoryMock);
        deploymentGeneratedOutput1 = new DeploymentGeneratedOutput("externalIP", "193.167.5.4", theDeployment);
        DeploymentGeneratedOutputResource outputResource1 = new DeploymentGeneratedOutputResource(deploymentGeneratedOutput1);
        payLoadGeneratedOutputList.add(outputResource1);
        deploymentGeneratedOutput2 = new DeploymentGeneratedOutput("sequence", generateString(1000), theDeployment);
        DeploymentGeneratedOutputResource outputResource2 = new DeploymentGeneratedOutputResource(deploymentGeneratedOutput2);
        payLoadGeneratedOutputList.add(outputResource2);
    }

    @Test
    public void testMissingSecret() throws DeploymentNotFoundException {

        Optional<ErrorMessage> errorMessageOpt = testCandidate.saveOrUpdateDeploymentOutputs(deploymentReference, null, payLoadGeneratedOutputList);
        ErrorMessage errorMessage = errorMessageOpt.get();
        assertEquals(HttpStatus.BAD_REQUEST, errorMessage.getStatus());
        assertEquals("Missing header : secret", errorMessage.getError());
    }

    @Test(expected = DeploymentNotFoundException.class)
    public void testInvalidSecret() throws DeploymentNotFoundException {

        String invalid_secret = "INVALID_SECRET";
        given(deploymentSecret.getSecret()).willReturn(secret);
        given(theDeployment.getDeploymentSecret()).willReturn(deploymentSecret);
        given(deploymentRepositoryMock.findByReference(deploymentReference)).willReturn(Optional.of(theDeployment));
        testCandidate.saveOrUpdateDeploymentOutputs(deploymentReference, invalid_secret, payLoadGeneratedOutputList);
    }

    @Test(expected = DeploymentNotFoundException.class)
    public void testInvalidReference() throws DeploymentNotFoundException {

        String invalid_reference = "TOI00002";
        given(deploymentRepositoryMock.findByReference(invalid_reference)).willReturn(Optional.empty());
        testCandidate.saveOrUpdateDeploymentOutputs(invalid_reference, secret, payLoadGeneratedOutputList);
    }

    @Test
    public void testDuplicateOutputName() throws DeploymentNotFoundException {
        DeploymentGeneratedOutputResource outputResource = new DeploymentGeneratedOutputResource();
        outputResource.setOutputName("sequence");
        outputResource.setGeneratedValue(generateString(500));
        payLoadGeneratedOutputList.add(outputResource);

        given(deploymentSecret.getSecret()).willReturn(secret);
        given(theDeployment.getDeploymentSecret()).willReturn(deploymentSecret);
        given(deploymentRepositoryMock.findByReference(deploymentReference)).willReturn(Optional.of(theDeployment));

        Optional<ErrorMessage> errorMessageOpt = testCandidate.saveOrUpdateDeploymentOutputs(deploymentReference, secret, payLoadGeneratedOutputList);
        ErrorMessage errorMessage = errorMessageOpt.get();
        assertEquals(HttpStatus.CONFLICT, errorMessage.getStatus());
        assertEquals("outputName should be unique for given List", errorMessage.getError());
    }

    @Test
    public void testDeploymentOutputExceedsLimit() throws DeploymentNotFoundException {

        DeploymentGeneratedOutputResource outputResource = new DeploymentGeneratedOutputResource();
        outputResource.setOutputName("result");
        outputResource.setGeneratedValue(generateString(1000001));
        payLoadGeneratedOutputList.add(outputResource);

        given(deploymentSecret.getSecret()).willReturn(secret);
        given(theDeployment.getDeploymentSecret()).willReturn(deploymentSecret);
        given(theDeployment.getGeneratedOutputs()).willReturn(depGenlist);
        given(deploymentRepositoryMock.findByReference(deploymentReference)).willReturn(Optional.of(theDeployment));

        Optional<ErrorMessage> errorMessageOpt = testCandidate.saveOrUpdateDeploymentOutputs(deploymentReference, secret, payLoadGeneratedOutputList);
        ErrorMessage errorMessage = errorMessageOpt.get();
        assertEquals(HttpStatus.BAD_REQUEST, errorMessage.getStatus());
        assertEquals("Key/Value pair should not exceed 1MB for a deployment", errorMessage.getError());
    }


    @Test
    public void testAddDeploymentOutputs1() throws DeploymentNotFoundException {

        given(deploymentSecret.getSecret()).willReturn(secret);
        given(theDeployment.getDeploymentSecret()).willReturn(deploymentSecret);
        given(theDeployment.getGeneratedOutputs()).willReturn(depGenlist);
        given(deploymentRepositoryMock.findByReference(deploymentReference)).willReturn(Optional.of(theDeployment));

        Optional<ErrorMessage> errorMessageOpt = testCandidate.saveOrUpdateDeploymentOutputs(deploymentReference, secret, payLoadGeneratedOutputList);
        assertFalse(errorMessageOpt.isPresent());
        assertEquals(2, testCandidate.deploymentGeneratedOutputList.size());
        assertEquals(deploymentGeneratedOutput1.getOutputName(), testCandidate.deploymentGeneratedOutputList.get(0).getOutputName());
        assertEquals(deploymentGeneratedOutput2.getOutputName(), testCandidate.deploymentGeneratedOutputList.get(1).getOutputName());
    }

    @Test
    public void testAddDeploymentOutputs2() throws DeploymentNotFoundException {

        DeploymentGeneratedOutput output1 = new DeploymentGeneratedOutput("internalIP", "10.90.10.4", theDeployment);
        depGenlist.add(output1);

        given(deploymentSecret.getSecret()).willReturn(secret);
        given(theDeployment.getDeploymentSecret()).willReturn(deploymentSecret);
        given(theDeployment.getGeneratedOutputs()).willReturn(depGenlist);
        given(deploymentRepositoryMock.findByReference(deploymentReference)).willReturn(Optional.of(theDeployment));

        Optional<ErrorMessage> errorMessageOpt = testCandidate.saveOrUpdateDeploymentOutputs(deploymentReference, secret, payLoadGeneratedOutputList);
        assertFalse(errorMessageOpt.isPresent());
        assertEquals(3, testCandidate.deploymentGeneratedOutputList.size());
        assertEquals(deploymentGeneratedOutput1.getOutputName(), testCandidate.deploymentGeneratedOutputList.get(1).getOutputName());
        assertEquals(deploymentGeneratedOutput2.getOutputName(), testCandidate.deploymentGeneratedOutputList.get(2).getOutputName());
    }

    @Test
    public void testUpdateDeploymentOutput() throws DeploymentNotFoundException {

        DeploymentGeneratedOutput output1 = new DeploymentGeneratedOutput("sequence", "FRTTGG", theDeployment);
        DeploymentGeneratedOutput output2 = new DeploymentGeneratedOutput("internalIP", "10.90.10.4", theDeployment);
        depGenlist.add(output1);
        depGenlist.add(output2);

        given(deploymentSecret.getSecret()).willReturn(secret);
        given(theDeployment.getDeploymentSecret()).willReturn(deploymentSecret);
        given(deploymentRepositoryMock.findByReference(deploymentReference)).willReturn(Optional.of(theDeployment));
        given(theDeployment.getGeneratedOutputs()).willReturn(depGenlist);

        Optional<ErrorMessage> errorMessageOpt = testCandidate.saveOrUpdateDeploymentOutputs(deploymentReference, secret, payLoadGeneratedOutputList);
        assertFalse(errorMessageOpt.isPresent());
        assertEquals(3, testCandidate.deploymentGeneratedOutputList.size());
        assertEquals(deploymentGeneratedOutput2.getValue(), testCandidate.deploymentGeneratedOutputList.get(0).getValue());
        assertEquals("10.90.10.4", testCandidate.deploymentGeneratedOutputList.get(1).getValue());
        assertEquals(deploymentGeneratedOutput1.getValue(), testCandidate.deploymentGeneratedOutputList.get(2).getValue());
    }

    private String generateString(int size) {
        StringBuilder build = new StringBuilder();
        for (int i = 0; i < size; i++) {
            build.append("f");
        }
        return build.toString();
    }
}
