package br.inatel.engsoftware.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionFactory {
    Connection abrir() throws SQLException;
}
