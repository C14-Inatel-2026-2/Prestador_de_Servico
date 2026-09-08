package br.inatel.engsoftware;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class AvaliacaoTest {

    @Test
    void testeValidarAvaliacaoCorreta() {
        // 6 parâmetros: id (10), Autor (1), Avaliado (2), Nota (5), Comentário, Data
        Avaliacao avaliacao = new Avaliacao(10, 1, 2, 5, "Serviço excelente!", LocalDate.now());

        Assertions.assertTrue(avaliacao.validar());
    }

    @Test
    void testeValidarBloqueiaNotaForaDoLimite() {
        // Removido o '0' extra. Apenas 6 parâmetros.
        Avaliacao avaliacaoAcima = new Avaliacao(11, 1, 2, 6, "Nota alta", LocalDate.now());
        Avaliacao avaliacaoAbaixo = new Avaliacao(12, 1, 2, 0, "Nota baixa", LocalDate.now());

        Assertions.assertFalse(avaliacaoAcima.validar());
        Assertions.assertFalse(avaliacaoAbaixo.validar());
    }

    @Test
    void testeValidarBloqueiaAutoAvaliacao() {
        // Autor e Avaliado têm o mesmo ID (1)
        Avaliacao avaliacao = new Avaliacao(13, 1, 1, 4, "Avaliando a mim mesmo", LocalDate.now());

        Assertions.assertFalse(avaliacao.validar());
    }

    @Test
    void testeValidarBloqueiaDataNula() {
        // O último parâmetro (data) está nulo
        Avaliacao avaliacao = new Avaliacao(14, 1, 2, 4, "Faltou a data", null);

        Assertions.assertFalse(avaliacao.validar());
    }

    @Test
    void testeFormatarComComentario() {
        LocalDate data = LocalDate.of(2026, 9, 3);
        Avaliacao avaliacao = new Avaliacao(15, 1, 2, 5, "Muito bom!", data);

        Assertions.assertEquals("[2026-09-03] Nota 5/5 - Muito bom!", avaliacao.formatar());
    }

    @Test
    void testeFormatarSemComentario() {
        LocalDate data = LocalDate.of(2026, 9, 3);
        // Passando string vazia (em branco) para testar o ternário
        Avaliacao avaliacao = new Avaliacao(16, 1, 2, 3, "   ", data);

        Assertions.assertEquals("[2026-09-03] Nota 3/5 - sem comentario", avaliacao.formatar());
    }
}