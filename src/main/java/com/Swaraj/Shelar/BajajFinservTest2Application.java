package com.Swaraj.Shelar;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class BajajFinservTest2Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(BajajFinservTest2Application.class, args);
    }

    @Override
    public void run(String... args) {

        // STEP 1: Create RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // STEP 2: Call Generate Webhook API
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> request = new HashMap<>();
        request.put("name", "Swaraj Shelar");
        request.put("regNo", "1616");
        request.put("email", "swarajrs2305@gmail.com");

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        String webhook = (String) response.getBody().get("webhook");
        String token = (String) response.getBody().get("accessToken");

        System.out.println("Webhook: " + webhook);
        System.out.println("Token: " + token);

        // STEP 3: Add your SQL query here
        String finalQuery = "SELECT \n" +
                "    e.EMP_ID, \n" +
                "    e.FIRST_NAME, \n" +
                "    e.LAST_NAME, \n" +
                "    d.DEPARTMENT_NAME,\n" +
                "    (\n" +
                "        SELECT COUNT(*) \n" +
                "        FROM EMPLOYEE e2 \n" +
                "        WHERE e2.DEPARTMENT = e.DEPARTMENT \n" +
                "        AND e2.DOB > e.DOB\n" +
                "    ) AS YOUNGER_EMPLOYEES_COUNT\n" +
                "FROM EMPLOYEE e\n" +
                "JOIN DEPARTMENT d \n" +
                "    ON e.DEPARTMENT = d.DEPARTMENT_ID\n" +
                "ORDER BY e.EMP_ID DESC;";
        // STEP 4: Send Answer
        sendAnswer(webhook, token, finalQuery);
    }

    private void sendAnswer(String webhook, String token, String query) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("finalQuery", query);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(webhook, request, String.class);

        System.out.println("Response: " + response.getBody());
    }
}