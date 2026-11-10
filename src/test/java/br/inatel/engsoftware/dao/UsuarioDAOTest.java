package br.inatel.engsoftware.dao;

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

    @BeforeEach
    void setUp() throws SQLException {
        usuarioDAO = new UsuarioDAO(factoryMock, criptografiaMock);

        lenient().when(criptografiaMock.gerarHash(anyString())).thenReturn("hashSeguro123");

        // Criando um prestador válido com ID 0 para simular um novo cadastro
        prestadorValido = new Prestador(0, "Ana Silva", "ana@email.com", "senha123", "999999999",
                criptografiaMock, "Encanadora", "Serviços gerais", "Santa Rita", 60.0);

        lenient().when(factoryMock.abrir()).thenReturn(connectionMock);
    }

    @Test
    void deveInserirPrestadorComSucessoEComitarTransacao() throws SQLException {

        when(connectionMock.getAutoCommit()).thenReturn(true);

        when(connectionMock.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stmtMock);

        when(connectionMock.prepareStatement(contains("INSERT INTO prestadores"))).thenReturn(stmtPerfilMock);

        when(stmtMock.executeUpdate()).thenReturn(1);
        when(stmtMock.getGeneratedKeys()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getInt(1)).thenReturn(15); // ID gerado pelo banco

        usuarioDAO.inserir(prestadorValido);

        assertEquals(15, prestadorValido.getId());

        verify(connectionMock).setAutoCommit(false);
        verify(stmtMock).executeUpdate();
        verify(stmtPerfilMock).executeUpdate();
        verify(connectionMock).commit();
        verify(connectionMock).setAutoCommit(true); // Retorna ao estado original
    }

    @Test
    void deveFazerRollbackEmCasoDeFalhaNaInsercao() throws SQLException {
        when(connectionMock.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stmtMock);


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
                criptografiaMock, "Profissão", "Desc", "Cidade", 50.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> usuarioDAO.inserir(usuarioEmailInvalido));
        assertEquals("Formato de e-mail inválido.", ex.getMessage());
    }

    @Test
    void naoDevePermitirValorHoraNegativo() {
        Prestador usuarioValorInvalido = new Prestador(0, "Ana", "ana@email.com", "senha", "999",
                criptografiaMock, "Profissão", "Desc", "Cidade", -10.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> usuarioDAO.inserir(usuarioValorInvalido));
        assertEquals("Valor por hora inválido.", ex.getMessage());
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
        when(rsMock.getString("tipo")).thenReturn("PRESTADOR");


        when(rsMock.getInt("perfil_prestador")).thenReturn(1);
        when(rsMock.wasNull()).thenReturn(false);
        when(rsMock.getString("profissao")).thenReturn("Marceneiro");
        when(rsMock.getString("descricao")).thenReturn("Móveis planejados");
        when(rsMock.getString("cidade_prestador")).thenReturn("Itajubá");
        when(rsMock.getDouble("valor_hora")).thenReturn(70.0);

        Usuario encontrado = usuarioDAO.buscarPorId(1);

        assertNotNull(encontrado);
        assertTrue(encontrado instanceof Prestador);
        assertEquals("Carlos", encontrado.getNome());
        assertEquals("Marceneiro", ((Prestador) encontrado).getProfissao());
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
        verify(connectionMock).commit();
    }
}