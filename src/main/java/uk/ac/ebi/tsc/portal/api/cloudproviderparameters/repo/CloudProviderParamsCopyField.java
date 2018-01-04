package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="cloud_provider_params_copy_field")
public class CloudProviderParamsCopyField {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String key;

    public String value;

    @ManyToOne
    @JoinColumn(name="cloud_provider_params_copy_id")
    public CloudProviderParamsCopy cloudProviderParametersCopy;

    CloudProviderParamsCopyField() { 
    }

    public CloudProviderParamsCopyField(String key, String value, CloudProviderParamsCopy cloudProviderParametersCopy) {
        this.key = key;
        this.value = value;
        this.cloudProviderParametersCopy = cloudProviderParametersCopy;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public CloudProviderParamsCopy getCloudProviderParametersCopy() {
		return cloudProviderParametersCopy;
	}

	public void setCloudProviderParametersCopy(CloudProviderParamsCopy cloudProviderParametersCopy) {
		this.cloudProviderParametersCopy = cloudProviderParametersCopy;
	}
}
