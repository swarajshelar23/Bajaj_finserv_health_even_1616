package startup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class StartupRunner implements CommandLineRunner {

    @Value("${startup.enabled:true}")
    private boolean enabled;

    @Value("${startup.name:John Doe}")
    private String name;

    @Value("${startup.regNo:REG12347}")
    private String regNoProp;

    @Value("${startup.email:john@example.com}")
    private String email;

    @Value("${startup.generateWebhookUrl:https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA}")
    private String generateWebhookUrl;

    @Value("${startup.maxRetries:3}")
    private int maxRetries;

    @Value("${startup.retryInitialDelayMs:1000}")
    private int retryInitialDelayMs;

    @Override
    public void run(String args[]) {
        if (!enabled) {
            System.out.println("Startup webhook flow is disabled (startup.enabled=false). Skipping.");
            return;
        }

        System.out.println("Starting startup webhook flow...");

        // Configure RestTemplate with timeouts
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(10000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        Map<String, String> request = new HashMap<>();
        request.put("name", name);
        request.put("regNo", regNoProp);
        request.put("email", email);

        // Call generateWebhook with retries
        Map responseBody = null;
        try {
            responseBody = performPostWithRetries(restTemplate, generateWebhookUrl, request, Map.class, maxRetries, retryInitialDelayMs);
        } catch (Exception ex) {
            System.err.println("generateWebhook failed after retries: " + ex.getMessage());
            return;
        }

        if (responseBody == null) {
            System.err.println("generateWebhook returned null body");
            return;
        }

        Object webhookObj = responseBody.get("webhook");
        Object accessTokenObj = responseBody.get("accessToken");

        if (webhookObj == null || accessTokenObj == null) {
            System.err.println("Missing webhook or accessToken in response: " + responseBody);
            return;
        }

        String webhookUrl = webhookObj.toString();
        String accessToken = accessTokenObj.toString();

        System.out.println("Webhook URL: " + webhookUrl);
        System.out.println("Access Token: " + (accessToken.length() > 10 ? accessToken.substring(0, 10) + "..." : accessToken));

        int lastTwo = extractLastTwoDigits(regNoProp);
        boolean isEven = (lastTwo % 2 == 0);

        String finalQuery;
        if (!isEven) {
            finalQuery = "-- QUESTION 1: Placeholder solution. Replace with real final SQL query for Question 1.\nSELECT * FROM employees WHERE 1=1;";
        } else {
            finalQuery = "-- QUESTION 2: Placeholder solution. Replace with real final SQL query for Question 2.\nSELECT * FROM orders WHERE 1=1;";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        Map<String, String> payload = new HashMap<>();
        payload.put("finalQuery", finalQuery);

        try {
            performPostWithRetries(restTemplate, webhookUrl, payload, String.class, maxRetries, retryInitialDelayMs, headers);
            System.out.println("Submitted Successfully");
        } catch (Exception ex) {
            System.err.println("Failed to submit final query after retries: " + ex.getMessage());
        }
    }

    // Generic helper to perform POST with retries (no custom headers)
    private <T> T performPostWithRetries(RestTemplate restTemplate, String url, Object body, Class<T> responseType, int maxRetries, int initialDelayMs) throws Exception {
        return performPostWithRetries(restTemplate, url, body, responseType, maxRetries, initialDelayMs, null);
    }

    // Generic helper to perform POST with retries and optional headers
    private <T> T performPostWithRetries(RestTemplate restTemplate, String url, Object body, Class<T> responseType, int maxRetries, int initialDelayMs, HttpHeaders headers) throws Exception {
        int attempt = 0;
        Exception lastEx = null;
        while (attempt < maxRetries) {
            try {
                HttpEntity<Object> entity = (headers == null) ? new HttpEntity<>(body) : new HttpEntity<>(body, headers);
                ResponseEntity<T> resp = restTemplate.postForEntity(url, entity, responseType);
                if (resp != null && resp.getStatusCode().is2xxSuccessful()) {
                    return resp.getBody();
                } else {
                    throw new RuntimeException("Non-2xx response: " + (resp == null ? "null" : resp.getStatusCode()));
                }
            } catch (Exception ex) {
                lastEx = ex;
                attempt++;
                int delay = initialDelayMs * (int) Math.pow(2, attempt - 1);
                System.err.println("Attempt " + attempt + " failed for URL " + url + ": " + ex.getMessage() + ". Retrying in " + delay + "ms");
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ie;
                }
            }
        }
        throw lastEx != null ? lastEx : new RuntimeException("Failed to POST to " + url);
    }

    // Helper extracts last two digits from a regNo like "REG12347" -> 47
    private int extractLastTwoDigits(String regNo) {
        if (regNo == null || regNo.isEmpty()) return 0;
        StringBuilder digits = new StringBuilder();
        for (int i = regNo.length() - 1; i >= 0 && digits.length() < 2; i--) {
            char c = regNo.charAt(i);
            if (Character.isDigit(c)) digits.append(c);
        }
        if (digits.length() == 0) return 0;
        String rev = digits.reverse().toString();
        try {
            return Integer.parseInt(rev);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}