package uk.ac.ebi.tsc.portal.usage.deployment.model;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Component
public class DocUpdate {

    @JsonProperty("total_running_time")
    public long totalRunningTime;

    @JsonProperty("@timestamp")
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss")
    public Date timeStamp;

}
