package br.inatel.engsoftware;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class MenuTest {

    private final InputStream entradaOriginal = System.in;
    private final PrintStream saidaOriginal = System.out;

    private String executarMenuCom(String digitado) {
        System.setIn(new ByteArrayInputStream(digitado.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
        new Menu().executar();
        return saida.toString(StandardCharsets.UTF_8);
    }

    @AfterEach
    void restaurarTecladoETela() {
        System.setIn(entradaOriginal);
        System.setOut(saidaOriginal);
    }

    @Test
    void testeSairDiretoEncerraOMenu() {
        String tela = executarMenuCom("0\n");
        assertTrue(tela.contains("Menu principal"));
        assertTrue(tela.contains("1 - Cadastrar usuario"));
        assertTrue(tela.contains("Ate logo!"));
        assertFalse(tela.contains("Entrada encerrada."));
    }

    @Test
    void testeOpcaoInvalidaMostraAvisoEContinuaNoMenu() {
        String tela = executarMenuCom("abc\n9\n0\n");
        assertTrue(tela.contains("[AVISO] Digite apenas o numero da opcao."));
        assertTrue(tela.contains("[AVISO] Opcao invalida. Escolha um numero da lista."));
        assertTrue(tela.contains("Ate logo!"));
        assertFalse(tela.contains("Entrada encerrada."));
    }

    @Test
    void testeFimDaEntradaEncerraOMenuSemErro() {
        String tela = executarMenuCom("");
        assertTrue(tela.contains("Menu principal"));
        assertTrue(tela.contains("Entrada encerrada."));
        assertTrue(tela.contains("Ate logo!"));
    }
}
