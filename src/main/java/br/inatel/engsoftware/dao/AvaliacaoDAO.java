package br.inatel.engsoftware.dao;

import br.inatel.engsoftware.Avaliacao;
import br.inatel.engsoftware.database.Conexao;
import br.inatel.engsoftware.database.ConnectionFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AvaliacaoDAO {

    private final ConnectionFactory connectionFactory;

    // Construtor padrão
    public AvaliacaoDAO() {
        this(Conexao::conectar);
    }

    // Construtor para facilitar os testes com Mockito
    public AvaliacaoDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory =
                Objects.requireNonNull(connectionFactory);
    }

    // 1. INSERIR AVALIAÇÃO
    // Salva a data informada na avaliação. Retorna o ID gerado pelo MySQL.
    public int inserir(Avaliacao avaliacao) throws SQLException {

        if (avaliacao == null) {
            throw new IllegalArgumentException(
                    "Avaliação não pode ser nula.");
        }

        if (avaliacao.getIdAutor() <= 0 ||
                avaliacao.getIdAvaliado() <= 0) {
            throw new IllegalArgumentException(
                    "IDs dos usuários são inválidos.");
        }

        if (avaliacao.getIdAutor() ==
                avaliacao.getIdAvaliado()) {
            throw new IllegalArgumentException(
                    "Um usuário não pode avaliar a si mesmo.");
        }

        if (avaliacao.getNota() < 1 ||
                avaliacao.getNota() > 5) {
            throw new IllegalArgumentException(
                    "A nota deve estar entre 1 e 5.");
        }

        if (avaliacao.getData() == null) {
            throw new IllegalArgumentException(
                    "A data da avaliação é obrigatória.");
        }

        if (avaliacao.getId() > 0) {
            throw new IllegalArgumentException(
                    "Esta avaliação já possui ID.");
        }

        String sql = """
            INSERT INTO avaliacoes
            (id_avaliador, id_avaliado, nota, comentario, data_avaliacao)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(
                     sql, new String[]{"id_avaliacao"})) {

            stmt.setInt(1, avaliacao.getIdAutor());
            stmt.setInt(2, avaliacao.getIdAvaliado());
            stmt.setInt(3, avaliacao.getNota());
            stmt.setString(4, avaliacao.getComentario());
            stmt.setObject(5, avaliacao.getData()); // LocalDate, sem hora

            int linhas = stmt.executeUpdate();

            if (linhas != 1) {
                throw new SQLException(
                        "Não foi possível cadastrar a avaliação.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            throw new SQLException(
                    "Não foi possível recuperar o ID da avaliação.");
        }
    }

    // 2. BUSCAR POR ID
    public Avaliacao buscarPorId(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        String sql = """
            SELECT * FROM avaliacoes
            WHERE id_avaliacao = ?
            """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarAvaliacao(rs);
                }
            }
        }

        return null;
    }

    // 3. LISTAR AVALIAÇÕES FEITAS POR UM USUÁRIO
    public List<Avaliacao> listarPorAvaliador(int idAvaliador)
            throws SQLException {

        validarId(idAvaliador);

        List<Avaliacao> avaliacoes = new ArrayList<>();

        String sql = """
            SELECT * FROM avaliacoes
            WHERE id_avaliador = ?
            ORDER BY data_avaliacao DESC, id_avaliacao DESC
            """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idAvaliador);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    avaliacoes.add(criarAvaliacao(rs));
                }
            }
        }

        return avaliacoes;
    }

    // 4. LISTAR AVALIAÇÕES RECEBIDAS
    public List<Avaliacao> listarPorAvaliado(int idAvaliado)
            throws SQLException {

        validarId(idAvaliado);

        List<Avaliacao> avaliacoes = new ArrayList<>();

        String sql = """
            SELECT * FROM avaliacoes
            WHERE id_avaliado = ?
            ORDER BY data_avaliacao DESC, id_avaliacao DESC
            """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idAvaliado);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    avaliacoes.add(criarAvaliacao(rs));
                }
            }
        }

        return avaliacoes;
    }

    // 5. CALCULAR MÉDIA DAS AVALIAÇÕES RECEBIDAS
    public double calcularMedia(int idAvaliado)
            throws SQLException {

        validarId(idAvaliado);

        String sql = """
            SELECT AVG(nota) AS media
            FROM avaliacoes
            WHERE id_avaliado = ?
            """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idAvaliado);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double media = rs.getDouble("media");

                    if (!rs.wasNull()) {
                        return media;
                    }
                }
            }
        }

        return 0.0;
    }

    // 6. LISTAR TODAS AS AVALIAÇÕES
    public List<Avaliacao> listarTodas() throws SQLException {

        List<Avaliacao> avaliacoes = new ArrayList<>();

        String sql = """
            SELECT * FROM avaliacoes
            ORDER BY id_avaliacao
            """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                avaliacoes.add(criarAvaliacao(rs));
            }
        }

        return avaliacoes;
    }

    // CONVERTER RESULTADO SQL EM OBJETO JAVA
    private Avaliacao criarAvaliacao(ResultSet rs)
            throws SQLException {

        LocalDate data = rs.getObject("data_avaliacao", LocalDate.class);

        if (data == null) {
            throw new SQLException(
                    "Avaliação sem data cadastrada.");
        }

        try {
            return new Avaliacao(
                    rs.getInt("id_avaliacao"),
                    rs.getInt("id_avaliador"),
                    rs.getInt("id_avaliado"),
                    rs.getInt("nota"),
                    rs.getString("comentario"),
                    data
            );
        } catch (IllegalArgumentException e) {
            throw new SQLException(
                    "Avaliação inválida no banco: " + e.getMessage(), e);
        }
    }

    private void validarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "O ID deve ser positivo.");
        }
    }
}