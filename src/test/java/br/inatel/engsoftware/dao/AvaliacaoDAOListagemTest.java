package br.inatel.engsoftware.dao;

import br.inatel.engsoftware.Avaliacao;
import br.inatel.engsoftware.database.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Testa a listagem de avaliações recebidas (listarPorAvaliado) com JDBC mockado.
// Nada aqui chega ao banco de dados.
@ExtendWith(MockitoExtension.class)
class AvaliacaoDAOListagemTest {

    @Mock
    private ConnectionFactory factoryMock;

    @Mock
    private Connection connectionMock;

    @Mock
    private PreparedStatement stmtMock;

    @Mock
    private ResultSet rsMock;

    private AvaliacaoDAO avaliacaoDAO;

    @BeforeEach
    void setUp() throws SQLException {
        avaliacaoDAO = new AvaliacaoDAO(factoryMock);
        lenient().when(factoryMock.abrir()).thenReturn(connectionMock);
    }

    @Test
    void deveListarAvaliacoesRecebidasPeloAvaliado() throws SQLException {
        // Arrange: o banco devolve duas avaliações recebidas pelo usuário 2
        when(connectionMock.prepareStatement(anyString())).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true, true, false);

        when(rsMock.getObject("data_avaliacao", LocalDate.class)).thenReturn(LocalDate.now());
        when(rsMock.getInt("id_avaliacao")).thenReturn(11, 12);
        when(rsMock.getInt("id_avaliador")).thenReturn(5, 6);
        when(rsMock.getInt("id_avaliado")).thenReturn(2, 2);
        when(rsMock.getInt("nota")).thenReturn(5, 3);
        when(rsMock.getString("comentario")).thenReturn("Excelente", "Atrasou um pouco");

        // Act
        List<Avaliacao> recebidas = avaliacaoDAO.listarPorAvaliado(2);

        // Assert: as duas linhas viraram objetos Avaliacao, na ordem do banco
        assertEquals(2, recebidas.size());
        assertEquals(11, recebidas.get(0).getId());
        assertEquals(5, recebidas.get(0).getNota());
        assertEquals(12, recebidas.get(1).getId());
        assertEquals("Atrasou um pouco", recebidas.get(1).getComentario());
        assertTrue(recebidas.stream().allMatch(a -> a.getIdAvaliado() == 2));

        // Assert: a consulta filtrou pelo avaliado certo
        verify(stmtMock).setInt(1, 2);
    }

    @Test
    void deveRetornarListaVaziaQuandoAvaliadoNaoTemAvaliacoes() throws SQLException {
        when(connectionMock.prepareStatement(anyString())).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(false);

        List<Avaliacao> recebidas = avaliacaoDAO.listarPorAvaliado(2);

        assertNotNull(recebidas);
        assertTrue(recebidas.isEmpty());
    }

    @Test
    void naoDeveListarPorAvaliadoInvalido() throws SQLException {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> avaliacaoDAO.listarPorAvaliado(0));

        assertEquals("O ID deve ser positivo.", erro.getMessage());
        verify(factoryMock, never()).abrir();
    }

    @Test
    void deveRecusarAvaliacaoInvalidaVindaDoBanco() throws SQLException {
        // Arrange: uma linha corrompida no banco, com nota 9 (fora de 1 a 5)
        when(connectionMock.prepareStatement(anyString())).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);

        when(rsMock.getObject("data_avaliacao", LocalDate.class)).thenReturn(LocalDate.now());
        when(rsMock.getInt("id_avaliacao")).thenReturn(11);
        when(rsMock.getInt("id_avaliador")).thenReturn(5);
        when(rsMock.getInt("id_avaliado")).thenReturn(2);
        when(rsMock.getInt("nota")).thenReturn(9);
        when(rsMock.getString("comentario")).thenReturn("Nota impossível");

        // Act + Assert: o DAO não devolve dado inválido; avisa que o banco tem um registro quebrado
        SQLException erro = assertThrows(SQLException.class,
                () -> avaliacaoDAO.listarPorAvaliado(2));

        assertTrue(erro.getMessage().startsWith("Avaliação inválida no banco:"));
        assertInstanceOf(IllegalArgumentException.class, erro.getCause());
    }
}
