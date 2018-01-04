package uk.ac.ebi.tsc.portal.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

public class SendMail {
	
	private static final Logger logger = LoggerFactory.getLogger(SendMail.class);

	public static void send(Collection<String> toNotify, String subject, String  body) throws IOException{

		InputStream input = SendMail.class.getClassLoader().getResourceAsStream("application.properties");
		
		Properties props = System.getProperties();
		props.load(input);
		String username = props.getProperty("sftp.mail.username");
		String password = props.getProperty("sftp.mail.password");
		String from = props.getProperty("sftp.mail.from");
		String host = props.getProperty("smtp.mail.host");
		String port = props.getProperty("smtp.mail.port");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		
		Session session = Session.getInstance(props,
				  new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				  });

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			Address[] addresses = new Address[toNotify.size()];
			int i = 0;
			for (String address : toNotify) {
				Address mailAddress = new InternetAddress(address);
				addresses[i] = mailAddress;
				i++;
			}
			message.addRecipients(Message.RecipientType.TO, addresses);
			message.setSubject(subject);
			String mailBody = "Hi, \n\n" + body + "\n\n" + "Thanks, \n" + "The CloudPortal Team.";
			message.setText(mailBody);
			Transport.send(message);
			logger.info("Sent message successfully....");
			
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}

}
