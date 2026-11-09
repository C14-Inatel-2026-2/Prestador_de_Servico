package br.inatel.engsoftware.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    private static String obterObrigatoria(String nome) {
        String valor = System.getenv(nome);

        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    "Variavel de ambiente nao configurada: " + nome
            );
        }

        return valor;
    }

    public static Connection conectar() throws SQLException {
        // Registra explicitamente o driver do PostgreSQL para o ambiente em nuvem
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver do PostgreSQL não encontrado. Verifique o pom.xml.", e);
        }

        String url = obterObrigatoria("DB_URL");
        String usuario = obterObrigatoria("DB_USER");
        String senha = obterObrigatoria("DB_PASSWORD");

        return DriverManager.getConnection(url, usuario, senha);
    }
}