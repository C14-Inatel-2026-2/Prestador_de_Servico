package br.inatel.engsoftware.dao;

import br.inatel.engsoftware.Avaliacao;
import br.inatel.engsoftware.database.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoDAOTest {

    @Mock
    private ConnectionFactory factoryMock;

    @Mock
    private Connection connectionMock;

    @Mock
    private PreparedStatement stmtMock;

    @Mock
    private ResultSet rsMock;

    private AvaliacaoDAO avaliacaoDAO;
    private Avaliacao avaliacaoValida;

    @BeforeEach
    void setUp() throws SQLException {
        avaliacaoDAO = new AvaliacaoDAO(factoryMock);

        // Avaliação mock para inserção (sem ID para inserção)
        avaliacaoValida = new Avaliacao(0, 1, 2, 5, "Ótimo serviço", LocalDate.now());

        // Comportamento padrão de sucesso para o Factory
        lenient().when(factoryMock.abrir()).thenReturn(connectionMock);
    }

    // ==========================================
    // TESTES DE INSERÇÃO
    // ==========================================

    @Test
    void deveInserirAvaliacaoComSucesso() throws SQLException {
        when(connectionMock.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stmtMock);
        when(stmtMock.executeUpdate()).thenReturn(1);
        when(stmtMock.getGeneratedKeys()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getInt(1)).thenReturn(10);

        int idGerado = avaliacaoDAO.inserir(avaliacaoValida);

        assertEquals(10, idGerado);
    }

    @Test
    void naoDeveInserirAvaliacaoNula() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            avaliacaoDAO.inserir(null);
        });
        assertEquals("Avaliação não pode ser nula.", exception.getMessage());
    }

    @Test
    void naoDeveInserirAvaliacaoComAutoAvaliacao() {
        // A exceção acontece no momento de criar o objeto, pois a sua classe é rigorosa
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Avaliacao(0, 1, 1, 5, "Me avaliando", LocalDate.now());
        });
        assertTrue(exception.getMessage().contains("Um usuário não pode avaliar a si mesmo"));
    }

    @Test
    void naoDeveInserirAvaliacaoComNotaInvalida() {
        // A exceção acontece no momento de criar o objeto, pois a sua classe é rigorosa
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Avaliacao(0, 1, 2, 6, "Nota fora do limite", LocalDate.now());
        });
        assertTrue(exception.getMessage().contains("Nota deve estar entre 1 e 5"));
    }

    @Test
    void deveLancarSQLExceptionQuandoNaoGerarChave() throws SQLException {
        when(connectionMock.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stmtMock);
        when(stmtMock.executeUpdate()).thenReturn(1);
        when(stmtMock.getGeneratedKeys()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(false);

        SQLException exception = assertThrows(SQLException.class, () -> {
            avaliacaoDAO.inserir(avaliacaoValida);
        });
        assertEquals("Não foi possível recuperar o ID da avaliação.", exception.getMessage());
    }

    // ==========================================
    // TESTES DE BUSCA POR ID
    // ==========================================

    @Test
    void deveBuscarPorIdComSucesso() throws SQLException {
        when(connectionMock.prepareStatement(anyString())).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);

        // Simulando os dados retornados do ResultSet (valores VÁLIDOS para não quebrar o Model)
        when(rsMock.getObject("data_avaliacao", LocalDate.class)).thenReturn(LocalDate.now());
        when(rsMock.getInt("id_avaliacao")).thenReturn(1);
        when(rsMock.getInt("id_avaliador")).thenReturn(2);
        when(rsMock.getInt("id_avaliado")).thenReturn(3);
        when(rsMock.getInt("nota")).thenReturn(4);
        when(rsMock.getString("comentario")).thenReturn("Muito bom");

        Avaliacao encontrada = avaliacaoDAO.buscarPorId(1);

        assertNotNull(encontrada);
        assertEquals(1, encontrada.getId());
        assertEquals(4, encontrada.getNota());
    }

    @Test
    void deveRetornarNullQuandoBuscarPorIdInexistente() throws SQLException {
        when(connectionMock.prepareStatement(anyString())).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(false);

        Avaliacao encontrada = avaliacaoDAO.buscarPorId(99);

        assertNull(encontrada);
    }

    @Test
    void naoDeveBuscarPorIdInvalido() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            avaliacaoDAO.buscarPorId(-1);
        });
        assertEquals("ID inválido.", exception.getMessage());
    }

    // ==========================================
    // TESTES DE LISTAGEM
    // ==========================================

    @Test
    void deveListarPorAvaliadorComSucesso() throws SQLException {
        when(connectionMock.prepareStatement(anyString())).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);

        when(rsMock.next()).thenReturn(true, true, false);

        // Simular valores válidos para o ResultSet, caso contrário `criarAvaliacao` explode a sua Model
        when(rsMock.getObject("data_avaliacao", LocalDate.class)).thenReturn(LocalDate.now());
        when(rsMock.getInt("id_avaliacao")).thenReturn(1, 2);
        when(rsMock.getInt("id_avaliador")).thenReturn(1, 1);
        when(rsMock.getInt("id_avaliado")).thenReturn(2, 3);
        when(rsMock.getInt("nota")).thenReturn(5, 4);
        when(rsMock.getString("comentario")).thenReturn("Bom", "Legal");

        List<Avaliacao> lista = avaliacaoDAO.listarPorAvaliador(1);

        assertEquals(2, lista.size());
    }

    @Test
    void naoDeveListarPorAvaliadorInvalido() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            avaliacaoDAO.listarPorAvaliador(0);
        });
        assertEquals("O ID deve ser positivo.", exception.getMessage());
    }

    // ==========================================
    // TESTES DE CÁLCULO DE MÉDIA
    // ==========================================

    @Test
    void deveCalcularMediaComSucesso() throws SQLException {
        when(connectionMock.prepareStatement(anyString())).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getDouble("media")).thenReturn(4.5);
        when(rsMock.wasNull()).thenReturn(false);

        double media = avaliacaoDAO.calcularMedia(1);

        assertEquals(4.5, media);
    }

    @Test
    void deveRetornarZeroQuandoNaoHouverMedia() throws SQLException {
        when(connectionMock.prepareStatement(anyString())).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getDouble("media")).thenReturn(0.0);
        when(rsMock.wasNull()).thenReturn(true);

        double media = avaliacaoDAO.calcularMedia(1);

        assertEquals(0.0, media);
    }

    // ==========================================
    // TESTE DE CONVERSÃO DE RESULTADO
    // ==========================================

    @Test
    void deveLancarExcecaoQuandoDataForNula() throws SQLException {
        when(connectionMock.prepareStatement(anyString())).thenReturn(stmtMock);
        when(stmtMock.executeQuery()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);

        when(rsMock.getObject("data_avaliacao", LocalDate.class)).thenReturn(null);

        SQLException exception = assertThrows(SQLException.class, () -> {
            avaliacaoDAO.buscarPorId(1);
        });
        assertEquals("Avaliação sem data cadastrada.", exception.getMessage());
    }
}