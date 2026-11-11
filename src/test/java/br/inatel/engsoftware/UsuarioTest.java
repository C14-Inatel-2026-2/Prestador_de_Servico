package br.inatel.engsoftware;

import br.inatel.engsoftware.dao.AvaliacaoDAO;
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

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

class Usuariobase extends Usuario {
    public Usuariobase(int id, String nome, String email, String senha, String telefone, CriptografiaService criptografiaService,String cidade) {
        super(id, nome, email, senha, telefone, criptografiaService,cidade);
    }
}

public class UsuarioTest {
    private Usuario usuario;
    private CriptografiaService criptografiaMock;
    private AvaliacaoDAO avaliacaoDAOMock;

    @BeforeEach
    void setUp() {
        criptografiaMock = Mockito.mock(CriptografiaService.class);
        avaliacaoDAOMock = Mockito.mock(AvaliacaoDAO.class);


        when(criptografiaMock.gerarHash("senha123")).thenReturn("hashFalso123");

        usuario = new Usuariobase(1,
                "Gabriel",
                "gabriel@inatel.br",
                "senha123",
                "999999999", criptografiaMock,
                "Santa Rita do Sapucaí");

        usuario.definirAvaliacaoDAO(avaliacaoDAOMock);
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

    @Test
    void testeGetESetCidade() {
        Assertions.assertEquals("Santa Rita do Sapucaí", usuario.getCidade());
        usuario.setCidade("Itajubá");
        Assertions.assertEquals("Itajubá", usuario.getCidade());
    }

    @Test
    void testeDefinirIdPersistido() {
        usuario.definirIdPersistido(10);
        Assertions.assertEquals(10, usuario.getId());

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            usuario.definirIdPersistido(0);
        });
    }

    @Test
    void testeAtualizarNotaMediaErroNaN() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            usuario.atualizarNotaMedia(Double.NaN);
        });
    }

    @Test
    void testeRegistrarAvaliacaoComSucesso() throws SQLException {
        Usuario avaliado = new Usuariobase(2, "Maria", "maria@inatel.br", "senha123", "888888888", criptografiaMock, "Pouso Alegre");
        avaliado.definirAvaliacaoDAO(avaliacaoDAOMock);

        when(avaliacaoDAOMock.inserir(any(Avaliacao.class))).thenReturn(100);
        Avaliacao avaliacaoSalvaNoBanco = new Avaliacao(100, 1, 2, 5, "Excelente serviço!", LocalDate.now());
        when(avaliacaoDAOMock.buscarPorId(100)).thenReturn(avaliacaoSalvaNoBanco);
        when(avaliacaoDAOMock.calcularMedia(2)).thenReturn(5.0);

        Avaliacao resultado = usuario.registrarAvaliacao(avaliado, 5, "Excelente serviço!");

        Assertions.assertNotNull(resultado);
        Assertions.assertEquals(100, resultado.getId());
        Assertions.assertEquals(5.0, avaliado.getNotamedia());
        verify(avaliacaoDAOMock).inserir(any(Avaliacao.class));
    }

    @Test
    void testeRegistrarAvaliacaoValidacoesDeErro() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            usuario.registrarAvaliacao(null, 5, "Bom");
        });

        Usuario avaliadoSemId = new Usuariobase(0, "Maria", "maria@inatel.br", "senha123", "888", criptografiaMock, "Cidade");
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            usuario.registrarAvaliacao(avaliadoSemId, 5, "Bom");
        });
    }

    @Test
    void testeReceberAvaliacaoInvalida() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            usuario.receberAvaliacao(null);
        });
    }

    @Test
    void testeListarAvaliacoesRecebidasEMinhasAvaliacoes() throws SQLException {
        Avaliacao av = new Avaliacao(10, 2, 1, 4, "Muito bom", LocalDate.now());
        when(avaliacaoDAOMock.listarPorAvaliado(1)).thenReturn(List.of(av));
        when(avaliacaoDAOMock.listarPorAvaliador(1)).thenReturn(List.of(av));

        List<Avaliacao> recebidas = usuario.listarAvaliacoesRecebidas();
        List<Avaliacao> feitas = usuario.listarMinhasAvaliacoes();

        Assertions.assertEquals(1, recebidas.size());
        Assertions.assertEquals(1, feitas.size());
    }

    @Test
    void testeExcecoesDeBancoNasAvaliacoes() throws SQLException {
        when(avaliacaoDAOMock.listarPorAvaliado(1)).thenThrow(new SQLException("Erro BD"));
        when(avaliacaoDAOMock.listarPorAvaliador(1)).thenThrow(new SQLException("Erro BD"));
        when(avaliacaoDAOMock.calcularMedia(1)).thenThrow(new SQLException("Erro BD"));

        Assertions.assertThrows(IllegalStateException.class, () -> usuario.listarAvaliacoesRecebidas());
        Assertions.assertThrows(IllegalStateException.class, () -> usuario.listarMinhasAvaliacoes());
        Assertions.assertThrows(IllegalStateException.class, () -> usuario.calcularEAtualizarMedia());
    }


}