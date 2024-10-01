package com.smart.services;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
	
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	@Value("$(spring.mail.username)")
	private String fromEmailId;

	@Value("$(spring.mail.password)")
	private String password;
	
	
	public boolean sendMail(String to, String subject, String message) {
		
		boolean isMailSent = false;
		
		String from = "nr1157161@gmail.com";
		
		String host = "smtp.gmail.com";
		
		Properties properties = System.getProperties();
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmailId, password);
			}
		});
		
		session.setDebug(true);
		
		try {
			MimeMessage msg = new MimeMessage(session);
			
			msg.setFrom(from);
			
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			msg.setSubject(subject);
			
			msg.setContent(message, "text/html");
			
			javaMailSender.send(msg);
			
			isMailSent = true;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return isMailSent;
	}

}
