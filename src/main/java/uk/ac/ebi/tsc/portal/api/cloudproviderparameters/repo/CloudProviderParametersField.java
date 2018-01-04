package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo;

import javax.persistence.*;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
@Entity
public class CloudProviderParametersField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String key;

    public String value;

    @ManyToOne
    public CloudProviderParameters cloudProviderParameters;

    CloudProviderParametersField() { // jpa only
    }

    public CloudProviderParametersField(String key, String value, CloudProviderParameters cloudProviderParameters) {
        this.key = key;
        this.value = value;
        this.cloudProviderParameters = cloudProviderParameters;
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

    public CloudProviderParameters getCloudProviderParameters() {
        return cloudProviderParameters;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
    
    
}
