package br.inatel.engsoftware.dao;

import br.inatel.engsoftware.Prestador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ServicoDAOBuscaTest {

    private Connection conexao;
    private PreparedStatement statement;
    private ServicoDAO dao;

    @BeforeEach
    void configurar() throws Exception {
        conexao = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        when(conexao.prepareStatement(anyString())).thenReturn(statement);
        dao = new ServicoDAO(() -> conexao);
    }

    @Test
    void deveBuscarPrestadoresPeloTermoDePesquisa() throws Exception {
        ResultSet resultadoBusca = mock(ResultSet.class);
        when(resultadoBusca.next()).thenReturn(true, false);
        when(resultadoBusca.getInt("id_usuario")).thenReturn(7);
        ResultSet resultadoUsuario = mock(ResultSet.class);
        when(resultadoUsuario.next()).thenReturn(true);
        when(resultadoUsuario.getInt("id_usuario")).thenReturn(7);
        when(resultadoUsuario.getString("nome")).thenReturn("Marcos Pintor");
        when(resultadoUsuario.getString("email")).thenReturn("marcos@email.com");
        when(resultadoUsuario.getString("senha_hash")).thenReturn("hashFalso");
        when(resultadoUsuario.getString("telefone")).thenReturn("99999-0000");
        when(resultadoUsuario.getString("cidade")).thenReturn("Santa Rita do Sapucaí");
        when(resultadoUsuario.getString("tipo")).thenReturn("PRESTADOR");
        when(resultadoUsuario.getInt("perfil_prestador")).thenReturn(7);
        when(resultadoUsuario.getString("profissao")).thenReturn("Pintor");
        when(resultadoUsuario.getString("descricao")).thenReturn("Pintura residencial");
        when(resultadoUsuario.getDouble("valor_hora")).thenReturn(60.0);
        when(statement.executeQuery()).thenReturn(resultadoBusca, resultadoUsuario);
        List<Prestador> encontrados = dao.buscarPrestadores("  PINTOR ");
        assertEquals(1, encontrados.size());
        assertEquals(7, encontrados.get(0).getId());
        assertEquals("Marcos Pintor", encontrados.get(0).getNome());
        assertEquals("Pintor", encontrados.get(0).getProfissao());
        for (int i = 1; i <= 4; i++) {
            verify(statement).setString(i, "%pintor%");
        }
    }
    @Test
    void deveRecusarIdDeServicoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPrestadoresPorServico(0));
    }
}
