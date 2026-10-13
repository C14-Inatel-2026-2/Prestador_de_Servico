package br.inatel.engsoftware;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class Usuariobase extends Usuario {
    public Usuariobase(int id, String nome, String email, String senha, String telefone, CriptografiaService criptografiaService) {
        super(id, nome, email, senha, telefone, criptografiaService);
    }
}

public class UsuarioTest {
    private Usuario usuario;
    private CriptografiaService criptografiaMock;

    @BeforeEach
    void setUp() {
        criptografiaMock = Mockito.mock(CriptografiaService.class);


        when(criptografiaMock.gerarHash("senha123")).thenReturn("hashFalso123");

        usuario = new Usuariobase(1,
                "Gabriel",
                "gabriel@inatel.br",
                "senha123",
                "999999999", criptografiaMock);
    }

    @Test
    void testeMudandoDadosPerfil(){
        usuario.setNome("João");
        usuario.setEmail("joão@gmail.com");
        usuario.setTelefone("999999999");

        Assertions.assertEquals("João", usuario.getNome());
        Assertions.assertEquals("joão@gmail.com", usuario.getEmail());
        Assertions.assertEquals("999999999",usuario.getTelefone());
    }

    @Test
    void testeExibirPerfil(){
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(saida));
        try {
            usuario.exibirPerfil();
        } finally {
            System.setOut(original);
        }

        String texto = saida.toString();

        Assertions.assertNotNull(texto);
        Assertions.assertTrue(texto.contains("ID: 1"));
        Assertions.assertTrue(texto.contains("Nome: Gabriel"));
        Assertions.assertTrue(texto.contains("Email: gabriel@inatel.br"));
        Assertions.assertTrue(texto.contains("Telefone: 999999999"));
        Assertions.assertTrue(texto.contains("Nota: 0.0"));
    }

    @Test
    void testeLoginCorreto(){
        when(criptografiaMock.verificarSenha("senha123","hashFalso123")).thenReturn(true);

        boolean resultado = usuario.login("gabriel@inatel.br","senha123");

        Assertions.assertTrue(resultado);

        verify(criptografiaMock, times(1)).verificarSenha("senha123", "hashFalso123");
    }

    @Test
    void testeLoginSenhaIncorreta(){
        when(criptografiaMock.verificarSenha("senhaErrada", "hashFalso123")).thenReturn(false);

        boolean resultadoLogin = usuario.login("gabriel@inatel.br", "senhaErrada");

        Assertions.assertFalse(resultadoLogin);
    }

    @Test
    void testeAlterarSenhaComSenhaAtualCorreta(){
        when(criptografiaMock.verificarSenha("senha123", "hashFalso123")).thenReturn(true);
        when(criptografiaMock.gerarHash("novaSenha456")).thenReturn("novoHashFalso456");

        boolean senhaAlterada = usuario.alterarSenha("senha123", "novaSenha456");

        assertTrue(senhaAlterada);
        verify(criptografiaMock).gerarHash("novaSenha456");
    }

    @Test
    void testeAlterarSenhaComSenhaIncorreta(){
        when(criptografiaMock.verificarSenha("senhaErrada", "hashFalso123")).thenReturn(false);

        boolean senhaAlterada = usuario.alterarSenha("senhaErrada", "novaSenha456");

        Assertions.assertFalse(senhaAlterada);
        verify(criptografiaMock, never()).gerarHash("novaSenha456");
    }

    @Test
    void testeAtualizarNotaMedia(){
        usuario.atualizarNotaMedia(4.5);

        Assertions.assertEquals(4.5, usuario.getNotamedia());
    }

    @Test
    void testeAtualizarNotaMediaErro(){

        IllegalArgumentException erro = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            usuario.atualizarNotaMedia(8.5);
        });


        Assertions.assertEquals("A nota média deve estar entre 0 e 5.", erro.getMessage());
    }

    @Test
    void testeAtualizarNotaMediaErroNotaNegativa(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            usuario.atualizarNotaMedia(-1.0);
        });
    }

    @Test
    void testeLoginEmailIncorretoComSenhaCorreta(){
        when(criptografiaMock.verificarSenha("senha123", "hashFalso123")).thenReturn(true);

        boolean resultado = usuario.login("outro@inatel.br", "senha123");

        Assertions.assertFalse(resultado);
    }

    @Test
    void testeAposAlterarSenhaSoANovaSenhaFazLogin(){
        when(criptografiaMock.verificarSenha("senha123", "hashFalso123")).thenReturn(true);
        when(criptografiaMock.gerarHash("novaSenha456")).thenReturn("novoHash456");
        when(criptografiaMock.verificarSenha("novaSenha456", "novoHash456")).thenReturn(true);

        usuario.alterarSenha("senha123", "novaSenha456");

        Assertions.assertTrue(usuario.login("gabriel@inatel.br", "novaSenha456"));
        Assertions.assertFalse(usuario.login("gabriel@inatel.br", "senha123"));
    }


}