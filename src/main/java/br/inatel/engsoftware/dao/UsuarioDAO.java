package br.inatel.engsoftware.dao;

import br.inatel.engsoftware.Usuario;
import br.inatel.engsoftware.Prestador;
import br.inatel.engsoftware.Contratante;
import br.inatel.engsoftware.BCryptService;
import br.inatel.engsoftware.CriptografiaService;
import br.inatel.engsoftware.database.Conexao;
import br.inatel.engsoftware.database.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UsuarioDAO {

    private final ConnectionFactory connectionFactory;
    private final CriptografiaService criptografiaService;

    // Construtor padrão
    public UsuarioDAO() {
        this(Conexao::conectar, new BCryptService());
    }

    // Construtor para facilitar os testes
    public UsuarioDAO(ConnectionFactory connectionFactory) {
        this(connectionFactory, new BCryptService());
    }

    public UsuarioDAO(ConnectionFactory connectionFactory,
                      CriptografiaService criptografiaService) {
        this.connectionFactory =
                Objects.requireNonNull(connectionFactory);
        this.criptografiaService =
                Objects.requireNonNull(criptografiaService);
    }

    // 1. CADASTRAR USUÁRIO
    // Salva os dados comuns e o perfil na mesma transação.
    public void inserir(Usuario usuario) throws SQLException {

        validarUsuario(usuario);

        if (usuario.getId() > 0) {
            throw new IllegalArgumentException(
                    "O usuário já possui um ID. Use atualizar().");
        }

        String sqlUsuario = """
            INSERT INTO usuarios
            (nome, email, senha_hash, telefone, tipo)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = connectionFactory.abrir()) {

            boolean autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                int idGerado;

                try (PreparedStatement stmt = conn.prepareStatement(
                        sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {

                    stmt.setString(1, usuario.getNome());
                    stmt.setString(2, usuario.getEmail());
                    stmt.setString(3, usuario.getSenhaHash());
                    stmt.setString(4, usuario.getTelefone());
                    stmt.setString(5, obterTipo(usuario));

                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "Não foi possível obter o ID do usuário.");
                        }
                        idGerado = rs.getInt(1);
                    }
                }

                inserirPerfil(conn, idGerado, usuario);

                conn.commit();
                usuario.definirIdPersistido(idGerado);

            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackErro) {
                    e.addSuppressed(rollbackErro);
                }
                throw e;

            } finally {
                conn.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    // Insere os dados específicos do perfil.
    private void inserirPerfil(Connection conn, int id,
                               Usuario usuario) throws SQLException {

        if (usuario instanceof Prestador prestador) {

            String sql = """
                INSERT INTO prestadores
                (id_usuario, profissao, descricao, cidade, valor_hora)
                VALUES (?, ?, ?, ?, ?)
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.setString(2, prestador.getProfissao());
                stmt.setString(3, prestador.getDescricao());
                stmt.setString(4, prestador.getCidade());
                stmt.setDouble(5, prestador.getValorHora());

                stmt.executeUpdate();
            }

        } else if (usuario instanceof Contratante contratante) {

            String sql = """
                INSERT INTO contratantes (id_usuario, cidade)
                VALUES (?, ?)
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.setString(2, contratante.getCidade());
                stmt.executeUpdate();
            }
        }
    }

    // 2. BUSCAR USUÁRIO POR ID
    public Usuario buscarPorId(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        String sql = consultaUsuarios() + """
            WHERE u.id_usuario = ?
            """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarUsuario(rs);
                }
            }
        }

        return null;
    }

    // 3. BUSCAR USUÁRIO POR E-MAIL
    public Usuario buscarPorEmail(String email) throws SQLException {

        validarTexto(email, "E-mail", 150);

        String sql = consultaUsuarios() + """
            WHERE u.email = ?
            """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarUsuario(rs);
                }
            }
        }

        return null;
    }

    // 4. BUSCAR USUÁRIOS POR NOME
    // Permite pesquisa parcial.
    public List<Usuario> buscarPorNome(String nome) throws SQLException {

        validarTexto(nome, "Nome", 150);

        List<Usuario> usuarios = new ArrayList<>();

        String sql = consultaUsuarios() + """
            WHERE u.nome LIKE ? ESCAPE '!'
            ORDER BY u.nome, u.id_usuario
            """;

        // Evita que %, _ e ! fornecidos pelo usuário
        // sejam interpretados como operadores do LIKE.
        String nomeEscapado = nome
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nomeEscapado + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(criarUsuario(rs));
                }
            }
        }

        return usuarios;
    }

    // 5. ATUALIZAR USUÁRIO
    // Atualiza os dados comuns e específicos do mesmo perfil.
    public boolean atualizar(Usuario usuario) throws SQLException {

        validarUsuario(usuario);

        if (usuario.getId() <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        String sqlUsuario = """
            UPDATE usuarios
            SET nome = ?, email = ?, senha_hash = ?, telefone = ?
            WHERE id_usuario = ? AND tipo = ?
            """;

        try (Connection conn = connectionFactory.abrir()) {

            boolean autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                // Confere se o usuário existe e mantém o tipo.
                String sqlTipo = """
                    SELECT tipo FROM usuarios
                    WHERE id_usuario = ? FOR UPDATE
                    """;

                try (PreparedStatement stmt =
                             conn.prepareStatement(sqlTipo)) {

                    stmt.setInt(1, usuario.getId());

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }

                        String tipoExistente = rs.getString("tipo");

                        if (!tipoExistente.equals(obterTipo(usuario))) {
                            throw new IllegalArgumentException(
                                    "Não é permitido alterar o tipo do usuário.");
                        }
                    }
                }

                try (PreparedStatement stmt =
                             conn.prepareStatement(sqlUsuario)) {

                    stmt.setString(1, usuario.getNome());
                    stmt.setString(2, usuario.getEmail());
                    stmt.setString(3, usuario.getSenhaHash());
                    stmt.setString(4, usuario.getTelefone());
                    stmt.setInt(5, usuario.getId());
                    stmt.setString(6, obterTipo(usuario));

                    stmt.executeUpdate();
                }

                atualizarPerfil(conn, usuario);

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackErro) {
                    e.addSuppressed(rollbackErro);
                }
                throw e;

            } finally {
                conn.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    // Atualiza os dados específicos do perfil (prestador ou contratante).
    private void atualizarPerfil(Connection conn, Usuario usuario)
            throws SQLException {

        if (usuario instanceof Prestador prestador) {

            String sql = """
                UPDATE prestadores
                SET profissao = ?, descricao = ?,
                    cidade = ?, valor_hora = ?
                WHERE id_usuario = ?
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, prestador.getProfissao());
                stmt.setString(2, prestador.getDescricao());
                stmt.setString(3, prestador.getCidade());
                stmt.setDouble(4, prestador.getValorHora());
                stmt.setInt(5, prestador.getId());

                if (stmt.executeUpdate() == 0) {
                    throw new SQLException(
                            "Perfil de prestador não encontrado.");
                }
            }

        } else if (usuario instanceof Contratante contratante) {

            String sql = """
                UPDATE contratantes
                SET cidade = ?
                WHERE id_usuario = ?
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, contratante.getCidade());
                stmt.setInt(2, contratante.getId());

                if (stmt.executeUpdate() == 0) {
                    throw new SQLException(
                            "Perfil de contratante não encontrado.");
                }
            }
        }
    }

    // 6. EXCLUIR USUÁRIO
    public boolean excluir(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            // As FKs com ON DELETE CASCADE removem
            // os registros dependentes.
            return stmt.executeUpdate() > 0;
        }
    }

    // 7. LISTAR TODOS OS USUÁRIOS
    public List<Usuario> listarTodos() throws SQLException {

        List<Usuario> usuarios = new ArrayList<>();

        String sql = consultaUsuarios() + """
            ORDER BY u.id_usuario
            """;

        try (Connection conn = connectionFactory.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(criarUsuario(rs));
            }
        }

        return usuarios;
    }

    // Consulta compartilhada para recuperar dados comuns e os campos específicos dos dois tipos.
    private String consultaUsuarios() {

        return """
            SELECT
                u.id_usuario,
                u.nome,
                u.email,
                u.senha_hash,
                u.telefone,
                u.tipo,
                p.id_usuario AS perfil_prestador,
                p.profissao,
                p.descricao,
                p.cidade AS cidade_prestador,
                p.valor_hora,
                c.id_usuario AS perfil_contratante,
                c.cidade AS cidade_contratante
            FROM usuarios u
            LEFT JOIN prestadores p
                ON p.id_usuario = u.id_usuario
            LEFT JOIN contratantes c
                ON c.id_usuario = u.id_usuario
            """;
    }

    // Reconstrói a classe concreta adequada.
    private Usuario criarUsuario(ResultSet rs) throws SQLException {

        int id = rs.getInt("id_usuario");
        String nome = rs.getString("nome");
        String email = rs.getString("email");
        String senhaHash = rs.getString("senha_hash");
        String telefone = rs.getString("telefone");
        String tipo = rs.getString("tipo");

        if ("PRESTADOR".equals(tipo)) {

            rs.getInt("perfil_prestador");

            if (rs.wasNull()) {
                throw new SQLException(
                        "Prestador sem registro na tabela prestadores.");
            }

            return new Prestador(
                    id,
                    nome,
                    email,
                    senhaHash,
                    telefone,
                    criptografiaService,
                    rs.getString("profissao"),
                    rs.getString("descricao"),
                    rs.getString("cidade_prestador"),
                    rs.getDouble("valor_hora"),
                    true
            );
        }

        if ("CONTRATANTE".equals(tipo)) {

            rs.getInt("perfil_contratante");

            if (rs.wasNull()) {
                throw new SQLException(
                        "Contratante sem registro na tabela contratantes.");
            }

            return new Contratante(
                    id,
                    nome,
                    email,
                    senhaHash,
                    telefone,
                    criptografiaService,
                    rs.getString("cidade_contratante"),
                    true
            );
        }

        throw new SQLException(
                "Tipo de usuário desconhecido: " + tipo);
    }

    // Identifica o tipo concreto.
    private String obterTipo(Usuario usuario) {

        if (usuario instanceof Prestador) {
            return "PRESTADOR";
        }

        if (usuario instanceof Contratante) {
            return "CONTRATANTE";
        }

        throw new IllegalArgumentException(
                "Tipo de usuário não suportado.");
    }

    // Valida os dados antes de operações de escrita.
    private void validarUsuario(Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "Usuário não pode ser nulo.");
        }

        obterTipo(usuario);

        validarTexto(usuario.getNome(), "Nome", 150);
        validarTexto(usuario.getEmail(), "E-mail", 150);

        if (!usuario.getEmail().matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Formato de e-mail inválido.");
        }

        validarTexto(usuario.getSenhaHash(), "Senha hash", 255);

        if (usuario.getTelefone() != null
                && usuario.getTelefone().length() > 20) {
            throw new IllegalArgumentException(
                    "Telefone muito longo.");
        }

        if (usuario instanceof Prestador prestador) {

            validarTexto(prestador.getProfissao(), "Profissão", 100);

            if (prestador.getCidade() != null
                    && prestador.getCidade().length() > 100) {
                throw new IllegalArgumentException(
                        "Cidade muito longa.");
            }

            if (!Double.isFinite(prestador.getValorHora())
                    || prestador.getValorHora() < 0) {
                throw new IllegalArgumentException(
                        "Valor por hora inválido.");
            }
        }

        if (usuario instanceof Contratante contratante
                && contratante.getCidade() != null
                && contratante.getCidade().length() > 100) {
            throw new IllegalArgumentException(
                    "Cidade muito longa.");
        }
    }

    private void validarTexto(String texto, String campo,
                              int tamanhoMaximo) {

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                    campo + " é obrigatório.");
        }

        if (texto.length() > tamanhoMaximo) {
            throw new IllegalArgumentException(
                    campo + " excede o tamanho permitido.");
        }
    }
}