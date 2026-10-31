
package br.inatel.engsoftware.dao;

import br.inatel.engsoftware.Servico;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ServicoDAOTest {

    private Connection conexao;
    private PreparedStatement statement;
    private ServicoDAO dao;

    @BeforeEach
    void configurar() throws Exception {

        conexao = mock(Connection.class);
        statement = mock(PreparedStatement.class);

        when(conexao.prepareStatement(anyString()))
                .thenReturn(statement);

        dao = new ServicoDAO(() -> conexao);
    }

    @Test
    void deveCadastrarServico() throws Exception {

        Servico servico = new Servico(
                0, "Eletricista", "Instalacao", 120.0
        );

        ResultSet chaves = mock(ResultSet.class);

        when(conexao.prepareStatement(
                anyString(), eq(Statement.RETURN_GENERATED_KEYS)
        )).thenReturn(statement);

        when(statement.getGeneratedKeys())
                .thenReturn(chaves);

        when(chaves.next()).thenReturn(true);
        when(chaves.getInt(1)).thenReturn(10);

        dao.inserir(servico);

        assertEquals(10, servico.getId());

        verify(statement).setString(1, "Eletricista");
        verify(statement).setDouble(3, 120.0);
        verify(statement).executeUpdate();
    }

    @Test
    void deveBuscarServicoPorId() throws Exception {

        ResultSet resultado = mock(ResultSet.class);

        when(statement.executeQuery())
                .thenReturn(resultado);

        when(resultado.next()).thenReturn(true);

        when(resultado.getInt("id")).thenReturn(1);
        when(resultado.getString("nome"))
                .thenReturn("Pintura");

        when(resultado.getString("descricao"))
                .thenReturn("Pintura residencial");

        when(resultado.getDouble("preco_sugerido"))
                .thenReturn(200.0);

        Servico servico = dao.buscarPorId(1);

        assertNotNull(servico);
        assertEquals(1, servico.getId());
        assertEquals("Pintura", servico.getNome());
        assertEquals(200.0, servico.getPrecoSugerido());

        verify(statement).setInt(1, 1);
    }

    @Test
    void deveRetornarNullQuandoNaoEncontrar()
            throws Exception {

        ResultSet resultado = mock(ResultSet.class);

        when(statement.executeQuery())
                .thenReturn(resultado);

        when(resultado.next()).thenReturn(false);

        assertNull(dao.buscarPorId(999));
    }

    @Test
    void deveExcluirServico() throws Exception {

        when(statement.executeUpdate()).thenReturn(1);

        boolean excluido = dao.excluir(5);

        assertTrue(excluido);

        verify(statement).setInt(1, 5);
        verify(statement).executeUpdate();
    }

    @Test
    void naoDeveCadastrarServicoInvalido()
            throws Exception {

        Servico servico = new Servico(
                0, "", "Descricao", 100.0
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> dao.inserir(servico)
        );

        verifyNoInteractions(conexao);
    }
}

