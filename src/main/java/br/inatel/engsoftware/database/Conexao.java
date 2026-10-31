
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

        String url = obterObrigatoria("DB_URL");
        String usuario = obterObrigatoria("DB_USER");
        String senha = obterObrigatoria("DB_PASSWORD");

        return DriverManager.getConnection(
                url, usuario, senha
        );
    }
}
