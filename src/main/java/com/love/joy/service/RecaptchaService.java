package com.love.joy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Tiny helper that talks to Google reCAPTCHA's /siteverify endpoint.
 * make sure the user is not a bot before I even look up the account in the database
 */
@Service
public class RecaptchaService
{

    //secret key from application.properties
    @Value("${recaptcha.secret}")
    private String secret;

    //official google verify endpoint
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    //HTTP client Spring gives me for making the request to google
    private final RestTemplate restTemplate = new RestTemplate();


    //sends users recaptcha token to google and checks if its vali
    // token from the login form (g-recaptcha-response)
    //outIP the IP of the client

    public boolean verify(String token, String outIP)
    {
        //if token is empty then captcha obviously failed
        if (token == null || token.isBlank())
        {
            return false;
        }

        //values sent to google
        Map<String, String> body = new HashMap<>();
        body.put("secret", secret);
        body.put("response", token);
        body.put("outIP", outIP);

        //make it query string
        String params =
                "secret={secret}&response={response}&outIP={outIP}";

        //send POST request to google anfd google replies with RecaptchaAnswer JSON
        RecaptchaAnswer resp = restTemplate.postForObject(
                VERIFY_URL + "?" + params,
                null,
                RecaptchaAnswer.class,
                body
        );

        // success = true means the captcha is valid
        return resp != null && resp.isSuccess();
    }
}