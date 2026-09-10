package com.marketlens.support;

import java.util.Map;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * Small helper for tests that need an authenticated session against the running
 * server.
 */
public class ApiClient {

    private final TestRestTemplate rest;
    private final int port;
    private String accessToken;

    public ApiClient(TestRestTemplate rest, int port) {
        this.rest = rest;
        this.port = port;
    }

    public String url(String path) {
        return "http://localhost:" + port + path;
    }

    @SuppressWarnings("unchecked")
    public ApiClient registerAndLogin(String username) {
        Map<String, Object> body = Map.of(
                "username", username,
                "displayName", username + " test",
                "password", "password123");
        ResponseEntity<Map> response = rest.postForEntity(url("/api/auth/register"), body, Map.class);
        this.accessToken = (String) response.getBody().get("accessToken");
        return this;
    }

    public void loginExisting(String username, String password) {
        @SuppressWarnings("unchecked")
        ResponseEntity<Map> response = rest.postForEntity(url("/api/auth/login"),
                Map.of("username", username, "password", password), Map.class);
        this.accessToken = (String) response.getBody().get("accessToken");
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (accessToken != null) {
            headers.setBearerAuth(accessToken);
        }
        return headers;
    }

    public <T> ResponseEntity<T> get(String path, Class<T> type) {
        return rest.exchange(url(path), HttpMethod.GET, new HttpEntity<>(authHeaders()), type);
    }

    public <T> ResponseEntity<T> post(String path, Object body, Class<T> type) {
        return rest.exchange(url(path), HttpMethod.POST, new HttpEntity<>(body, authHeaders()), type);
    }

    public <T> ResponseEntity<T> delete(String path, Class<T> type) {
        return rest.exchange(url(path), HttpMethod.DELETE, new HttpEntity<>(authHeaders()), type);
    }
}
