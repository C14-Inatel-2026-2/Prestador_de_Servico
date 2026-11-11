
package br.inatel.engsoftware.dao;

import br.inatel.engsoftware.Servico;
import br.inatel.engsoftware.database.Conexao;
import br.inatel.engsoftware.database.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import br.inatel.engsoftware.Prestador;
import br.inatel.engsoftware.Usuario;

public class ServicoDAO {

    private final ConnectionFactory connectionFactory;

    public record ServicoPrestado(Servico servico, double precoCobrado) { }

    public ServicoDAO() {
        this(Conexao::conectar);
    }
    public ServicoDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory =
                java.util.Objects.requireNonNull(connectionFactory);
    }

    // Cadastro de serviço
    public void inserir(Servico servico) throws SQLException {

        if (servico == null || !servico.validar()) {
            throw new IllegalArgumentException("Serviço invalido");
        }

        String sql = """
            INSERT INTO servicos
            (nome, descricao, preco_sugerido)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(
                     sql, new String[]{"id"})) {

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
    public List<Servico> buscarServicoPorNome(String nome)
            throws SQLException {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe o nome do serviço.");
        }

        List<Servico> servicos = new ArrayList<>();

        String sql = """
        SELECT id, nome, descricao, preco_sugerido
        FROM servicos
        WHERE nome ILIKE ?
        ORDER BY nome
        """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nome.trim() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    servicos.add(criarServico(rs));
                }
            }
        }

        return servicos;
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

        if (servico == null || !servico.validar()) {
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

    // Associar prestador a um serviço
    public void associarPrestador(int idPrestador, int idServico,
                                  double precoCobrado) throws SQLException {

        if (idPrestador <= 0 || idServico <= 0 ||
                !Double.isFinite(precoCobrado) || precoCobrado < 0) {
            throw new IllegalArgumentException("Dados inválidos.");
        }

        String sql = """
            INSERT INTO prestador_servicos
            (id_prestador, id_servico, preco_cobrado)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPrestador);
            stmt.setInt(2, idServico);
            stmt.setDouble(3, precoCobrado);

            stmt.executeUpdate();
        }
    }

    //Buscar prestador por serviço
    public List<Prestador> buscarPrestadoresPorServico(int idServico)
            throws SQLException {

        if (idServico <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        List<Prestador> prestadores = new ArrayList<>();
        UsuarioDAO usuarioDAO = new UsuarioDAO(connectionFactory);

        String sql = """
        SELECT id_prestador
        FROM prestador_servicos
        WHERE id_servico = ?
        ORDER BY id_prestador
        """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idServico);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    int idPrestador = rs.getInt("id_prestador");

                    Usuario usuario = usuarioDAO.buscarPorId(idPrestador);

                    if (usuario instanceof Prestador prestador) {
                        prestadores.add(prestador);
                    }
                }
            }
        }

        return prestadores;
    }


    // Buscar prestadores por nome, profissão, cidade ou serviço
    public List<Prestador> buscarPrestadores(String termo)
            throws SQLException {

        if (termo == null || termo.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe um termo para pesquisa.");
        }

        List<Integer> ids = new ArrayList<>();
        List<Prestador> prestadores = new ArrayList<>();

        String sql = """
        SELECT p.id_usuario
        FROM prestadores p
        INNER JOIN usuarios u
            ON u.id_usuario = p.id_usuario
        WHERE LOWER(u.nome) LIKE ?
           OR LOWER(p.profissao) LIKE ?
           OR LOWER(u.cidade) LIKE ?
           OR EXISTS (
               SELECT 1
               FROM prestador_servicos ps
               INNER JOIN servicos s
                   ON s.id = ps.id_servico
               WHERE ps.id_prestador = p.id_usuario
                 AND LOWER(s.nome) LIKE ?
           )
        ORDER BY u.nome
        """;

        String pesquisa = "%" + termo.trim().toLowerCase(
                java.util.Locale.ROOT) + "%";

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 4; i++) {
                stmt.setString(i, pesquisa);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id_usuario"));
                }
            }
        }

        UsuarioDAO usuarioDAO = new UsuarioDAO(connectionFactory);

        for (int id : ids) {
            Usuario usuario = usuarioDAO.buscarPorId(id);

            if (usuario instanceof Prestador prestador) {
                prestadores.add(prestador);
            }
        }

        return prestadores;
    }

    // Listar serviços oferecidos por um prestador
    public List<ServicoPrestado> listarServicosPorPrestador(int idPrestador)
            throws SQLException {

        if (idPrestador <= 0) {
            throw new IllegalArgumentException("ID do prestador inválido.");
        }

        List<ServicoPrestado> servicos = new ArrayList<>();

        String sql = """
        SELECT s.id, s.nome, s.descricao, s.preco_sugerido, ps.preco_cobrado
        FROM servicos s
        INNER JOIN prestador_servicos ps
            ON s.id = ps.id_servico
        WHERE ps.id_prestador = ?
        ORDER BY s.nome
        """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPrestador);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    servicos.add(new ServicoPrestado(
                            criarServico(rs),
                            rs.getDouble("preco_cobrado")));
                }
            }
        }

        return servicos;
    }



    // Desassociar um prestador de um serviço
    public boolean desassociarPrestador(int idPrestador, int idServico)
            throws SQLException {

        if (idPrestador <= 0 || idServico <= 0) {
            throw new IllegalArgumentException("IDs inválidos.");
        }

        String sql = """
        DELETE FROM prestador_servicos
        WHERE id_prestador = ? AND id_servico = ?
        """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPrestador);
            stmt.setInt(2, idServico);

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
