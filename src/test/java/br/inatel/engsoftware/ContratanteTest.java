package br.inatel.engsoftware;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
}

