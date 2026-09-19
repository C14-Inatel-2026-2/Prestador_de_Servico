package br.inatel.engsoftware;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.Mockito.*;

public class PrestadorTest {

    private Prestador prestador;
    private CriptografiaService criptografiaMock;

    @BeforeEach
    void setUp() {
        criptografiaMock = Mockito.mock(CriptografiaService.class);
        when(criptografiaMock.gerarHash(anyString())).thenReturn("hashFalso");


        prestador = new Prestador(1, "João", "joao@email.com", "senha123", "999999999",
                criptografiaMock, "Encanador", "Consertos em geral", "Santa Rita", 50.0);
    }

    @Test
    void testePrestadorNovoTemMediaZero() {
        prestador.calcularEAtualizarMedia();
        Assertions.assertEquals(0.0, prestador.getNotamedia());
    }

    @Test
    void testeReceberAvaliacaoAtualizaMediaCorretamente() {
        Avaliacao avaliacao1 = Mockito.mock(Avaliacao.class);
        Avaliacao avaliacao2 = Mockito.mock(Avaliacao.class);


        when(avaliacao1.validar()).thenReturn(true);
        when(avaliacao2.validar()).thenReturn(true);


        when(avaliacao1.getIdAvaliado()).thenReturn(1);
        when(avaliacao2.getIdAvaliado()).thenReturn(1);


        when(avaliacao1.getNota()).thenReturn(5);
        when(avaliacao2.getNota()).thenReturn(4);


        prestador.receberAvaliacao(avaliacao1);
        prestador.receberAvaliacao(avaliacao2);

        Assertions.assertEquals(4.5, prestador.getNotamedia());
    }

    @Test
    void testeReceberAvaliacaoRejeitaAvaliacaoInvalida() {
        Avaliacao avaliacaoInvalida = Mockito.mock(Avaliacao.class);

        // Finge que a avaliação tem algum dado errado e falhou na regra
        when(avaliacaoInvalida.validar()).thenReturn(false);
        when(avaliacaoInvalida.getIdAvaliado()).thenReturn(1);

        // Garante que o Fail-Fast funciona
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            prestador.receberAvaliacao(avaliacaoInvalida);
        });
    }

    @Test
    void testeReceberAvaliacaoRejeitaAvaliacaoDeOutroPrestador() {
        Avaliacao avaliacaoDeOutro = Mockito.mock(Avaliacao.class);

        when(avaliacaoDeOutro.validar()).thenReturn(true);


        when(avaliacaoDeOutro.getIdAvaliado()).thenReturn(2);


        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            prestador.receberAvaliacao(avaliacaoDeOutro);
        });
    }

    @Test
    void testeCadastrarServicoComSucesso() {
        prestador.cadastrarServico("Troca de torneira");

        Assertions.assertTrue(prestador.getServicos().contains("Troca de torneira"));
        Assertions.assertEquals(1, prestador.getServicos().size());
    }

    @Test
    void testePrestadorRecebeAvaliacaoRuimEMediaEhRecalculada() {
        Avaliacao boa1 = criarAvaliacaoMock(1, 5);
        Avaliacao boa2 = criarAvaliacaoMock(1, 5);
        Avaliacao ruim = criarAvaliacaoMock(1, 1);

        prestador.receberAvaliacao(boa1);
        prestador.receberAvaliacao(ruim);
        prestador.receberAvaliacao(boa2);

        Assertions.assertEquals(3, prestador.listarAvaliacoesRecebidas().size());
        Assertions.assertEquals(11.0 / 3.0, prestador.getNotamedia(), 0.0001);
        verify(ruim).validar();
    }

    @Test
    void testePrestadorCadastraERemoveServicosInvalidos() {
        Prestador prestadorReal = new Prestador(1, "Mario e Luigi", "SuperMarioBros@email.com", "minhasenhaeh123", "999999999",
                new BCryptService(), "Encanador", "Consertar canos é o nosso forte!", "Santa Rita", 50.0);
        prestadorReal.cadastrarServico(null);
        prestadorReal.cadastrarServico("");
        prestadorReal.cadastrarServico("   ");
        prestadorReal.cadastrarServico("Troca de torneira");
        prestadorReal.cadastrarServico("Troca de torneira");
        prestadorReal.removerServico("Serviço que nn existe");
        prestadorReal.removerServico(null);
        Assertions.assertEquals(List.of("Troca de torneira"), prestadorReal.getServicos());
    }

    private Avaliacao criarAvaliacaoMock(int idAvaliado, int nota) {
        Avaliacao avaliacao = Mockito.mock(Avaliacao.class);
        when(avaliacao.validar()).thenReturn(true);
        when(avaliacao.getIdAvaliado()).thenReturn(idAvaliado);
        when(avaliacao.getNota()).thenReturn(nota);
        return avaliacao;
    }
}
