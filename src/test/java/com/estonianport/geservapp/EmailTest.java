package com.estonianport.geservapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.estonianport.geservapp.commons.emailService.Email;
import com.estonianport.geservapp.commons.emailService.EmailService;

@SpringBootTest
class EmailTest {

	@Autowired
	EmailService emailService;

	@Test
	void testEmail() {
		Email emailBody = new Email();
		emailBody.setContent("Cuerpo del Email");
		emailBody.setSubject("Mail prueba GESERVAPP");
		emailBody.setEmail("rdzsebastian@gmail.com");

//		emailService.sendEmail(emailBody);
	}

}
