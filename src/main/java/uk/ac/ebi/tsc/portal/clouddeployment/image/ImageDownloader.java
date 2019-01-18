package uk.ac.ebi.tsc.portal.clouddeployment.image;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
@Component
public class ImageDownloader {

    private static final Logger logger = LoggerFactory.getLogger(ImageDownloader.class);


    public static int downloadImage(String imageUri, String path) throws IOException {

        logger.debug("Downloading image at: " + imageUri);

        FileUtils.copyURLToFile(new URL(imageUri),  new File(path));

        return 0;
    }
}
