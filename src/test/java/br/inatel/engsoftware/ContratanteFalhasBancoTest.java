package br.inatel.engsoftware;

import br.inatel.engsoftware.dao.AvaliacaoDAO;
import br.inatel.engsoftware.dao.ServicoDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContratanteFalhasBancoTest {
    private Contratante contratante;
    private AvaliacaoDAO avaliacaoDAOMock;
    private ServicoDAO servicoDAOMock;

    @BeforeEach
    void setUp() {
        CriptografiaService criptografiaMock = Mockito.mock(CriptografiaService.class);
        when(criptografiaMock.gerarHash(anyString())).thenReturn("hashFalso");
        avaliacaoDAOMock = Mockito.mock(AvaliacaoDAO.class);
        servicoDAOMock = Mockito.mock(ServicoDAO.class);
        contratante = new Contratante(2, "Roberto Carlos da Silva", "roberto@gmail.com", "1234567",
                "98888-8888", criptografiaMock, "Santa Rita do Sapucaí");
        contratante.definirAvaliacaoDAO(avaliacaoDAOMock);
        contratante.definirServicoDAO(servicoDAOMock);
    }

    @Test
    void testeAvaliarUsuarioComFalhaAoSalvarNaoEntregaAvaliacaoAoPrestador() throws SQLException {
        SQLException erroDoBanco = new SQLException("conexão perdida");
        when(avaliacaoDAOMock.inserir(any(Avaliacao.class))).thenThrow(erroDoBanco);
        Prestador prestadorMock = Mockito.mock(Prestador.class);
        when(prestadorMock.getId()).thenReturn(8);
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> contratante.avaliarUsuario(prestadorMock, 4, "Bom serviço."));
        assertEquals("Erro ao salvar avaliação no banco de dados.", erro.getMessage());
        assertSame(erroDoBanco, erro.getCause());
        verify(avaliacaoDAOMock, times(1)).inserir(any(Avaliacao.class));
        verify(prestadorMock, never()).receberAvaliacao(any(Avaliacao.class));
    }

    @Test
    void testeAvaliarUsuarioSalvaMasMediaFalhaInformaIdDaAvaliacaoSalva() throws SQLException {
        when(avaliacaoDAOMock.inserir(any(Avaliacao.class))).thenReturn(10);
        Prestador prestadorMock = Mockito.mock(Prestador.class);
        when(prestadorMock.getId()).thenReturn(8);
        IllegalStateException falhaNaMedia = new IllegalStateException("Erro ao calcular a nota média.");
        doThrow(falhaNaMedia).when(prestadorMock).receberAvaliacao(any(Avaliacao.class));
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> contratante.avaliarUsuario(prestadorMock, 4, "Bom serviço."));
        assertEquals("A avaliação foi salva (id 10), mas não foi possível atualizar a média do avaliado.",
                erro.getMessage());
        assertSame(falhaNaMedia, erro.getCause());
        verify(prestadorMock, times(1)).receberAvaliacao(argThat(avaliacao -> avaliacao.getId() == 10));
    }

    @Test
    void testeBuscarPrestadorComFalhaNoBancoLancaIllegalStateException() throws SQLException {
        // Arrange: o banco vai falhar na busca de prestadores
        SQLException erroDoBanco = new SQLException("conexão perdida");
        when(servicoDAOMock.buscarPrestadores("Eletricista")).thenThrow(erroDoBanco);

        // Act + Assert: a falha chega como IllegalStateException, não como SQLException
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> contratante.buscarPrestador("Eletricista"));

        assertEquals("Erro ao buscar prestadores no banco", erro.getMessage());
        assertSame(erroDoBanco, erro.getCause());
    }
}
