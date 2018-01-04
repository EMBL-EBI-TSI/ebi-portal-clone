package uk.ac.ebi.tsc.portal.usage.deployment.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 **/

public class DeploymentDocument {

    private String user;

    @JsonProperty("deployment_reference")
    private String deploymentReference;

    @JsonProperty("cloud_credentials_provider")
    private String cloudCredentialsProvider;

    @JsonProperty("cloud_credentials_name")
    private String cloudCredentialsName;

    @JsonProperty("application_name")
    private String applicationName;

    @JsonProperty("application_contact")
    private String applicationContact;

    @JsonProperty("application_version")
    private String applicationVersion;

    @JsonProperty("status")
    private String status;

    @JsonProperty("deployment_inputs")
    private Collection<ParameterDocument> deploymentInputs = new LinkedList<>();

    @JsonProperty("deployment_inputs_count")
    private long deploymentInputsCount;

    @JsonProperty("deployment_outputs")
    private Collection<ParameterDocument> deploymentOutputs = new LinkedList<>();

    @JsonProperty("deployment_outputs_count")
    private long deploymentOutputsCount;

    @JsonProperty("deployment_parameters")
    private Collection<ParameterDocument> deploymentParameters = new LinkedList<>();

    @JsonProperty("deployment_parameters_count")
    private long deploymentParametersCount;

    @JsonProperty("started_time")
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date startedTime;

    @JsonProperty("deployed_time")
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date deployedTime;

    @JsonProperty("failed_time")
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date failedTime;

    @JsonProperty("destroyed_time")
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date destroyedTime;

    @JsonProperty("total_running_time")
    private long totalRunningTime;

    @JsonProperty("total_vcpus")
    private double totalVcpus;

    @JsonProperty("total_ram_gb")
    private double totalRamGb;

    @JsonProperty("total_disk_gb")
    private double totalDiskGb;

    @JsonProperty("instance_count")
    private long instanceCount;

    @JsonProperty("error_cause")
    private String errorCause;

    @JsonProperty("@timestamp")
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date timeStamp;

    public DeploymentDocument() {
    }

    public DeploymentDocument(
            String user,
            String deploymentReference,
            String applicationName,
            String applicationContact,
            String applicationVersion,
            String cloudCredentialsProvider,
            String cloudCredentialsName) {
        this.user = user;
        this.deploymentReference = deploymentReference;
        this.applicationName = applicationName;
        this.applicationContact = applicationContact;
        this.applicationVersion = applicationVersion;
        this.cloudCredentialsProvider = cloudCredentialsProvider;
        this.cloudCredentialsName = cloudCredentialsName;
        this.timeStamp = new Date(System.currentTimeMillis());
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDeploymentReference() {
        return deploymentReference;
    }

    public void setDeploymentReference(String deploymentReference) {
        this.deploymentReference = deploymentReference;
    }

    public String getCloudCredentialsProvider() {
        return cloudCredentialsProvider;
    }

    public void setCloudCredentialsProvider(String cloudCredentialsProvider) {
        this.cloudCredentialsProvider = cloudCredentialsProvider;
    }

    public String getCloudCredentialsName() {
        return cloudCredentialsName;
    }

    public void setCloudCredentialsName(String cloudCredentialsName) {
        this.cloudCredentialsName = cloudCredentialsName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationContact() {
        return applicationContact;
    }

    public void setApplicationContact(String applicationContact) {
        this.applicationContact = applicationContact;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Collection<ParameterDocument> getDeploymentInputs() {
        return deploymentInputs;
    }

    public void setDeploymentInputs(Collection<ParameterDocument> deploymentInputs) {
        this.deploymentInputs = deploymentInputs;
        this.deploymentInputsCount = deploymentInputs.size();
    }

    public Collection<ParameterDocument> getDeploymentOutputs() {
        return deploymentOutputs;
    }

    public void setDeploymentOutputs(Collection<ParameterDocument> deploymentOutputs) {
        this.deploymentOutputs = deploymentOutputs;
        this.deploymentOutputsCount = deploymentOutputs.size();
    }

    public Collection<ParameterDocument> getDeploymentParameters() {
        return deploymentParameters;
    }

    public void setDeploymentParameters(Collection<ParameterDocument> deploymentParameters) {
        this.deploymentParameters = deploymentParameters;
        this.deploymentParametersCount = deploymentParameters.size();
    }

    public Date getStartedTime() {
        return startedTime;
    }

    public void setStartedTime(Date startedTime) {
        this.startedTime = startedTime;
    }

    public Date getDeployedTime() {
        return deployedTime;
    }

    public void setDeployedTime(Date deployedTime) {
        this.deployedTime = deployedTime;
    }

    public Date getFailedTime() {
        return failedTime;
    }

    public void setFailedTime(Date failedTime) {
        this.failedTime = failedTime;
    }

    public Date getDestroyedTime() {
        return destroyedTime;
    }

    public void setDestroyedTime(Date destroyedTime) {
        this.destroyedTime = destroyedTime;
    }

    public long getTotalRunningTime() {
        return totalRunningTime;
    }

    public void setTotalRunningTime(long totalRunningTime) {
        this.totalRunningTime = totalRunningTime;
    }

    public String getErrorCause() {
        return errorCause;
    }

    public double getTotalVcpus() {
        return totalVcpus;
    }

    public void setTotalVcpus(double totalVcpus) {
        this.totalVcpus = totalVcpus;
    }

    public double getTotalRamGb() {
        return totalRamGb;
    }

    public void setTotalRamGb(double totalRamGb) {
        this.totalRamGb = totalRamGb;
    }

    public double getTotalDiskGb() {
        return totalDiskGb;
    }

    public void setTotalDiskGb(double totalDiskGb) {
        this.totalDiskGb = totalDiskGb;
    }

    public void setErrorCause(String errorCause) {
        this.errorCause = errorCause;
    }


    public long getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(long instanceCount) {
        this.instanceCount = instanceCount;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return super.toString();
    }
}
