package uk.ac.ebi.tsc.portal.api.configuration.repo;

import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.*;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@Entity
public class ConfigurationDeploymentParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String key;

    public String value;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="configuration_deployment_parameters_id")
    public ConfigurationDeploymentParameters configurationDeploymentParameters;

    ConfigurationDeploymentParameter() { // jpa only
    }

    public ConfigurationDeploymentParameter(String key, String value, 
    		ConfigurationDeploymentParameters configurationDeploymentParameters) {
        this.key = key;
        this.value = value;
        this.configurationDeploymentParameters = configurationDeploymentParameters;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

	public ConfigurationDeploymentParameters getConfigurationDeploymentParameters() {
		return configurationDeploymentParameters;
	}

	public void setConfigurationDeploymentParameters(ConfigurationDeploymentParameters configurationDeploymentParameters) {
		this.configurationDeploymentParameters = configurationDeploymentParameters;
	}
    
    
}
