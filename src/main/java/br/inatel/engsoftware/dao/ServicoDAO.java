
package br.inatel.engsoftware.dao;

import br.inatel.engsoftware.Servico;
import br.inatel.engsoftware.database.Conexao;
import br.inatel.engsoftware.database.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicoDAO {

    private final ConnectionFactory connectionFactory;

    public ServicoDAO() {
        this(Conexao::conectar);
    }

    public ServicoDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory =
                java.util.Objects.requireNonNull(connectionFactory);
    }

    // Cadastro de serviço
    public void inserir(Servico servico) throws SQLException {

        if (servico == null || servico.validar()) {
            throw new IllegalArgumentException("Serviço invalido");
        }

        String sql = """
            INSERT INTO servicos
            (nome, descricao, preco_sugerido)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, servico.getNome());
            stmt.setString(2, servico.getDescricao());
            stmt.setDouble(3, servico.getPrecoSugerido());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    servico.setId(rs.getInt(1));
                }
            }
        }
    }

    // Buscar serviço pelo ID
    public Servico buscarPorId(int id) throws SQLException {

        String sql = "SELECT * FROM servicos WHERE id = ?";

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarServico(rs);
                }
            }
        }

        return null;
    }

    // Listar serviços
    public List<Servico> listarTodos() throws SQLException {

        List<Servico> servicos = new ArrayList<>();

        String sql = "SELECT * FROM servicos ORDER BY id";

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                servicos.add(criarServico(rs));
            }
        }

        return servicos;
    }

    // Atualizar serviço
    public boolean atualizar(Servico servico) throws SQLException {

        if (servico == null || servico.validar()) {
            throw new IllegalArgumentException("Serviço invalido");
        }

        String sql = """
            UPDATE servicos
            SET nome = ?, descricao = ?, preco_sugerido = ?
            WHERE id = ?
            """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, servico.getNome());
            stmt.setString(2, servico.getDescricao());
            stmt.setDouble(3, servico.getPrecoSugerido());
            stmt.setInt(4, servico.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    // Excluir serviço
    public boolean excluir(int id) throws SQLException {

        String sql = "DELETE FROM servicos WHERE id = ?";

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;
        }
    }

    // Converter resultado SQL em objeto Java
    private Servico criarServico(ResultSet rs) throws SQLException {

        return new Servico(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("descricao"),
                rs.getDouble("preco_sugerido")
        );
    }
}
