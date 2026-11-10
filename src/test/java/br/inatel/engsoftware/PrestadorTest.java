package br.inatel.engsoftware;

import br.inatel.engsoftware.dao.AvaliacaoDAO;
import br.inatel.engsoftware.dao.ServicoDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PrestadorTest {

    @Mock
    private CriptografiaService criptografiaMock;

    @Mock
    private AvaliacaoDAO avaliacaoDAOMock;

    @Mock
    private ServicoDAO servicoDAOMock;

    private Prestador prestador;

    @BeforeEach
    void setUp() {

        lenient().when(criptografiaMock.gerarHash(anyString())).thenReturn("hashFalso");

        prestador = new Prestador(1, "João", "joao@email.com", "senha123", "999999999",
                criptografiaMock, "Encanador", "Consertos em geral", "Santa Rita", 50.0);

        // Injetando os DAOs mockados para evitar acesso real ao banco de dados
        prestador.definirAvaliacaoDAO(avaliacaoDAOMock);
        prestador.definirServicoDAO(servicoDAOMock);
    }



    @Test
    void testeSettersEGetters() {
        prestador.setNome("Pedro Alves");
        prestador.setProfissao("Eletricista");
        prestador.setValorHora(95.5);

        assertEquals("Pedro Alves", prestador.getNome());
        assertEquals("Eletricista", prestador.getProfissao());
        assertEquals(95.5, prestador.getValorHora(), 0.001);
    }

    @Test
    void testeExibirPerfilPrestador() throws SQLException {
        // Finge que o banco não retornou nenhum serviço para não quebrar o print
        when(servicoDAOMock.listarServicosPorPrestador(1)).thenReturn(new ArrayList<>());
        when(avaliacaoDAOMock.calcularMedia(1)).thenReturn(4.5);

        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(saida));

        try {
            prestador.exibirPerfil();
        } finally {
            System.setOut(original);
        }

        String texto = saida.toString();
        assertTrue(texto.contains("Email: joao@email.com"));
        assertTrue(texto.contains("Encanador"));
        assertTrue(texto.contains("Valor/hora: R$ 50.0"));
    }



    @Test
    void testeCalcularEAtualizarMediaComSucesso() throws SQLException {
        // Simula o banco retornando média 4.2
        when(avaliacaoDAOMock.calcularMedia(1)).thenReturn(4.2);

        prestador.calcularEAtualizarMedia();

        assertEquals(4.2, prestador.getNotamedia());
        verify(avaliacaoDAOMock).calcularMedia(1);
    }

    @Test
    void testeCalcularEAtualizarMediaPrestadorNaoCadastrado() {
        Prestador prestadorNaoCadastrado = new Prestador(0, "Ana", "ana@email.com", "senha", "123",
                criptografiaMock, "Pintora", "Pinturas", "SR", 40.0);

        IllegalStateException ex = assertThrows(IllegalStateException.class, prestadorNaoCadastrado::calcularEAtualizarMedia);
        assertEquals("O prestador precisa estar cadastrado.", ex.getMessage());
    }


    @Test
    void testeReceberAvaliacaoValidaAtualizaMedia() throws SQLException {
        Avaliacao aval = new Avaliacao(10, 2, 1, 5, "Excelente", LocalDate.now());

        // Simula a busca no banco confirmando que a avaliação existe e é para ele
        when(avaliacaoDAOMock.buscarPorId(10)).thenReturn(aval);
        // Simula o recálculo da média após receber
        when(avaliacaoDAOMock.calcularMedia(1)).thenReturn(5.0);

        prestador.receberAvaliacao(aval);

        assertEquals(5.0, prestador.getNotamedia());
        verify(avaliacaoDAOMock).buscarPorId(10);
    }

    @Test
    void testeReceberAvaliacaoInvalida() {

        Avaliacao aval = new Avaliacao(10, 2, 99, 5, "Ruim", LocalDate.now());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> prestador.receberAvaliacao(aval));
        assertEquals("Avaliação inválida ou não pertence a este prestador.", ex.getMessage());
    }

    @Test
    void testeAvaliarContratanteComSucesso() throws SQLException {
        Contratante contratanteMock = Mockito.mock(Contratante.class);
        when(contratanteMock.getId()).thenReturn(2);


        when(avaliacaoDAOMock.inserir(any(Avaliacao.class))).thenReturn(55);

        Avaliacao gerada = prestador.avaliarContratante(contratanteMock, 4, "Bom cliente");

        assertEquals(55, gerada.getId());
        assertEquals(1, gerada.getIdAutor());
        assertEquals(2, gerada.getIdAvaliado());


        verify(contratanteMock).calcularEAtualizarMedia();
        verify(avaliacaoDAOMock).inserir(any(Avaliacao.class));
    }

    @Test
    void testeCadastrarServicoNoBancoComSucesso() throws SQLException {
        prestador.cadastrarServico(5, 120.0);

        // Verifica se a classe chamou o DAO corretamente passando o ID do prestador (1), o do serviço (5) e o preço
        verify(servicoDAOMock).associarPrestador(1, 5, 120.0);
    }

    @Test
    void testeCadastrarServicoNoBancoPrecoNegativo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> prestador.cadastrarServico(5, -10.0));
        assertEquals("O preço cobrado deve ser válido e não negativo.", ex.getMessage());
    }

    @Test
    void testeRemoverServicoDoBancoComSucesso() throws SQLException {
        when(servicoDAOMock.desassociarPrestador(1, 5)).thenReturn(true);

        boolean removido = prestador.removerServico(5);

        assertTrue(removido);
        verify(servicoDAOMock).desassociarPrestador(1, 5);
    }

    @Test
    void testeRemoverServicoDoBancoIdInvalido() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> prestador.removerServico(0));
        assertEquals("O ID do serviço deve ser positivo.", ex.getMessage());
    }
}