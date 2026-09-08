package br.inatel.engsoftware;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

        usuario = new Usuariobase(1,"Gabriel", "gabriel@inatel.br", "senha123", "999999999", criptografiaMock);
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


}