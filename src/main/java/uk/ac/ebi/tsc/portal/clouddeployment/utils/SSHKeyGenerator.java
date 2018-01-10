package uk.ac.ebi.tsc.portal.clouddeployment.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jose A. Dianes <jdianes@ebi.ac.uk>
 * @since v0.0.1
 * @author Navis Raj <navis@ebi.ac.uk>
 */
public class SSHKeyGenerator {

	private static final Logger logger = LoggerFactory.getLogger(SSHKeyGenerator.class);

	public static void generateKeys(String userEmail, String filePath) {

		if(!keysExist(filePath)){
			
			userEmail = "'" + userEmail + "'";
			filePath = "'" + filePath + "'";
			
			ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c",
					"ssh-keygen -t rsa -b 4096  -C " + userEmail + " -f " + filePath + " -P " + "''" );

			Process process;
			try {
				process = processBuilder.start();
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = null;
				while ((line = reader.readLine()) != null) {
					logger.info(line);
				}
			} catch (IOException e) {
				logger.error("Failed to write keys to " + filePath);
			}
		}
		
	}
	
	//check keys exist  
	private static boolean keysExist(String filePath){
		File privateKeyFile = new File(filePath);
		File publicKeyFile = new File(filePath + "_pub");
		if(privateKeyFile.exists() && publicKeyFile.exists()){
			return true;
		}
		
		//if any one of the keys not present delete both 
		if(privateKeyFile.exists() && !publicKeyFile.exists()||
		   !privateKeyFile.exists() && publicKeyFile.exists()){
			
			privateKeyFile.delete();
			publicKeyFile.delete();
		}
		return false;
	}
}
