package com.spe.calculator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculatorServerIntegrationTest {

    private static CalculatorServer server;
    private static HttpClient client;
    private static String baseUrl;

    @BeforeAll
    static void startServer() throws Exception {
        server = new CalculatorServer(0); // port 0 = random free port
        server.start();
        baseUrl = "http://localhost:" + server.getPort();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void addEndpointReturnsResult() throws Exception {
        HttpResponse<String> res = get("/api/v1/add?a=2&b=3");
        assertEquals(200, res.statusCode());
        assertEquals("{\"operation\":\"add\",\"a\":2,\"b\":3,\"result\":5}", res.body());
    }

    @Test
    void subtractEndpointReturnsResult() throws Exception {
        HttpResponse<String> res = get("/api/v1/subtract?a=10&b=4");
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"result\":6"));
    }

    @Test
    void multiplyEndpointReturnsResult() throws Exception {
        HttpResponse<String> res = get("/api/v1/multiply?a=3&b=4");
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"result\":12"));
    }

    @Test
    void divideEndpointReturnsResult() throws Exception {
        HttpResponse<String> res = get("/api/v1/divide?a=10&b=4");
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"result\":2.5"));
    }

    @Test
    void divideByZeroReturns400() throws Exception {
        HttpResponse<String> res = get("/api/v1/divide?a=10&b=0");
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("Division by zero"));
    }

    @Test
    void missingParameterReturns400() throws Exception {
        HttpResponse<String> res = get("/api/v1/add?a=1");
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("Missing parameter: b"));
    }

    @Test
    void invalidNumberReturns400() throws Exception {
        HttpResponse<String> res = get("/api/v1/add?a=abc&b=2");
        assertEquals(400, res.statusCode());
        assertTrue(res.body().contains("Invalid number"));
    }

    @Test
    void healthEndpointReturnsUp() throws Exception {
        HttpResponse<String> res = get("/health");
        assertEquals(200, res.statusCode());
        assertEquals("{\"status\":\"UP\"}", res.body());
    }
}
