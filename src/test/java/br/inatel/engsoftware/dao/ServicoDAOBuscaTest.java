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

    // Monta um ResultSet falso com as colunas que o UsuarioDAO lê para criar um Prestador
    private ResultSet resultadoDePrestador(int id, String nome) throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id_usuario")).thenReturn(id);
        when(rs.getString("nome")).thenReturn(nome);
        when(rs.getString("email")).thenReturn("prestador" + id + "@email.com");
        when(rs.getString("senha_hash")).thenReturn("hashFalso");
        when(rs.getString("telefone")).thenReturn("35 99999-0000");
        when(rs.getString("cidade")).thenReturn("Santa Rita do Sapucaí");
        when(rs.getString("tipo")).thenReturn("PRESTADOR");
        when(rs.getInt("perfil_prestador")).thenReturn(id);
        when(rs.getString("profissao")).thenReturn("Eletricista");
        when(rs.getString("descricao")).thenReturn("Instalações elétricas");
        when(rs.getDouble("valor_hora")).thenReturn(90.0);
        return rs;
    }

    @Test
    void deveBuscarPrestadoresDoServicoIgnorandoIdSemUsuario() throws Exception {
        ResultSet resultadoBusca = mock(ResultSet.class);
        when(resultadoBusca.next()).thenReturn(true, true, false);
        when(resultadoBusca.getInt("id_prestador")).thenReturn(7, 8);
        ResultSet resultadoPrestador = resultadoDePrestador(7, "Rita Eletricista");
        ResultSet resultadoVazio = mock(ResultSet.class);
        when(resultadoVazio.next()).thenReturn(false);
        when(statement.executeQuery())
                .thenReturn(resultadoBusca, resultadoPrestador, resultadoVazio);
        List<Prestador> encontrados = dao.buscarPrestadoresPorServico(3);
        assertEquals(1, encontrados.size());
        assertEquals(7, encontrados.get(0).getId());
        assertEquals("Rita Eletricista", encontrados.get(0).getNome());
        verify(statement).setInt(1, 3);
        verify(statement).setInt(1, 7);
        verify(statement).setInt(1, 8);
        verify(statement, times(3)).executeQuery();
    }
}
