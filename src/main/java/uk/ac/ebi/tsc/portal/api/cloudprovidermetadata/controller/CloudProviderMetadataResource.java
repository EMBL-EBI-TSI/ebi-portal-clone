package uk.ac.ebi.tsc.portal.api.cloudprovidermetadata.controller;


import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.jose4j.json.internal.json_simple.parser.ParseException;
import org.springframework.hateoas.ResourceSupport;

public class CloudProviderMetadataResource extends ResourceSupport {

    private String username;
    private String password;
    private String tenantName;
    private String domainName;
    private String endpoint;
    private String version;

    public CloudProviderMetadataResource(String request) {

        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {
            json = (JSONObject) parser.parse(request);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        username = (String) json.get("username");
        password = (String) json.get("password");
        tenantName = (String) json.get("tenantName");
        domainName = (String) json.get("domainName");
        endpoint = (String) json.get("endpoint");
        version = (String) json.get("version");
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getDomainName() { return domainName; }

    public String getEndpoint() {
        return endpoint;
    }

    public String getVersion() {
        return version;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
