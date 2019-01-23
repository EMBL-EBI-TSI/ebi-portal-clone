package uk.ac.ebi.tsc.portal.api.deployment.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ebi.tsc.portal.api.deployment.bean.DeploymentOutputsProcessResult;
import uk.ac.ebi.tsc.portal.api.deployment.controller.DeploymentGeneratedOutputResource;
import uk.ac.ebi.tsc.portal.api.deployment.repo.*;
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

    final String DEPLOY_REFERENCE = "TSI000001";
    final String INVALID_REFERENCE = "TOI00002";
    final String SECRET = "SECRET";
    final String INVALID_SECRET = "INVALID_SECRET";
    Deployment theDeployment = new Deployment(DEPLOY_REFERENCE, null, null, "", "");
    DeploymentSecret deploymentSecret = new DeploymentSecret(null, SECRET);
    DeploymentGeneratedOutput deploymentGeneratedOutput1 = new DeploymentGeneratedOutput("externalIP", "193.167.5.4", theDeployment);
    DeploymentGeneratedOutput deploymentGeneratedOutput2 = new DeploymentGeneratedOutput("sequence", generateString(1000), theDeployment);
    List<DeploymentGeneratedOutputResource> payLoadGeneratedOutputList = new ArrayList<>();
    List<DeploymentGeneratedOutput> depGenlist = new ArrayList<>();

    private DeploymentRepository deploymentRepositoryMock = mock(DeploymentRepository.class);
    private DeploymentGeneratedOutputService testCandidate = new DeploymentGeneratedOutputService(deploymentRepositoryMock);

    @Test
    public void testMissingSecret() throws DeploymentNotFoundException {

        DeploymentOutputsProcessResult deploymentOutputsProcessResult = testCandidate.saveOrUpdateDeploymentOutputs(DEPLOY_REFERENCE, null, payLoadGeneratedOutputList);
        ErrorMessage errorMessage = deploymentOutputsProcessResult.getErrorMessage().get();
        assertEquals(HttpStatus.BAD_REQUEST, errorMessage.getStatus());
        assertEquals("Missing header : secret", errorMessage.getError());
    }

    @Test(expected = DeploymentNotFoundException.class)
    public void testInvalidSecret() throws DeploymentNotFoundException {
        theDeployment.setDeploymentSecret(deploymentSecret);
        theDeployment.setGeneratedOutputs(depGenlist);
        given(deploymentRepositoryMock.findByReference(DEPLOY_REFERENCE)).willReturn(Optional.of(theDeployment));
        testCandidate.saveOrUpdateDeploymentOutputs(DEPLOY_REFERENCE, INVALID_SECRET, payLoadGeneratedOutputList);
    }

    @Test(expected = DeploymentNotFoundException.class)
    public void testInvalidReference() throws DeploymentNotFoundException {
        given(deploymentRepositoryMock.findByReference(INVALID_REFERENCE)).willReturn(Optional.empty());
        testCandidate.saveOrUpdateDeploymentOutputs(INVALID_REFERENCE, SECRET, payLoadGeneratedOutputList);
    }

    @Test
    public void testDuplicateOutputName() throws DeploymentNotFoundException {

        theDeployment.setDeploymentSecret(deploymentSecret);
        theDeployment.setGeneratedOutputs(depGenlist);
        DeploymentGeneratedOutputResource outputResource1 = new DeploymentGeneratedOutputResource(deploymentGeneratedOutput1);
        payLoadGeneratedOutputList.add(outputResource1);
        DeploymentGeneratedOutputResource outputResource2 = new DeploymentGeneratedOutputResource(deploymentGeneratedOutput2);
        payLoadGeneratedOutputList.add(outputResource2);
        DeploymentGeneratedOutputResource outputResource = new DeploymentGeneratedOutputResource();
        outputResource.setOutputName("sequence");
        outputResource.setGeneratedValue(generateString(500));
        payLoadGeneratedOutputList.add(outputResource);
        given(deploymentRepositoryMock.findByReference(DEPLOY_REFERENCE)).willReturn(Optional.of(theDeployment));

        DeploymentOutputsProcessResult deploymentOutputsProcessResult = testCandidate.saveOrUpdateDeploymentOutputs(DEPLOY_REFERENCE, SECRET, payLoadGeneratedOutputList);
        ErrorMessage errorMessage = deploymentOutputsProcessResult.getErrorMessage().get();
        assertEquals(HttpStatus.CONFLICT, errorMessage.getStatus());
        assertEquals("outputName should be unique for given List", errorMessage.getError());
    }

    @Test
    public void testDeploymentOutputExceedsLimit() throws DeploymentNotFoundException {

        theDeployment.setDeploymentSecret(deploymentSecret);
        theDeployment.setGeneratedOutputs(depGenlist);
        DeploymentGeneratedOutputResource outputResource1 = new DeploymentGeneratedOutputResource(deploymentGeneratedOutput1);
        payLoadGeneratedOutputList.add(outputResource1);
        DeploymentGeneratedOutputResource outputResource2 = new DeploymentGeneratedOutputResource(deploymentGeneratedOutput2);
        payLoadGeneratedOutputList.add(outputResource2);
        DeploymentGeneratedOutputResource outputResource = new DeploymentGeneratedOutputResource();
        outputResource.setOutputName("result");
        outputResource.setGeneratedValue(generateString(1000001));
        payLoadGeneratedOutputList.add(outputResource);
        given(deploymentRepositoryMock.findByReference(DEPLOY_REFERENCE)).willReturn(Optional.of(theDeployment));

        DeploymentOutputsProcessResult deploymentOutputsProcessResult = testCandidate.saveOrUpdateDeploymentOutputs(DEPLOY_REFERENCE, SECRET, payLoadGeneratedOutputList);
        ErrorMessage errorMessage = deploymentOutputsProcessResult.getErrorMessage().get();
        assertEquals(HttpStatus.BAD_REQUEST, errorMessage.getStatus());
        assertEquals("Key/Value pair should not exceed 1MB for a deployment", errorMessage.getError());
    }

    @Test
    public void testAddDeploymentOutputs1() throws DeploymentNotFoundException {

        theDeployment.setDeploymentSecret(deploymentSecret);
        theDeployment.setGeneratedOutputs(depGenlist);
        DeploymentGeneratedOutputResource outputResource1 = new DeploymentGeneratedOutputResource(deploymentGeneratedOutput1);
        payLoadGeneratedOutputList.add(outputResource1);
        DeploymentGeneratedOutputResource outputResource2 = new DeploymentGeneratedOutputResource(deploymentGeneratedOutput2);
        payLoadGeneratedOutputList.add(outputResource2);
        given(deploymentRepositoryMock.findByReference(DEPLOY_REFERENCE)).willReturn(Optional.of(theDeployment));

        DeploymentOutputsProcessResult deploymentOutputsProcessResult = testCandidate.saveOrUpdateDeploymentOutputs(DEPLOY_REFERENCE, SECRET, payLoadGeneratedOutputList);
        Optional<ErrorMessage> errorMessageOptional = deploymentOutputsProcessResult.getErrorMessage();
        List<DeploymentGeneratedOutput> deploymentGeneratedOutputList = deploymentOutputsProcessResult.getDeploymentGeneratedOutputList();

        assertFalse(errorMessageOptional.isPresent());
        assertEquals(2, deploymentOutputsProcessResult.getDeploymentGeneratedOutputList().size());
        assertEquals(deploymentGeneratedOutput1.getOutputName(), deploymentGeneratedOutputList.get(0).getOutputName());
        assertEquals(deploymentGeneratedOutput2.getOutputName(), deploymentGeneratedOutputList.get(1).getOutputName());
    }

    @Test
    public void testAddDeploymentOutputs2() throws DeploymentNotFoundException {

        theDeployment.setDeploymentSecret(deploymentSecret);
        DeploymentGeneratedOutputResource outputResource1 = new DeploymentGeneratedOutputResource(deploymentGeneratedOutput1);
        payLoadGeneratedOutputList.add(outputResource1);
        DeploymentGeneratedOutputResource outputResource2 = new DeploymentGeneratedOutputResource(deploymentGeneratedOutput2);
        payLoadGeneratedOutputList.add(outputResource2);
        DeploymentGeneratedOutput output1 = new DeploymentGeneratedOutput("internalIP", "10.90.10.4", theDeployment);
        depGenlist.add(output1);
        theDeployment.setGeneratedOutputs(depGenlist);
        given(deploymentRepositoryMock.findByReference(DEPLOY_REFERENCE)).willReturn(Optional.of(theDeployment));

        DeploymentOutputsProcessResult deploymentOutputsProcessResult = testCandidate.saveOrUpdateDeploymentOutputs(DEPLOY_REFERENCE, SECRET, payLoadGeneratedOutputList);
        Optional<ErrorMessage> errorMessageOptional = deploymentOutputsProcessResult.getErrorMessage();
        List<DeploymentGeneratedOutput> deploymentGeneratedOutputList = deploymentOutputsProcessResult.getDeploymentGeneratedOutputList();

        assertFalse(errorMessageOptional.isPresent());
        assertEquals(3, deploymentGeneratedOutputList.size());
        assertEquals(deploymentGeneratedOutput1.getOutputName(), deploymentGeneratedOutputList.get(1).getOutputName());
        assertEquals(deploymentGeneratedOutput2.getOutputName(), deploymentGeneratedOutputList.get(2).getOutputName());
    }

    @Test
    public void testUpdateDeploymentOutput() throws DeploymentNotFoundException {

        theDeployment.setDeploymentSecret(deploymentSecret);
        DeploymentGeneratedOutputResource outputResource1 = new DeploymentGeneratedOutputResource(deploymentGeneratedOutput1);
        payLoadGeneratedOutputList.add(outputResource1);
        DeploymentGeneratedOutputResource outputResource2 = new DeploymentGeneratedOutputResource(deploymentGeneratedOutput2);
        payLoadGeneratedOutputList.add(outputResource2);
        DeploymentGeneratedOutput output1 = new DeploymentGeneratedOutput("sequence", "FRTTGG", theDeployment);
        DeploymentGeneratedOutput output2 = new DeploymentGeneratedOutput("internalIP", "10.90.10.4", theDeployment);
        depGenlist.add(output1);
        depGenlist.add(output2);
        theDeployment.setGeneratedOutputs(depGenlist);
        given(deploymentRepositoryMock.findByReference(DEPLOY_REFERENCE)).willReturn(Optional.of(theDeployment));

        DeploymentOutputsProcessResult deploymentOutputsProcessResult = testCandidate.saveOrUpdateDeploymentOutputs(DEPLOY_REFERENCE, SECRET, payLoadGeneratedOutputList);
        Optional<ErrorMessage> errorMessageOptional = deploymentOutputsProcessResult.getErrorMessage();
        List<DeploymentGeneratedOutput> deploymentGeneratedOutputList = deploymentOutputsProcessResult.getDeploymentGeneratedOutputList();

        assertFalse(errorMessageOptional.isPresent());
        assertEquals(3, deploymentGeneratedOutputList.size());
        assertEquals(deploymentGeneratedOutput2.getValue(), deploymentGeneratedOutputList.get(0).getValue());
        assertEquals("10.90.10.4", deploymentGeneratedOutputList.get(1).getValue());
        assertEquals(deploymentGeneratedOutput1.getValue(), deploymentGeneratedOutputList.get(2).getValue());
    }

    private String generateString(int size) {
        StringBuilder build = new StringBuilder();
        for (int i = 0; i < size; i++) {
            build.append("f");
        }
        return build.toString();
    }
}
