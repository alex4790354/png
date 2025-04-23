package com.pokemons.png;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pokemons.png.service.MainLogic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class PngApplication {

	public static void main(String[] args) throws JsonProcessingException {
		//SpringApplication.run(PngApplication.class, args);
		ApplicationContext context = SpringApplication.run(PngApplication.class, args);
		MainLogic mainService = context.getBean(MainLogic.class);
		mainService.justRun();
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
