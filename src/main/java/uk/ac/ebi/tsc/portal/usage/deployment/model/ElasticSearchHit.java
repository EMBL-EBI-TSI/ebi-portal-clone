package uk.ac.ebi.tsc.portal.usage.deployment.model;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Component
public class ElasticSearchHit {
    public String _index;
    public String _type;
    public int _score;
    public DeploymentDocument _source;
}
