package br.inatel.engsoftware;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


public class ContratanteCriptografiaTest {

    @Test
    void testeLoginSenhaCorreta(){
        CriptografiaService criptografia = new BCryptService();

        Contratante contratante = new Contratante(2, "Roberto", "roberto@gmail.com", "senha123", "98888-8888", criptografia, "Santa Rita");

        boolean resultado = contratante.login("roberto@gmail.com", "senha123");

        Assertions.assertTrue(resultado);
    }

    @Test
    void testeLoginSenhaErrada(){
        CriptografiaService criptografia = new BCryptService();

        Contratante contratante = new Contratante(2, "Roberto", "roberto@gmail.com", "senha123", "98888-8888", criptografia, "Santa Rita");

        boolean resultado = contratante.login("roberto@gmail.com", "senhaErrada");

        Assertions.assertFalse(resultado);
    }

    @Test
    void testeGerarHashCadastrarContratante(){
        CriptografiaService criptografiaMock = Mockito.mock(CriptografiaService.class);

        Mockito.when(criptografiaMock.gerarHash("senha123")).thenReturn("hashFalso");

        Contratante contratante = new Contratante(2, "Roberto", "roberto@gmail.com", "senha123", "98888-8888", criptografiaMock, "Santa Rita");

        Mockito.verify(criptografiaMock,Mockito.times(1)).gerarHash("senha123");

        Assertions.assertNotNull(contratante); // confirma que o Contratante foi criado
    }

    @Test
    void testeAlterarSenhaComSenhaAtualErrada(){
        CriptografiaService criptografiaMock = Mockito.mock(CriptografiaService.class);

        Mockito.when(criptografiaMock.gerarHash("senha123")).thenReturn("hashFalso");

        Contratante contratante = new Contratante(2, "Roberto", "roberto@gmail.com", "senha123", "98888-8888", criptografiaMock, "Santa Rita");

        Mockito.when(criptografiaMock.verificarSenha("senhaErrada","hashFalso")).thenReturn(false); // faz a simulação de que o usuario digitou a senha atual incorreta

        boolean resultado = contratante.alterarSenha("senhaErrada","novaSenha"); // tenta alterar a senha, utilizando a senha incorreta

        Assertions.assertFalse(resultado);

        Mockito.verify(criptografiaMock,Mockito.times(1)).verificarSenha("senhaErrada","hashFalso");

        Mockito.verify(criptografiaMock, Mockito.never()).gerarHash("novaSenha123"); // se a senha atual for incorreta, não criptografo ela

    }
}
