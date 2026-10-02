package br.inatel.engsoftware;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BCryptServiceTest {
    private final BCryptService service = new BCryptService();

    @Test
    void hashNaoEhIgualASenhaOriginal() {
        String hash = service.gerarHash("senha123");
        Assertions.assertNotEquals("senha123", hash);
    }

    @Test
    void mesmaSenhaGeraHashesDiferentesPorCausaDoSalt() {
        Assertions.assertNotEquals(service.gerarHash("senha123"), service.gerarHash("senha123"));
    }

    @Test
    void verificarSenhaCorreta() {
        String hash = service.gerarHash("senha123");
        Assertions.assertTrue(service.verificarSenha("senha123", hash));
    }

    @Test
    void verificarSenhaErrada() {
        String hash = service.gerarHash("senha123");
        Assertions.assertFalse(service.verificarSenha("outraSenha", hash));
    }

    @Test
    void verificarComHashInvalidoLancaExcecao() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> service.verificarSenha("senha123", "hashInvalido"));
    }
}

