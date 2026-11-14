package br.inatel.engsoftware;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {

        new Menu().executar();


        String portEnv = System.getenv("PORT");
        if (portEnv != null || System.console() == null) {
            int port = (portEnv != null && !portEnv.isBlank()) ? Integer.parseInt(portEnv.trim()) : 10000;
            manterAtivoNoRender(port);
        }
    }

    private static void manterAtivoNoRender(int port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            server.createContext("/", exchange -> {
                byte[] response = "Prestador de Servico - Aplicacao Ativa no Render!".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            });
            server.start();
            System.out.println("Servidor ativo na porta " + port + " para o Render.");

            // Mantém a thread principal viva indefinidamente
            Thread.currentThread().join();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Erro ao manter servico ativo: " + e.getMessage());
        }
    }
}