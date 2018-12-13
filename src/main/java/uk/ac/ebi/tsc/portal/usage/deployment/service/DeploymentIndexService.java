package uk.ac.ebi.tsc.portal.usage.deployment.service;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.tsc.portal.usage.deployment.model.*;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Service
public class DeploymentIndexService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentIndexService.class);

    @Autowired
    private RestTemplate restTemplate;
    
    private String indexUrl;
    
    private String username;
  
    private String password;

    @Autowired
    public DeploymentIndexService(RestTemplate restTemplate,  
    	    @Qualifier("elasticindexurl")  String indexUrl, 
    	    @Qualifier("elasticsearchusername") String username, 
    	    @Qualifier("elasticsearchpassword")  String password) {
        this.restTemplate = restTemplate;
        this.indexUrl = indexUrl;
        this.username = username;
        this.password = password;
    }

    public DeploymentDocument findById(String id) {

        logger.debug("Retrieving DeploymentDocument with id " + id + " from index at " + this.indexUrl);

        Search search = new Search();
        Query query = new Query();
        query.match_all = "";
        search.query = query;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", this.getAuthHeader());
        HttpEntity<?> requestEntity = new HttpEntity<>(search, headers);


        ResponseEntity<ElasticSearchResult> response =
                restTemplate.exchange(
                        this.indexUrl + "/_search?q=_id:" + id,
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<ElasticSearchResult>() { }
                );


        if (response.getBody().hits.hits.length==0) {
            return null;
        }

        List<DeploymentDocument> res =
                Arrays.stream(response.getBody().hits.hits).map(hit -> hit._source).collect(Collectors.toList());

        if (res == null || res.size()==0) {
            return null;
        }

        return res.get(0);
    }

    public Collection<DeploymentDocument> findByStatus(String status) {

        logger.debug("Retrieving all DeploymentDocument by status " + status + " from index at " + this.indexUrl);

        Search search = new Search();
        Query query = new Query();
        query.match_all = "";
        search.query = query;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", this.getAuthHeader());
        HttpEntity<?> requestEntity = new HttpEntity<>(search, headers);

        ResponseEntity<ElasticSearchResult> response =
                restTemplate.exchange(
                        this.indexUrl + "/_search?q=status:" + status,
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<ElasticSearchResult>() { }
                );


        if (response.getBody().hits.hits.length==0) {
            return new LinkedList<>();
        }

        List<DeploymentDocument> res =
                Arrays.stream(response.getBody().hits.hits).map(hit -> hit._source).collect(Collectors.toList());

        if (res == null || res.size()==0) {
            return new LinkedList<>();
        }

        return res;
    }

    public void save(DeploymentDocument deploymentDocument) {

        logger.debug("Indexing to index index at " + this.indexUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", this.getAuthHeader());
        HttpEntity<?> requestEntity = new HttpEntity<>(deploymentDocument, headers);

        logger.debug("Indexing with reference", deploymentDocument.getDeploymentReference());

        restTemplate.exchange(
                        this.indexUrl+ "/deployment/" + deploymentDocument.getDeploymentReference(),
                        HttpMethod.PUT,
                        requestEntity,
                        Object.class
                );
    }

    public void saveTotalRunningTime(String deploymentReference, long totalRunningTime) {

        logger.debug("Indexing to index index at " + this.indexUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", this.getAuthHeader());

        Update update = new Update();
        DocUpdate doc = new DocUpdate();
        doc.totalRunningTime = totalRunningTime;
        doc.timeStamp = new Date(System.currentTimeMillis());

        update.doc = doc;

        HttpEntity<?> requestEntity = new HttpEntity<>(update, headers);

        restTemplate.exchange(
                this.indexUrl+ "/deployment/" + deploymentReference + "/_update",
                HttpMethod.POST,
                requestEntity,
                Object.class
        );
    }

    private String getAuthHeader(){
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("US-ASCII")) );
        String authHeader = "Basic " + new String( encodedAuth );

        return authHeader;
    }

    
}
