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

    public Avaliacao avaliarUsuario(Prestador prestador, int nota, String comentario) {
        if (prestador == null) {
            throw new IllegalArgumentException("O prestador não pode ser nulo.");
        }
        if (this.getId() <= 0 || prestador.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Os usuários precisam estar cadastrados.");
        }

        try {
            // Tenta criar a avaliação. Se a nota for -1 ou 1000, a classe da sua colega vai estourar um erro aqui.
            Avaliacao avaliacao = new Avaliacao(0, this.getId(), prestador.getId(), nota, comentario, LocalDate.now());

            int idGerado = avaliacaoDAO.inserir(avaliacao);
            Avaliacao avaliacaoSalva = new Avaliacao(
                    idGerado,
                    avaliacao.getIdAutor(),
                    avaliacao.getIdAvaliado(),
                    avaliacao.getNota(),
                    avaliacao.getComentario(),
                    avaliacao.getData()
            );
            return avaliacaoSalva;
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
        if (this.getId() > 0) {
            calcularEAtualizarMedia();
        }
        super.exibirPerfil();
        System.out.println("Cidade: " + cidade);
    }
    public void calcularEAtualizarMedia() {

        if (this.getId() <= 0) {
            throw new IllegalStateException(
                    "O contratante precisa estar cadastrado.");
        }

        try {
            double media = avaliacaoDAO.calcularMedia(this.getId());

            super.atualizarNotaMedia(media);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao calcular a média do contratante.", e);
        }
    }

    // Listar avaliações recebidas pelo contratante
    public List<Avaliacao> listarAvaliacoesRecebidas() {
        try {
            return avaliacaoDAO.listarPorAvaliado(this.getId());

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao consultar avaliações recebidas.", e);
        }
    }

}
