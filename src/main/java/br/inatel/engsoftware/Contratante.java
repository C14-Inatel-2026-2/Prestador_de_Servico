package br.inatel.engsoftware;

import java.time.LocalDate;
import java.util.List;

import br.inatel.engsoftware.dao.ServicoDAO;
import br.inatel.engsoftware.dao.AvaliacaoDAO;
import java.sql.SQLException;
import java.util.Objects;

public class Contratante extends Usuario {

    private String cidade;
    private AvaliacaoDAO avaliacaoDAO = new AvaliacaoDAO();

    //Permite injeção de dependencia
    public void definirAvaliacaoDAO(AvaliacaoDAO avaliacaoDAO) {
        this.avaliacaoDAO = Objects.requireNonNull(avaliacaoDAO);
    }


    //Permite injeção de dependencia
    private ServicoDAO servicoDAO = new ServicoDAO();
    public void definirServicoDAO(ServicoDAO servicoDAO){
        this.servicoDAO = Objects.requireNonNull(servicoDAO);
    }


    //Usado para CADASTRAR um contratante novo (a senha vem em texto puro e vira hash)
    public Contratante(int id, String nome, String email, String senha,
                       String telefone, CriptografiaService criptografiaService,
                       String cidade) {
        this(id, nome, email, senha, telefone,
                criptografiaService, cidade, false);
    }

    public Contratante(int id, String nome, String email, String senha, String telefone,
                       CriptografiaService criptografiaService, String cidade, boolean senhaJaCriptografada) {
        super(id, nome, email, senha, telefone, criptografiaService, senhaJaCriptografada );
        this.cidade = cidade;
    }

    public String getCidade() { return cidade; }

    public void setCidade(String cidade) { this.cidade = cidade; }

    public List<Prestador> buscarPrestador(String termo) {
        try {
            return servicoDAO.buscarPrestadores(termo);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao buscar prestadores no banco", e);
        }
    }

    public Avaliacao avaliarUsuario(Usuario avaliado, int nota, String comentario) {
        if (avaliado == null) {
            return null; // Protege contra NullPointerException
        }

        try {
            // Tenta criar a avaliação. Se a nota for -1 ou 1000, a classe da sua colega vai estourar um erro aqui.
            Avaliacao avaliacao = new Avaliacao(0, this.getId(), avaliado.getId(), nota, comentario, LocalDate.now());

            int idGerado = avaliacaoDAO.inserir(avaliacao);
            return new Avaliacao(
                    idGerado,
                    avaliacao.getIdAutor(),
                    avaliacao.getIdAvaliado(),
                    avaliacao.getNota(),
                    avaliacao.getComentario(),
                    avaliacao.getData()
            );

        } catch (IllegalArgumentException e){
            return null;
        }catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao salvar avaliação no banco de dados.", e);
        }


    }

    public List<Avaliacao> listarMinhasAvaliacoes() {
        try {
            return avaliacaoDAO.listarPorAvaliador(this.getId());

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao consultar histórico de avaliações.", e);
        }
    }
    @Override
    public void exibirPerfil() {
        super.exibirPerfil();
        System.out.println("Cidade: " + cidade);
    }



}
