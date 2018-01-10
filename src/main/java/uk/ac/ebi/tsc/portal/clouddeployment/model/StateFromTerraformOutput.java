package uk.ac.ebi.tsc.portal.clouddeployment.model;

import java.util.Map;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class StateFromTerraformOutput {

    private String id;
    private String accessIp;
    private String imageName;
    private Map<String, String> generatedOutputs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccessIp() {
        return accessIp;
    }

    public void setAccessIp(String accessIp) {
        this.accessIp = accessIp;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Map<String, String> getGeneratedOutputs() {
        return generatedOutputs;
    }

    public void setGeneratedOutputs(Map<String, String> generatedOutputs) {
        this.generatedOutputs = generatedOutputs;
    }
}
