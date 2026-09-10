package com.marketlens.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.marketlens.support.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class AuthFlowTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void registerThenCallMe() {
        Map<String, String> register = Map.of(
                "username", "alice",
                "displayName", "Alice Stone",
                "password", "password123");

        ResponseEntity<Map> registered = rest.postForEntity(url("/api/auth/register"), register, Map.class);
        assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String accessToken = (String) registered.getBody().get("accessToken");
        assertThat(accessToken).isNotBlank();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<Map> me = rest.exchange(url("/api/auth/me"), HttpMethod.GET,
                new HttpEntity<>(headers), Map.class);
        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(me.getBody().get("username")).isEqualTo("alice");
    }

    @Test
    void meWithoutTokenIsUnauthorized() {
        ResponseEntity<Map> me = rest.getForEntity(url("/api/auth/me"), Map.class);
        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void loginWithWrongPasswordIsRejected() {
        rest.postForEntity(url("/api/auth/register"), Map.of(
                "username", "bob",
                "displayName", "Bob Lin",
                "password", "password123"), Map.class);

        ResponseEntity<Map> login = rest.postForEntity(url("/api/auth/login"), Map.of(
                "username", "bob",
                "password", "wrongpass"), Map.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
