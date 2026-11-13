package br.inatel.engsoftware;

import br.inatel.engsoftware.dao.ServicoDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrestadorFalhasBancoTest {

    @Mock
    private CriptografiaService criptografiaMock;

    @Mock
    private ServicoDAO servicoDAOMock;

    private Prestador prestador;

    @BeforeEach
    void setUp() {
        lenient().when(criptografiaMock.gerarHash(anyString())).thenReturn("hashFalso");
        prestador = new Prestador(1, "João", "joao@email.com", "senha123", "999999999",
                criptografiaMock, "Santa Rita", "Encanador", "Consertos em geral", 50.0);
        prestador.definirServicoDAO(servicoDAOMock);
    }

    @Test
    void testeCadastrarServicoComFalhaNoBancoLancaIllegalStateException() throws SQLException {
        SQLException erroDoBanco = new SQLException("conexão perdida");
        doThrow(erroDoBanco).when(servicoDAOMock).associarPrestador(1, 5, 80.0);

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> prestador.cadastrarServico(5, 80.0));

        assertEquals("Erro ao cadastrar serviço do prestador.", erro.getMessage());
        assertSame(erroDoBanco, erro.getCause());
    }

    @Test
    void testeRemoverServicoComFalhaNoBancoLancaIllegalStateException() throws SQLException {
        SQLException erroDoBanco = new SQLException("conexão perdida");
        when(servicoDAOMock.desassociarPrestador(1, 5)).thenThrow(erroDoBanco);
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> prestador.removerServico(5));
        assertEquals("Erro ao remover serviço do prestador.", erro.getMessage());
        assertSame(erroDoBanco, erro.getCause());
    }
}
