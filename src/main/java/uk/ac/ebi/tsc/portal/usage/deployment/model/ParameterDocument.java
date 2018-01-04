package uk.ac.ebi.tsc.portal.usage.deployment.model;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 **/

public class ParameterDocument {

    private String key;

    private String value;

    public ParameterDocument() {
    }

    public ParameterDocument(String key, String value) {
        this.key = key;
        this.value = value;
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
}
