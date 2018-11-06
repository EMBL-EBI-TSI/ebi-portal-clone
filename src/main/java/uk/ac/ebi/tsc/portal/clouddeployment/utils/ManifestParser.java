package uk.ac.ebi.tsc.portal.clouddeployment.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ebi.tsc.portal.clouddeployment.model.ApplicationManifest;

import java.io.File;
import java.io.IOException;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class ManifestParser {

    public static ApplicationManifest parseApplicationManifest(String manifestFilePath) {
        
        try 
        {
            ObjectMapper objectMapper = new ObjectMapper();
        
            return objectMapper.readValue(new File(manifestFilePath), ApplicationManifest.class);
        } 
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
