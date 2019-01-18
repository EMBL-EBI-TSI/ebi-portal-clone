package uk.ac.ebi.tsc.portal.clouddeployment.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import uk.ac.ebi.tsc.portal.clouddeployment.utils.InputStreamLogger;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class ImageInstaller {

    private static final Logger logger = LoggerFactory.getLogger(ImageInstaller.class);

    private static final String GLANCE_COMMAND = "glance";


    private String osUsername;
    private String osTenantName;
    private String osAuthUrl;
    private String osPassword;

    public ImageInstaller(String osUsername, String osTenantName, String osAuthUrl, String osPassword) {
        this.osUsername = osUsername;
        this.osTenantName = osTenantName;
        this.osAuthUrl = osAuthUrl;
        this.osPassword = osPassword;
    }

    public int installImage(String imagePath, String diskFormat, String imageName) {
        try {
            activateVirtualEnvironment("venv");

            // Call glance
            uploadImageToProvider(imagePath, diskFormat, imageName);

            // Deactivate venv
            deactivateVirtualEnvironment();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0; // TODO: temporary
    }


    private int activateVirtualEnvironment(String virtualEnvironmentName) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "source",
                virtualEnvironmentName + File.separator + "bin" + File.separator + "activate");

        Process p = processBuilder.start();

        // TODO: return something meaningful?

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (p.exitValue() != 0) {
            InputStreamLogger.logInputStream(p.getErrorStream());
        } else {
            InputStreamLogger.logInputStream(p.getInputStream());
        }

        return p.exitValue();
    }

    private int uploadImageToProvider(String imagePath, String diskFormat, String imageName) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "glance", "image-create",
                "--file", imagePath,
                "--disk-format", diskFormat,
                "--container-format", "bare",
                "--name", imageName
                );

        Map<String, String> env = processBuilder.environment();
        env.put("OS_USERNAME", this.osUsername);
        env.put("OS_TENANT_NAME", this.osTenantName);
        env.put("OS_AUTH_URL", this.osAuthUrl);
        env.put("OS_PASSWORD", this.osPassword);

        Process p = processBuilder.start();

        // TODO: return something meaningful?

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (p.exitValue() != 0) {
            InputStreamLogger.logInputStream(p.getErrorStream());
        } else {
            InputStreamLogger.logInputStream(p.getInputStream());
        }

        return p.exitValue();
    }

    private int deactivateVirtualEnvironment() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("deactivate");

        Process p = processBuilder.start();

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (p.exitValue() != 0) {
            InputStreamLogger.logInputStream(p.getErrorStream());
        } else {
            InputStreamLogger.logInputStream(p.getInputStream());
        }

        return p.exitValue();
    }

}
