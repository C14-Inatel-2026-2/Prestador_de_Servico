package br.inatel.engsoftware;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.mockito.Mockito.*;

public class PrestadorTest {

    private Prestador prestador;
    private CriptografiaService criptografiaMock;

    @BeforeEach
    void setUp() {
        criptografiaMock = Mockito.mock(CriptografiaService.class);
        when(criptografiaMock.gerarHash(anyString())).thenReturn("hashFalso");


        prestador = new Prestador(1, "João",
                "joao@email.com",
                "senha123",
                "999999999",
                criptografiaMock,
                "Encanador",
                "Consertos em geral",
                "Santa Rita",
                50.0);
    }

    @Test
    void testeSettersEGetters() {
        prestador.setNome("Pedro Alves");
        prestador.setEmail("pedro@email.com");
        prestador.setTelefone("35966660000");
        prestador.setProfissao("Eletricista");
        prestador.setDescricao("Reparos hidraulicos");
        prestador.setCidade("Varginha");
        prestador.setValorHora(95.5);

        Assertions.assertEquals("Pedro Alves", prestador.getNome());
        Assertions.assertEquals("pedro@email.com", prestador.getEmail());
        Assertions.assertEquals("35966660000", prestador.getTelefone());
        Assertions.assertEquals("Eletricista", prestador.getProfissao());
        Assertions.assertEquals("Reparos hidraulicos", prestador.getDescricao());
        Assertions.assertEquals("Varginha", prestador.getCidade());
        Assertions.assertEquals(95.5, prestador.getValorHora(), 0.001);
    }

    @Test
    void testeExibirPerfilPrestador() {
        prestador.cadastrarServico("Instalacao eletrica");
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(saida));

        try {
            prestador.exibirPerfil();
        } finally {
            System.setOut(original);
        }

        String texto = saida.toString();

        Assertions.assertTrue(texto.contains("Email: joao@email.com"));
        Assertions.assertTrue(texto.contains("Encanador"));
        Assertions.assertTrue(texto.contains("Consertos em geral"));
        Assertions.assertTrue(texto.contains("Cidade: Santa Rita"));
        Assertions.assertTrue(texto.contains("Valor/hora: R$ 50.0"));
        Assertions.assertTrue(texto.contains("Instalacao eletrica"));
    }

    @Test
    void testePrestadorNovoTemMediaZero() {
        prestador.calcularEAtualizarMedia();
        Assertions.assertEquals(0.0, prestador.getNotamedia());
    }

    @Test
    void setetrsgettersTest(){
        prestador.setProfissao("Eletricista");

    }

    @Test
    void testeReceberAvaliacaoAtualizaMediaCorretamente() {
        Avaliacao avaliacao1 = Mockito.mock(Avaliacao.class);
        Avaliacao avaliacao2 = Mockito.mock(Avaliacao.class);


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

        when(avaliacaoInvalida.getIdAvaliado()).thenReturn(1);

        // Garante que o Fail-Fast funciona
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            prestador.receberAvaliacao(avaliacaoInvalida);
        });
    }

    @Test
    void testeReceberAvaliacaoRejeitaAvaliacaoDeOutroPrestador() {
        Avaliacao avaliacaoDeOutro = Mockito.mock(Avaliacao.class);


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

    @Test
    void testeRemoverServicoExistenteManterOsOutrosServicos() {
        Prestador prestadorReal = new Prestador(3, "José", "jose@email.com", "arrumando6789", "999999999",
                new BCryptService(), "Encanador", "Consertos em geral", "Santa Rita", 65.0);
        prestadorReal.cadastrarServico("Troca de valvula de descarga");
        prestadorReal.cadastrarServico("Troca de torneira");
        prestadorReal.cadastrarServico("Troca de sifão");
        prestadorReal.cadastrarServico("Desentupimento");
        prestadorReal.removerServico("Troca de torneira");
        prestadorReal.removerServico("Troca de valvula de descarga");
        Assertions.assertEquals(List.of("Troca de sifão","Desentupimento"), prestadorReal.getServicos());
    }

    private Avaliacao criarAvaliacaoMock(int idAvaliado, int nota) {
        Avaliacao avaliacao = Mockito.mock(Avaliacao.class);
        when(avaliacao.getIdAvaliado()).thenReturn(idAvaliado);
        when(avaliacao.getNota()).thenReturn(nota);
        return avaliacao;
    }

    @Test
    void testeAvaliacaoNulaEhRejeitadaENaoAlteraMedia() {
        prestador.receberAvaliacao(criarAvaliacaoMock(1, 4));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> prestador.receberAvaliacao(null));

        Assertions.assertEquals(1, prestador.listarAvaliacoesRecebidas().size());
        Assertions.assertEquals(4.0, prestador.getNotamedia());
    }
}
