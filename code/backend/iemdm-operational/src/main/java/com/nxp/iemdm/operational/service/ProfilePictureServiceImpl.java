package com.nxp.iemdm.operational.service;

import com.nxp.iemdm.shared.intf.operational.ProfilePictureService;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Log
@Service
public class ProfilePictureServiceImpl implements ProfilePictureService {
  private final Pattern TOKEN_PATTERN =
      Pattern.compile(
          "\\{\"token_type\":\"(.+)\",\"expires_in\":(\\d+),\"ext_expires_in\":(\\d+),\"access_token\":\"(.+)\"}");

  private final String clientId;
  private final String clientSecret;
  private final RestTemplate restTemplate;

  @Autowired
  public ProfilePictureServiceImpl(
      @Value("${profilepicture.clientid}") String clientId,
      @Value("${profilepicture.clientsecret}") String clientSecret,
      RestTemplate restTemplate) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.restTemplate = restTemplate;
  }

  public byte[] getProfilePictureForUser(String email) {
    String body =
        String.format(
            "client_id = %s&client_secret = %s&scope = https://graph.microsoft.com/.default&grant_type = client_credentials",
            clientId, clientSecret);

    HttpHeaders tokenHeaders = new HttpHeaders();
    tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    HttpEntity<String> tokenEntity = new HttpEntity<>(body, tokenHeaders);

    ResponseEntity<String> stringResponseEntity =
        restTemplate.postForEntity(
            "https://login.microsoftonline.com/nxp1.onmicrosoft.com/oauth2/v2.0/token",
            tokenEntity,
            String.class);

    Matcher tokenMatcher = TOKEN_PATTERN.matcher(stringResponseEntity.getBody());

    if (tokenMatcher.matches()) {
      String bearer = tokenMatcher.group(4);

      HttpHeaders imageHeaders = new HttpHeaders();
      imageHeaders.setBearerAuth(bearer);

      HttpEntity<String> imageEntity = new HttpEntity<>(imageHeaders);
      try {
        ResponseEntity<byte[]> responseEntity =
            restTemplate.exchange(
                String.format("https://graph.microsoft.com/v1.0/users/%s/photo/$value", email),
                HttpMethod.GET,
                imageEntity,
                byte[].class);
        return responseEntity.getBody();
      } catch (RestClientException rce) {
        log.log(Level.WARNING, "Failed to get profile image from Microsoft Graph.");
      }
    }

    return null;
  }
}
