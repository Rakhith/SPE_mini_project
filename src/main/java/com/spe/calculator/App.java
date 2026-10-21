package com.spe.calculator;

public class App {
    public static void main(String[] args) throws Exception {
        String portEnv = System.getenv("PORT");
        int port = (portEnv == null || portEnv.isBlank()) ? 8080 : Integer.parseInt(portEnv);
        CalculatorServer server = new CalculatorServer(port);
        server.start();
        System.out.println("Calculator API running on port " + port);
    }
}
