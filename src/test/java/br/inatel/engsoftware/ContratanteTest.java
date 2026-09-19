package br.inatel.engsoftware;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class ContratanteTest {
    private Prestador prestador;
    private Contratante contratante;
    private CriptografiaService criptografiaMock;

    @BeforeEach
    void setUp() {
        criptografiaMock = Mockito.mock(CriptografiaService.class);
        when(criptografiaMock.gerarHash(anyString())).thenReturn("hashFalso");


        prestador = new Prestador(8,"Paulo Ricardo Alvez","Paulo@gmail.com","8756985",
                "95555-5555", criptografiaMock, "Carpinteiro","Faço carpintaria a mais de 25 anos, sou o senhor responsavel e trabalhador, caso precisem dos meus serviços estou a disposição!",
                "Cachoeira de Minas",34.8);

        contratante = new Contratante(2,"Roberto Carlos da Siva","roberto@gmail.com","1234567",
                "98888-8888",criptografiaMock,"Santa Rita do Sapucai");
    }

    @Test
    void testeReceberNotaInvalidaAvaliacao(){
        criptografiaMock = Mockito.mock(CriptografiaService.class);

        Avaliacao notaInvalida = contratante.avaliarUsuario(prestador,-1,"Muito bom");
        Assertions.assertNull(notaInvalida); //como a nota não é valida deve retornar null
    }

    @Test
    void testeAvaliarUsuarioComMesmoId(){
        criptografiaMock = Mockito.mock(CriptografiaService.class);

        prestador = new Prestador(2,"Paulo Ricardo Alvez","Paulo@gmail.com","8756985",
                "95555-5555", criptografiaMock, "Carpinteiro","Faço carpintaria a mais de 25 anos, sou o senhor responsavel e trabalhador, caso precisem dos meus serviços estou a disposição!",
                "Cachoeira de Minas",34.8);

        contratante = new Contratante(2,"Roberto Carlos da Siva","roberto@gmail.com","1234567",
                "98888-8888",criptografiaMock,"Santa Rita do Sapucai");

        Avaliacao idIgual = contratante.avaliarUsuario(prestador,5,"Muito bom");
        Assertions.assertNull(idIgual); //como estou colocando o id do avaliador igual o id de quem ele esta avaliando
    }
    @Test
    void testeContratanteComNotaAbsurdaNaoGeraAvaliacao() { //teste onde uma nota absurda é dada obs: teste negativo
        Prestador prestadorMock = Mockito.mock(Prestador.class);
        when(prestadorMock.getId()).thenReturn(8);

        Avaliacao resultado = contratante.avaliarUsuario(prestadorMock, 1000, "Nota mil!!! ;)");

        Assertions.assertNull(resultado);
        verify(prestadorMock, never()).receberAvaliacao(any()); //prestador não recebe a avaliação
    }


    @Test
    void testeContratanteVingativoDaNotaMinimaSemComentario() { //teste sem mock: objetos reais e um fake simples no lugar do BCrypt
        CriptografiaService fake = new CriptografiaFake();
        Contratante contratanteReal = new Contratante(2, "Browser", "browser@gmail.com", "amoprincesapeach",
                "98888-8888", fake, "Santa Rita do Sapucai");
        Prestador prestadorReal = new Prestador(8, "Mario", "mario@gmail.com", "familia123",
                "95555-5555", fake, "Encanador", "Encanador experiente", "Cachoeira de Minas", 34.8);

        Avaliacao avaliacao = contratanteReal.avaliarUsuario(prestadorReal, 1, "   ");

        Assertions.assertNotNull(avaliacao);
        Assertions.assertAll( //executa todas as verificações da avaliação devolvida pelo contratante
                () -> Assertions.assertEquals(2, avaliacao.getIdAutor()),
                () -> Assertions.assertEquals(8, avaliacao.getIdAvaliado()),
                () -> Assertions.assertEquals(1, avaliacao.getNota()),
                () -> Assertions.assertTrue(avaliacao.formatar().endsWith("sem comentario"))
        );
    }

    private static class CriptografiaFake implements CriptografiaService {
        @Override
        public String gerarHash(String senhaNormal) {
            return "hash:" + senhaNormal;
        }

        @Override
        public boolean verificarSenha(String senhaNormal, String senhaHash) {
            return gerarHash(senhaNormal).equals(senhaHash);
        }
    }
}
