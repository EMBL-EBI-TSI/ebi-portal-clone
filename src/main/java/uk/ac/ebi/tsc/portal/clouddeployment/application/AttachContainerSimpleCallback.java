package uk.ac.ebi.tsc.portal.clouddeployment.application;

import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.AttachContainerResultCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 */
public class AttachContainerSimpleCallback extends AttachContainerResultCallback {
    private String deploymentReference;
    private StringBuffer log = new StringBuffer();
    private FileOutputStream outputFile;


    public AttachContainerSimpleCallback(String deploymentReference, String fileName) throws FileNotFoundException {
        this.deploymentReference = deploymentReference;
        this.outputFile = new FileOutputStream(
                System.getProperty("user.dir") + File.separator + "ecp-agent" + File.separator + this.deploymentReference + File.separator + fileName);

    }


    @Override
    public void onNext(Frame item) {
        try {
            outputFile.write(item.getPayload());
            // TODO: do we need to update DB, etc?

        } catch (IOException e) {
            e.printStackTrace();
        }
        String stringFrame = new String(item.getPayload());
        log.append(stringFrame);
        super.onNext(item);
    }

    @Override
    public void onComplete() {
        // TODO: Check of exit status and do all the stuff we need to do?

        super.onComplete();
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public String toString() {
        return log.toString();
    }


}

