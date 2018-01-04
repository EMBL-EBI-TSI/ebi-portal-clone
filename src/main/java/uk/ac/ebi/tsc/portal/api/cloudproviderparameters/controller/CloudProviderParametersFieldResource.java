package uk.ac.ebi.tsc.portal.api.cloudproviderparameters.controller;

import org.springframework.hateoas.ResourceSupport;
import uk.ac.ebi.tsc.portal.api.cloudproviderparameters.repo.CloudProviderParametersField;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
public class CloudProviderParametersFieldResource extends ResourceSupport {

    private String key;
    private String value;

    public CloudProviderParametersFieldResource() {
    }

    public CloudProviderParametersFieldResource(CloudProviderParametersField cloudProviderParametersField) {
        this.key = cloudProviderParametersField.getKey();
//        this.value = cloudProviderParametersField.getValue();
        this.value = "ENCRYPTED VALUE";
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
