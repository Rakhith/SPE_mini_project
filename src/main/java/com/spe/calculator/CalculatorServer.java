package com.spe.calculator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CalculatorServer {

    private final HttpServer server;
    private final Calculator calculator = new Calculator();

    public CalculatorServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        for (String op : new String[]{"add", "subtract", "multiply", "divide"}) {
            server.createContext("/api/v1/" + op, ex -> handleOperation(ex, op));
        }
        server.createContext("/health", this::handleHealth);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    private void handleHealth(HttpExchange ex) throws IOException {
        send(ex, 200, "{\"status\":\"UP\"}");
    }

    private void handleOperation(HttpExchange ex, String op) throws IOException {
        if (!"GET".equals(ex.getRequestMethod())) {
            send(ex, 405, error("Method not allowed", 405));
            return;
        }
        try {
            Map<String, String> query = parseQuery(ex.getRequestURI().getRawQuery());
            double a = parseParam(query, "a");
            double b = parseParam(query, "b");
            double result = switch (op) {
                case "add" -> calculator.add(a, b);
                case "subtract" -> calculator.subtract(a, b);
                case "multiply" -> calculator.multiply(a, b);
                case "divide" -> calculator.divide(a, b);
                default -> throw new IllegalArgumentException("Unknown operation");
            };
            send(ex, 200, "{\"operation\":\"" + op + "\",\"a\":" + fmt(a)
                    + ",\"b\":" + fmt(b) + ",\"result\":" + fmt(result) + "}");
        } catch (IllegalArgumentException | ArithmeticException e) {
            send(ex, 400, error(e.getMessage(), 400));
        }
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return map;
        }
        for (String pair : rawQuery.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(key, value);
        }
        return map;
    }

    private static double parseParam(Map<String, String> query, String name) {
        String raw = query.get(name);
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Missing parameter: " + name);
        }
        try {
            double value = Double.parseDouble(raw.trim());
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                throw new IllegalArgumentException("Invalid number for parameter: " + name);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number for parameter: " + name);
        }
    }

    private static String fmt(double d) {
        if (d == Math.rint(d) && Math.abs(d) < 1e15) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }

    private static String error(String message, int status) {
        return "{\"error\":\"" + message + "\",\"status\":" + status + "}";
    }

    private static void send(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
