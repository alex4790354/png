package com.pokemons.png.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemons.png.response.ResponseMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;


@Service
public class SendRequest {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SendRequest(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public ResponseMessage makeRequest(String jsonString, String url, HttpMethod method) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("trainer_token", "84fb0a551fe90dbff1c851517db9fa6c");

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonString, headers);
        ResponseMessage message = new ResponseMessage();
        message.setMessage("Start");

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, method, requestEntity, String.class);
            message = objectMapper.readValue(response.getBody(), ResponseMessage.class);
            message.setStatusCode(response.getStatusCode().toString());
        } catch (HttpClientErrorException e) {
            System.out.println("HTTP error occurred: " + e.getStatusCode());
            System.out.println("Error response: " + e.getResponseBodyAsString());
            message.setMessage("HttpClientErrorException. " + e);
        } catch (JsonMappingException e) {
            message.setMessage("JsonMappingException. " + e);
        } catch (JsonProcessingException e) {
            message.setMessage("JsonProcessingException. " + e);
        }
        return message;
    }

}
