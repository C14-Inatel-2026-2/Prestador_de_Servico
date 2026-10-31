package br.inatel.engsoftware;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ServicoTest {

    private Servico servicoPadrao;

    @BeforeEach
    void setUp() {
        servicoPadrao = new Servico(1, "Troca de torneira", "Troca simples de torneira", 80.0);

    }

    @Test
    void deveCadastrarServicoCorretamente() {
        Servico servico = new Servico(1, "Eletricista", "Trocar quadro de disjuntores", 150.0);

        assertEquals(1, servico.getId());
        assertEquals("Eletricista", servico.getNome());
        assertEquals("Trocar quadro de disjuntores", servico.getDescricao());
        assertEquals(150.0, servico.getPrecoSugerido());
    }

    @Test
    void naoDevePermitirPrecoNegativo() {
        Servico servico = new Servico(2, "Pintura", "Pintura de 4 portas internas de aço", 200.0);

        assertThrows(IllegalArgumentException.class, () -> {
            servico.setPrecoSugerido(-50.0);
        });
    }

    @Test
    void naoDevePermitirPrecoNegativoNoConstrutor() {
        assertThrows(IllegalArgumentException.class,
                () -> new Servico(3, "Invalido", "Preco negativo", -1.0));
    }

    @Test
    void devePermitirPrecoZeroNoConstrutor() {
        Servico gratis = new Servico(4, "Orcamento", "Visita para orcamento", 0.0);

        assertEquals(0.0, gratis.getPrecoSugerido());
    }

    @Test
    void deveAtualizarDadosPeloSetter() {
        servicoPadrao.setId(9);
        servicoPadrao.setNome("Desentupimento");
        servicoPadrao.setDescricao("Desentupimento de pia");
        servicoPadrao.setPrecoSugerido(120.5);

        assertEquals(9, servicoPadrao.getId());
        assertEquals("Desentupimento", servicoPadrao.getNome());
        assertEquals("Desentupimento de pia", servicoPadrao.getDescricao());
        assertEquals(120.5, servicoPadrao.getPrecoSugerido());
    }

    @Test
    void devePermitirPrecoZeroNoSetter() {
        servicoPadrao.setPrecoSugerido(0.0);

        assertEquals(0.0, servicoPadrao.getPrecoSugerido());
    }

    @Test
    void deveManterPrecoAnteriorQuandoSetterRecebeNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> servicoPadrao.setPrecoSugerido(-5.0));

        assertEquals(80.0, servicoPadrao.getPrecoSugerido());
    }

    @Test
    void deveValidarServicoCorreto() {
        assertTrue(servicoPadrao.validar());
    }

    @Test
    void deveValidarServicoComPrecoZero() {
        servicoPadrao.setPrecoSugerido(0.0);

        assertTrue(servicoPadrao.validar());
    }

    @Test
    void naoDeveValidarServicoComNomeNulo() {
        servicoPadrao.setNome(null);

        assertFalse(servicoPadrao.validar());
    }

    @Test
    void naoDeveValidarServicoComNomeEmBranco() {
        servicoPadrao.setNome("   ");

        assertFalse(servicoPadrao.validar());
    }

    @Test
    void naoDeveValidarServicoComPrecoNaN() {
        servicoPadrao.setPrecoSugerido(Double.NaN); // NaN passa pelo "< 0", mas não pelo ">= 0"

        assertFalse(servicoPadrao.validar());
    }

    @Test
    void deveFormatarServico() {
        assertEquals("Troca de torneira - R$ 80.0", servicoPadrao.formatar());
    }
}