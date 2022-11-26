package com.estonianport.agendaza;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@SpringBootApplication
@EnableEncryptableProperties
public class AgendazaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgendazaApplication.class, args);
	}

}
