package br.inatel.engsoftware.dao;

import br.inatel.engsoftware.Contratante;
import br.inatel.engsoftware.CriptografiaService;
import br.inatel.engsoftware.Prestador;
import br.inatel.engsoftware.Usuario;
import br.inatel.engsoftware.database.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioDAOTest {

    @Mock
    private ConnectionFactory factoryMock;
    @Mock
    private Connection connectionMock;
    @Mock
    private PreparedStatement stmtMock;
    @Mock
    private PreparedStatement stmtPerfilMock;
    @Mock
    private ResultSet rsMock;
    @Mock
    private CriptografiaService criptografiaMock;

    private UsuarioDAO usuarioDAO;
    private Prestador prestadorValido;
    private Contratante contratanteValido;


    @BeforeEach
    void setUp() throws SQLException {
        usuarioDAO = new UsuarioDAO(factoryMock, criptografiaMock);

        lenient().when(criptografiaMock.gerarHash(anyString())).thenReturn("hashSeguro123");

        // Criando um prestador válido com ID 0 para simular um novo cadastro
        prestadorValido = new Prestador(0, "Ana Silva", "ana@email.com", "senha123", "999999999",
                criptografiaMock, "Santa Rita", "Encanadora", "Serviços gerais", 60.0);

        // NOVO: contratante para testar o caminho do outro tipo
        contratanteValido = new Contratante(0, "Bruno Lima", "bruno@email.com", "senha123", "888888888",
                criptografiaMock, "Pouso Alegre");

        lenient().when(factoryMock.abrir()).thenReturn(connectionMock);
    }

    @Test
    void deveInserirPrestadorComSucessoEComitarTransacao() throws SQLException {

        when(connectionMock.getAutoCommit()).thenReturn(true);
        when(connectionMock.prepareStatement(anyString(),(any(String[].class)))).thenReturn(stmtMock);
        when(connectionMock.prepareStatement(contains("INSERT INTO prestadores"))).thenReturn(stmtPerfilMock);

        when(stmtMock.executeUpdate()).thenReturn(1);
        when(stmtMock.getGeneratedKeys()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getInt(1)).thenReturn(15); // ID gerado pelo banco

        usuarioDAO.inserir(prestadorValido);

        assertEquals(15, prestadorValido.getId());

        // NOVO: a cidade vai para a tabela usuarios (5º parâmetro) e o tipo é o 6º faço a verificação se real mudou a posição da cidade dentro do banco
        verify(stmtMock).setString(5, "Santa Rita");
        verify(stmtMock).setString(6, "PRESTADOR");

        verify(connectionMock).setAutoCommit(false);
        verify(stmtMock).executeUpdate();
        verify(stmtPerfilMock).executeUpdate();
        verify(connectionMock).commit();
        verify(connectionMock).setAutoCommit(true); // Retorna ao estado original
    }

    // NOVO: o contratante grava a cidade em usuarios e só o id em contratantes
    @Test
    void deveInserirContratanteComCidadeNaTabelaUsuarios() throws SQLException {

        when(connectionMock.getAutoCommit()).thenReturn(true);
        when(connectionMock.prepareStatement(anyString(), any(String[].class))).thenReturn(stmtMock);
        when(connectionMock.prepareStatement(contains("INSERT INTO contratantes"))).thenReturn(stmtPerfilMock);

        when(stmtMock.getGeneratedKeys()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getInt(1)).thenReturn(20);

        usuarioDAO.inserir(contratanteValido);

        assertEquals(20, contratanteValido.getId());
        // fazendo a mesma verificação aqui, pra ver se a troca foi feita
        verify(stmtMock).setString(5, "Pouso Alegre");
        verify(stmtMock).setString(6, "CONTRATANTE");

        verify(stmtPerfilMock).setInt(1, 20);
        verify(stmtPerfilMock, never()).setString(anyInt(), anyString()); // perfil sem cidade
        verify(stmtPerfilMock).executeUpdate();
        verify(connectionMock).commit();
    }


    @Test
    void deveFazerRollbackEmCasoDeFalhaNaInsercao() throws SQLException {
        when(connectionMock.prepareStatement(anyString(),(any(String[].class)))).thenReturn(stmtMock);
        when(stmtMock.executeUpdate()).thenThrow(new SQLException("Erro no banco"));

        SQLException ex = assertThrows(SQLException.class, () -> usuarioDAO.inserir(prestadorValido));

        assertEquals("Erro no banco", ex.getMessage());
        verify(connectionMock).rollback();
        verify(connectionMock, never()).commit();
    }

    @Test
    void naoDeveInserirUsuarioJaComId() {
        prestadorValido.definirIdPersistido(10); // Simula que já veio do banco

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> usuarioDAO.inserir(prestadorValido));
        assertEquals("O usuário já possui um ID. Use atualizar().", ex.getMessage());
    }

    @Test
    void naoDevePermitirEmailInvalido() {
        Prestador usuarioEmailInvalido = new Prestador(0, "Ana", "email-sem-arroba", "senha", "999",
                criptografiaMock, "Cidade", "Profissão", "Desc", 50.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> usuarioDAO.inserir(usuarioEmailInvalido));
        assertEquals("Formato de e-mail inválido.", ex.getMessage());
    }

    @Test
    void naoDevePermitirValorHoraNegativo() {
        Prestador usuarioValorInvalido = new Prestador(0, "Ana", "ana@email.com", "senha", "999",
                criptografiaMock, "Cidade", "Profissão", "Desc", -10.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> usuarioDAO.inserir(usuarioValorInvalido));
        assertEquals("Valor por hora inválido.", ex.getMessage());
    }

    // NOVO: a validação de tamanho da cidade agora vale para qualquer usuário
    @Test
    void naoDevePermitirCidadeMuitoLonga() {
        Contratante cidadeLonga = new Contratante(0, "Bruno", "bruno@email.com", "senha", "888",
                criptografiaMock, "x".repeat(101));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioDAO.inserir(cidadeLonga));
        assertEquals("Cidade muito longa.", ex.getMessage());
    }

    @Test
    void deveBuscarUsuarioPorIdRetornandoPrestador() throws SQLException {
        when(connectionMock.prepareStatement(anyString())).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);


        when(rsMock.getInt("id_usuario")).thenReturn(1);
        when(rsMock.getString("nome")).thenReturn("Carlos");
        when(rsMock.getString("email")).thenReturn("carlos@email.com");
        when(rsMock.getString("senha_hash")).thenReturn("hash");
        when(rsMock.getString("telefone")).thenReturn("123");
        when(rsMock.getString("cidade")).thenReturn("Itajubá");
        when(rsMock.getString("tipo")).thenReturn("PRESTADOR");


        when(rsMock.getInt("perfil_prestador")).thenReturn(1);
        when(rsMock.wasNull()).thenReturn(false);
        when(rsMock.getString("profissao")).thenReturn("Marceneiro");
        when(rsMock.getString("descricao")).thenReturn("Móveis planejados");
        when(rsMock.getDouble("valor_hora")).thenReturn(70.0);

        Usuario encontrado = usuarioDAO.buscarPorId(1);

        assertNotNull(encontrado);
        Prestador prestador = assertInstanceOf(Prestador.class, encontrado);

        assertEquals("Carlos", prestador.getNome());
        // NOVO: cada campo no lugar certo (pega troca de ordem no construtor) -- para ver se a ordem esta correta

        assertEquals("Itajubá", prestador.getCidade());
        assertEquals("Marceneiro", prestador.getProfissao());
        assertEquals("Móveis planejados", prestador.getDescricao());
        assertEquals(70.0, prestador.getValorHora());
    }

    @Test
    void deveRetornarNullSeUsuarioNaoExistir() throws SQLException {
        when(connectionMock.prepareStatement(anyString())).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(false);

        Usuario encontrado = usuarioDAO.buscarPorId(99);

        assertNull(encontrado);
    }

    @Test
    void deveBuscarPorNomeComEscapamentoDeCaracteres() throws SQLException {
        when(connectionMock.prepareStatement(anyString())).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(false); // Retorna lista vazia para simplificar


        usuarioDAO.buscarPorNome("Ana % Silva");


        verify(stmtMock).setString(1, "%Ana !% Silva%");
    }

    // NOVO: a consulta precisa trazer u.cidade e não as colunas antigas
    @Test
    void consultaDeveLerCidadeDaTabelaUsuarios() throws SQLException {
        when(connectionMock.prepareStatement(anyString())).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(false);

        usuarioDAO.buscarPorId(1);

        verify(connectionMock).prepareStatement(argThat(sql ->
                sql.contains("u.cidade")
                        && !sql.contains("cidade_prestador")
                        && !sql.contains("cidade_contratante")));
    }


    @Test
    void deveExcluirUsuarioComSucesso() throws SQLException {
        when(connectionMock.prepareStatement(anyString())).thenReturn(stmtMock);
        when(stmtMock.executeUpdate()).thenReturn(1);

        boolean excluido = usuarioDAO.excluir(5);

        assertTrue(excluido);
        verify(stmtMock).setInt(1, 5);
        verify(stmtMock).executeUpdate();
    }

    @Test
    void deveAtualizarUsuarioComSucesso() throws SQLException {
        prestadorValido.definirIdPersistido(1); // Para atualizar, precisa ter ID

        // Mock para a verificação de tipo (SELECT tipo FOR UPDATE)
        when(connectionMock.prepareStatement(contains("SELECT tipo FROM usuarios"))).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getString("tipo")).thenReturn("PRESTADOR");

        // Mock para o UPDATE na tabela usuarios
        PreparedStatement stmtUpdateUser = mock(PreparedStatement.class);
        when(connectionMock.prepareStatement(contains("UPDATE usuarios"))).thenReturn(stmtUpdateUser);

        // Mock para o UPDATE na tabela prestadores
        PreparedStatement stmtUpdatePerfil = mock(PreparedStatement.class);
        when(connectionMock.prepareStatement(contains("UPDATE prestadores"))).thenReturn(stmtUpdatePerfil);
        when(stmtUpdatePerfil.executeUpdate()).thenReturn(1); // 1 linha afetada

        boolean atualizado = usuarioDAO.atualizar(prestadorValido);

        assertTrue(atualizado);

        // NOVO: cidade vai no UPDATE de usuarios (5º), id e tipo vêm depois
        verify(stmtUpdateUser).setString(5, "Santa Rita");
        verify(stmtUpdateUser).setInt(6, 1);
        verify(stmtUpdateUser).setString(7, "PRESTADOR");

        // NOVO: o UPDATE de prestadores tem 4 parâmetros, sem cidade
        verify(stmtUpdatePerfil).setString(1, "Encanadora");
        verify(stmtUpdatePerfil).setString(2, "Serviços gerais");
        verify(stmtUpdatePerfil).setDouble(3, 60.0);
        verify(stmtUpdatePerfil).setInt(4, 1);

        verify(connectionMock).commit();
    }

    @Test
    void deveBuscarUsuarioPorEmailComSucesso() throws SQLException {
        when(connectionMock.prepareStatement(anyString())).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);

        when(rsMock.getInt("id_usuario")).thenReturn(1);
        when(rsMock.getString("nome")).thenReturn("Carlos");
        when(rsMock.getString("email")).thenReturn("carlos@email.com");
        when(rsMock.getString("senha_hash")).thenReturn("hash");
        when(rsMock.getString("telefone")).thenReturn("123");
        when(rsMock.getString("cidade")).thenReturn("Santa Rita");
        when(rsMock.getString("tipo")).thenReturn("CONTRATANTE");
        when(rsMock.getInt("perfil_contratante")).thenReturn(1);
        when(rsMock.wasNull()).thenReturn(false);

        Usuario encontrado = usuarioDAO.buscarPorEmail("carlos@email.com");

        assertNotNull(encontrado);
        assertEquals("carlos@email.com", encontrado.getEmail());
    }

    @Test
    void deveBuscarUsuariosPorNomeComSucesso() throws SQLException {
        when(connectionMock.prepareStatement(anyString())).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true, false); // Retorna 1 item e depois para

        when(rsMock.getInt("id_usuario")).thenReturn(1);
        when(rsMock.getString("nome")).thenReturn("Carlos");
        when(rsMock.getString("email")).thenReturn("carlos@email.com");
        when(rsMock.getString("senha_hash")).thenReturn("hash");
        when(rsMock.getString("telefone")).thenReturn("123");
        when(rsMock.getString("cidade")).thenReturn("Santa Rita");
        when(rsMock.getString("tipo")).thenReturn("CONTRATANTE");
        when(rsMock.getInt("perfil_contratante")).thenReturn(1);
        when(rsMock.wasNull()).thenReturn(false);

        List<Usuario> encontrados = usuarioDAO.buscarPorNome("Carlos");

        assertNotNull(encontrados);
        assertFalse(encontrados.isEmpty());
        assertEquals("Carlos", encontrados.get(0).getNome());
    }

    // NOVO: contratante só confere se o perfil existe
    @Test
    void deveAtualizarContratanteConferindoPerfil() throws SQLException {
        contratanteValido.definirIdPersistido(2);

        when(connectionMock.prepareStatement(contains("SELECT tipo FROM usuarios"))).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getString("tipo")).thenReturn("CONTRATANTE");

        PreparedStatement stmtUpdateUser = mock(PreparedStatement.class);
        when(connectionMock.prepareStatement(contains("UPDATE usuarios"))).thenReturn(stmtUpdateUser);

        ResultSet rsPerfil = mock(ResultSet.class);
        when(connectionMock.prepareStatement(contains("SELECT 1 FROM contratantes"))).thenReturn(stmtPerfilMock);
        when(stmtPerfilMock.executeQuery()).thenReturn(rsPerfil);
        when(rsPerfil.next()).thenReturn(true);

        assertTrue(usuarioDAO.atualizar(contratanteValido));

        verify(stmtUpdateUser).setString(5, "Pouso Alegre");
        verify(stmtPerfilMock).setInt(1, 2);
        verify(connectionMock).commit();
    }

    // NOVO: perfil de contratante inexistente desfaz a transação
    @Test
    void deveFazerRollbackSePerfilDeContratanteNaoExistir() throws SQLException {
        contratanteValido.definirIdPersistido(2);

        when(connectionMock.prepareStatement(contains("SELECT tipo FROM usuarios"))).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getString("tipo")).thenReturn("CONTRATANTE");

        PreparedStatement stmtUpdateUser = mock(PreparedStatement.class);
        when(connectionMock.prepareStatement(contains("UPDATE usuarios"))).thenReturn(stmtUpdateUser);

        ResultSet rsPerfil = mock(ResultSet.class);
        when(connectionMock.prepareStatement(contains("SELECT 1 FROM contratantes"))).thenReturn(stmtPerfilMock);
        when(stmtPerfilMock.executeQuery()).thenReturn(rsPerfil);
        when(rsPerfil.next()).thenReturn(false);

        SQLException ex = assertThrows(SQLException.class, () -> usuarioDAO.atualizar(contratanteValido));

        assertEquals("Perfil de contratante não encontrado.", ex.getMessage());
        verify(connectionMock).rollback();
        verify(connectionMock, never()).commit();
    }
}