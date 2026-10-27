package br.inatel.engsoftware;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class AvaliacaoTest {
    LocalDate data = LocalDate.of(2026, 9, 3);
    LocalDate amanha = LocalDate.now().plusDays(1);

    private Avaliacao criarComNota(int nota) {
        return new Avaliacao(1, 1, 2, nota, "Serviço otimo", LocalDate.now());
    }
    @Test
    void devePermitirNotaMinimaEMaxima(){
        assertDoesNotThrow(() -> criarComNota(Avaliacao.NOTA_MINIMA));
        assertDoesNotThrow(() -> criarComNota(Avaliacao.NOTA_MAXIMA));
    }

    @Test
    void testeGettersSetters(){
            Avaliacao avaliacao = new Avaliacao(10, 1, 2, 5, "Serviço excelente!", data);

            Assertions.assertEquals(10, avaliacao.getId());
            Assertions.assertEquals(1,avaliacao.getIdAutor());
            Assertions.assertEquals(2,avaliacao.getIdAvaliado());
            Assertions.assertEquals(5,avaliacao.getNota());
            Assertions.assertEquals("Serviço excelente!",avaliacao.getComentario());
            Assertions.assertEquals(data,avaliacao.getData());
    }

    @Test
    void testeBloqueiaNotaForaDoLimite() {
        assertThrows(IllegalArgumentException.class, () -> new Avaliacao(1, 2, 3, -1, "Serviço excelente!", LocalDate.now()));
        assertThrows(IllegalArgumentException.class, () -> new Avaliacao(1, 4, 5, 6, "Serviço excelente!", LocalDate.now()));
    }

    @Test
    void testeBloqueiaAutoAvaliacao() {
        assertThrows(IllegalArgumentException.class, () -> new Avaliacao(1, 2, 2, 3, "Serviço excelente!", LocalDate.now()));
    }
    @Test
    void deveRejeitarIdAvaliadoNaoPositivo() {
        assertThrows(IllegalArgumentException.class, () -> new Avaliacao(1, 1, -2, 4, "Serviço excelente!", LocalDate.now()));
    }

    @Test
    void deveRejeitarIdAutorNaoPositivo() {
        assertThrows(IllegalArgumentException.class, () -> new Avaliacao(1, -3, 1, 4, "Serviço excelente!", LocalDate.now()));
    }

    @Test
    void deveRejeitarDataNoFuturo() {
        assertThrows(IllegalArgumentException.class, () -> new Avaliacao(1, 1, 2, 4, "Serviço excelente!", amanha));
    }
    @Test
    void deveAceitarDataDeHoje() {
        assertDoesNotThrow(() -> new Avaliacao(1, 1, 2, 4, "Serviço excelente!", LocalDate.now()));
    }

    @Test
    void deveRejeitarComentarioMuitoLongo() {
        String longo = "a".repeat(Avaliacao.TAMANHO_MAXIMO_COMENTARIO + 1);
        assertThrows(IllegalArgumentException.class, () -> new Avaliacao(1, 1, 2, 4, longo, LocalDate.now()));
    }

    @Test
    void deveAceitarComentarioNoTamanhoMaximo() {
        String limite = "a".repeat(Avaliacao.TAMANHO_MAXIMO_COMENTARIO);
        assertDoesNotThrow(() -> new Avaliacao(1, 1, 2, 4, limite, LocalDate.now()));
    }

    @Test
    void deveAceitarComentarioNulo() {
        assertDoesNotThrow(() -> new Avaliacao(1, 1, 2, 4, null, LocalDate.now()));
    }

    @Test
    void mensagemDeveIndicarNotaInvalida() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Avaliacao(1, 1, 2, 99, "Serviço excelente!", LocalDate.now()));
        assertTrue(ex.getMessage().contains("Nota deve estar entre 1 e 5"));
    }

    @Test
    void mensagemDeveIndicarAutoAvaliacao() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Avaliacao(1, 3, 3, 4, "Serviço excelente!", LocalDate.now()));
        assertTrue(ex.getMessage().contains("Um usuário não pode avaliar a si mesmo"));
    }

    @Test
    void mensagemDeveIndicarDataObrigatoria() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Avaliacao(1, 1, 2, 4, "Serviço excelente!", null));
        assertTrue(ex.getMessage().contains("Data é obrigatória"));
    }

    @Test
    void mensagemDeveIndicarDataNoFuturo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Avaliacao(1, 1, 2, 4, "Serviço excelente!", amanha));
        assertTrue(ex.getMessage().contains("Data não pode estar no futuro"));
    }

    @Test
    void deveReportarTodosOsErrosDeUmaVez() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Avaliacao(1, 3, 3, 99, "Serviço excelente!", null));

        String msg = ex.getMessage();
        assertTrue(msg.contains("Nota deve estar entre 1 e 5"));
        assertTrue(msg.contains("Um usuário não pode avaliar a si mesmo"));
        assertTrue(msg.contains("Data é obrigatória"));
    }
    @Test
    void formatarDeveUsarDataNoPadraoBrasileiro() {
        Avaliacao avaliacao = new Avaliacao(1, 1, 2, 5, "Ótimo",data);
        assertEquals("[03/09/2026] Nota 5/5 - Ótimo", avaliacao.formatar());
    }

    @Test
    void testeFormatarComComentarioNulo() {
        Avaliacao avaliacao = new Avaliacao(18, 1, 2, 3, null, data);

        assertEquals("[03/09/2026] Nota 3/5 - sem comentario", avaliacao.formatar());
    }

    @Test
    void formatarDeveMostrarSemComentarioQuandoSoEspacos() {
        Avaliacao avaliacao = new Avaliacao(1, 1, 2, 3, "   ", data);
        assertEquals("[03/09/2026] Nota 3/5 - sem comentario", avaliacao.formatar());
    }

    @Test
    void formatarDeveRemoverEspacosDasPontasDoComentario() {
        Avaliacao avaliacao = new Avaliacao(1, 1, 2, 4, "  legal  ", data);
        assertEquals("[03/09/2026] Nota 4/5 - legal", avaliacao.formatar());
    }


}