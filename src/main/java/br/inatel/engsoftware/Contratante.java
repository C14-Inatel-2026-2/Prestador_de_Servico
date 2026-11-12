package br.inatel.engsoftware;
import java.util.List;

import br.inatel.engsoftware.dao.ServicoDAO;
import java.sql.SQLException;
import java.util.Objects;

public class Contratante extends Usuario {

    // Cada filha usa o ServicoDAO para uma coisa diferente, então ele fica aqui e não em Usuario.
    private ServicoDAO servicoDAO = new ServicoDAO();

    void definirServicoDAO(ServicoDAO servicoDAO) {
        this.servicoDAO = Objects.requireNonNull(servicoDAO);
    }

    //Usado para CADASTRAR um contratante novo (a senha vem em texto puro e vira hash)
    public Contratante(int id, String nome, String email, String senha,
                       String telefone,CriptografiaService criptografiaService,String cidade
                       ) {
        this(id, nome, email, senha, telefone, criptografiaService,cidade, false);
    }

    public Contratante(int id, String nome, String email, String senha, String telefone,
                       CriptografiaService criptografiaService,String cidade ,boolean senhaJaCriptografada) {
        super(id, nome, email, senha, telefone,criptografiaService,cidade,senhaJaCriptografada );
    }


    public List<Prestador> buscarPrestador(String termo) {
        try {
            return servicoDAO.buscarPrestadores(termo);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao buscar prestadores no banco", e);
        }
    }

    public Avaliacao avaliarUsuario(Prestador prestador, int nota, String comentario) {
        return registrarAvaliacao(prestador, nota, comentario);
    }

}
