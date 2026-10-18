package br.inatel.engsoftware;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ServicoTest {

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
}