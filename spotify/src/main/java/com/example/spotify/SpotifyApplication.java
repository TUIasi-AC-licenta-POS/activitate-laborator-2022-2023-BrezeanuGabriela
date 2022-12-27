package com.example.spotify;

import com.example.consumingwebservice.wsdl.AuthorizeResponse;
import com.example.spotify.Exceptions.JWTExceptions.CorruptedJwt;
import com.example.spotify.JWT.JwtTokenUtil;
import com.example.spotify.SoapClient.ClientForSoap;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class SpotifyApplication {
	@Autowired
	public JwtTokenUtil jwtTokenUtil;

	public static void main(String[] args) {
		SpringApplication.run(SpotifyApplication.class, args);
	}

//	@Bean
//	CommandLineRunner lookup(ClientForSoap clientForSoap) {
//		return args -> {
//		};
//	}
}
