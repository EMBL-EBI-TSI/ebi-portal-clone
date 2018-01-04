package uk.ac.ebi.tsc.portal.api.configuration.repo;

import java.util.Collection;
import java.util.LinkedList;

import javax.persistence.*;

/**
 * 
 * @author navis
 *
 */
@Entity
public class ConfigDeploymentParamCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String key;

    public String value;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="config_deployment_params_id")
    public ConfigDeploymentParamsCopy configDeploymentParamsCopy;

    ConfigDeploymentParamCopy() { // jpa only
    }

    public ConfigDeploymentParamCopy(String key, String value, 
    		ConfigDeploymentParamsCopy configDeploymentParamsCopy) {
        this.key = key;
        this.value = value;
        this.configDeploymentParamsCopy = configDeploymentParamsCopy;
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

	public ConfigDeploymentParamsCopy getConfigDeploymentParamsCopy() {
		return configDeploymentParamsCopy;
	}

	public void setConfigDeploymentParamsCopy(ConfigDeploymentParamsCopy configDeploymentParamsCopy) {
		this.configDeploymentParamsCopy = configDeploymentParamsCopy;
	}
}
