package uk.ac.ebi.tsc.portal.clouddeployment.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ebi.tsc.portal.clouddeployment.model.ApplicationManifest;

import java.io.File;
import java.io.IOException;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
public class ManifestParser {

    public static ApplicationManifest parseApplicationManifest(String manifestFilePath) throws IOException {
        ApplicationManifest applicationManifest;

        ObjectMapper objectMapper = new ObjectMapper();
        applicationManifest = objectMapper.readValue(new File(manifestFilePath), ApplicationManifest.class);

        return applicationManifest;
    }
}
