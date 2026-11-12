
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
import java.util.List;

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
                anyString(), any(String[].class)
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

    @Test
    void deveListarTodosOsServicos() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(rs);
        // Simula 2 resultados encontrados
        when(rs.next()).thenReturn(true, true, false);

        when(rs.getInt("id")).thenReturn(1, 2);
        when(rs.getString("nome")).thenReturn("Servico A", "Servico B");
        when(rs.getString("descricao")).thenReturn("Desc A", "Desc B");
        when(rs.getDouble("preco_sugerido")).thenReturn(50.0, 100.0);

        List<Servico> servicos = dao.listarTodos();

        assertEquals(2, servicos.size());
        assertEquals("Servico A", servicos.get(0).getNome());
        assertEquals("Servico B", servicos.get(1).getNome());
    }

    @Test
    void deveBuscarServicoPorNome() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("nome")).thenReturn("Jardinagem");
        when(rs.getString("descricao")).thenReturn("Cortar grama");
        when(rs.getDouble("preco_sugerido")).thenReturn(80.0);

        List<Servico> servicos = dao.buscarServicoPorNome("Jardim");

        assertEquals(1, servicos.size());
        assertEquals("Jardinagem", servicos.get(0).getNome());
        verify(statement).setString(1, "%Jardim%");
    }

    @Test
    void naoDeveBuscarServicoComNomeVazio() {
        assertThrows(IllegalArgumentException.class, () -> dao.buscarServicoPorNome("   "));
    }

    @Test
    void deveAtualizarServico() throws Exception {
        Servico servico = new Servico(1, "Eletricista", "Instalacao nova", 150.0);
        when(statement.executeUpdate()).thenReturn(1); // 1 linha afetada

        boolean atualizado = dao.atualizar(servico);

        assertTrue(atualizado);
        verify(statement).setString(1, "Eletricista");
        verify(statement).setDouble(3, 150.0);
        verify(statement).setInt(4, 1);
        verify(statement).executeUpdate();
    }

    @Test
    void naoDeveAtualizarServicoInvalido() {
        Servico servicoInvalido = new Servico(1, "", "Descricao", 10.0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> dao.atualizar(servicoInvalido));
        assertEquals("Serviço invalido", ex.getMessage());


        verifyNoInteractions(conexao);
    }

    @Test
    void deveAssociarPrestadorAoServico() throws Exception {
        when(statement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> dao.associarPrestador(10, 5, 120.0));

        verify(statement).setInt(1, 10); // ID Prestador
        verify(statement).setInt(2, 5);  // ID Servico
        verify(statement).setDouble(3, 120.0); // Preço cobrado
        verify(statement).executeUpdate();
    }

    @Test
    void naoDeveAssociarComDadosInvalidos() {
        // Testando preço negativo e IDs inválidos
        assertThrows(IllegalArgumentException.class, () -> dao.associarPrestador(0, 5, 100.0));
        assertThrows(IllegalArgumentException.class, () -> dao.associarPrestador(10, -1, 100.0));
        assertThrows(IllegalArgumentException.class, () -> dao.associarPrestador(10, 5, -50.0));
    }

    @Test
    void deveDesassociarPrestador() throws Exception {
        when(statement.executeUpdate()).thenReturn(1);

        boolean removido = dao.desassociarPrestador(10, 5);

        assertTrue(removido);
        verify(statement).setInt(1, 10);
        verify(statement).setInt(2, 5);
        verify(statement).executeUpdate();
    }

    @Test
    void deveListarServicosPorPrestador() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        when(rs.getInt("id")).thenReturn(3);
        when(rs.getString("nome")).thenReturn("Encanador");
        when(rs.getString("descricao")).thenReturn("Conserto de canos");
        when(rs.getDouble("preco_sugerido")).thenReturn(90.0);
        when(rs.getDouble("preco_cobrado")).thenReturn(100.0); // Preço específico deste prestador

        List<ServicoDAO.ServicoPrestado> servicos = dao.listarServicosPorPrestador(10);

        assertEquals(1, servicos.size());
        assertEquals("Encanador", servicos.get(0).servico().getNome());
        assertEquals(100.0, servicos.get(0).precoCobrado());
        verify(statement).setInt(1, 10);
    }


}

