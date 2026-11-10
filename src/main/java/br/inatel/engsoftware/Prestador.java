package br.inatel.engsoftware;

import java.util.ArrayList;
import java.util.List;

import br.inatel.engsoftware.dao.AvaliacaoDAO;
import br.inatel.engsoftware.dao.ServicoDAO;
import br.inatel.engsoftware.dao.ServicoDAO.ServicoPrestado;


import java.time.LocalDate;
import java.sql.SQLException;
import java.util.Objects;


public class Prestador extends Usuario {

    private String profissao;
    private String descricao;
    private String cidade;
    private double valorHora;
    private final List<String> servicos;
    private final List<Avaliacao> avaliacoesRecebidas;

    private AvaliacaoDAO avaliacaoDAO = new AvaliacaoDAO();
    // Permite injeção de dependência
    public void definirAvaliacaoDAO(AvaliacaoDAO avaliacaoDAO) {
        this.avaliacaoDAO = Objects.requireNonNull(avaliacaoDAO);
    }

    private ServicoDAO servicoDAO = new ServicoDAO();
    // Permite injeção de dependência
    public void definirServicoDAO(ServicoDAO servicoDAO) {
        this.servicoDAO = Objects.requireNonNull(servicoDAO);
    }



    //Usado para CADASTRAR um prestador novo (a senha vem em texto puro e vira hash)
    public Prestador(int id, String nome, String email, String senha,
                     String telefone, CriptografiaService criptografiaService,
                     String profissao, String descricao, String cidade,
                     double valorHora) {
        this(id, nome, email, senha, telefone, criptografiaService,
                profissao, descricao, cidade, valorHora, false);
    }

    //Usado para a persistencia dos dados no banco, USO EXCLUSIVO DO UsuarioDAO: carrega um prestador do banco.
    public Prestador(int id, String nome, String email, String senha, String telefone,
                     CriptografiaService criptografiaService,
                     String profissao, String descricao, String cidade, double valorHora, boolean senhaJaCriptografada) {
        super(id, nome, email, senha, telefone, criptografiaService, senhaJaCriptografada);
        this.profissao = profissao;
        this.descricao = descricao;
        this.cidade = cidade;
        this.valorHora = valorHora;
        this.servicos = new ArrayList<>();
        this.avaliacoesRecebidas = new ArrayList<>();
    }

    public String getProfissao() { return profissao; }
    public void setProfissao(String profissao) { this.profissao = profissao; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public double getValorHora() { return valorHora; }
    public void setValorHora(double valorHora) { this.valorHora = valorHora; }

    public List<String> getServicos() { return servicos; }

    public void cadastrarServico(String servico) {
        if (servico != null && !servico.isBlank() && !servicos.contains(servico)) {
            servicos.add(servico);
        }
    }

    // Associa um serviço ao prestador no banco de dados
    public void cadastrarServico(int idServico, double precoCobrado) {

        if (this.getId() <= 0) {
            throw new IllegalStateException(
                    "O prestador precisa estar cadastrado.");
        }

        if (idServico <= 0) {
            throw new IllegalArgumentException(
                    "O ID do serviço deve ser positivo.");
        }

        if (!Double.isFinite(precoCobrado) || precoCobrado < 0) {
            throw new IllegalArgumentException(
                    "O preço cobrado deve ser válido e não negativo.");
        }

        try {
            servicoDAO.associarPrestador(
                    this.getId(),
                    idServico,
                    precoCobrado
            );

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao cadastrar serviço do prestador.", e);
        }
    }


    public void calcularEAtualizarMedia() {

        if (this.getId() <= 0) {
            throw new IllegalStateException(
                    "O prestador precisa estar cadastrado.");
        }

        try {
            // Recupera a média diretamente do banco
            double media = avaliacaoDAO.calcularMedia(this.getId());
            super.atualizarNotaMedia(media);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao calcular a média das avaliações.", e);
        }
    }


    // Remover associação entre prestador e serviço
    public boolean removerServico(int idServico) {

        if (this.getId() <= 0) {
            throw new IllegalStateException(
                    "O prestador precisa estar cadastrado.");
        }

        if (idServico <= 0) {
            throw new IllegalArgumentException(
                    "O ID do serviço deve ser positivo.");
        }

        try {
            return servicoDAO.desassociarPrestador(
                    this.getId(),
                    idServico
            );

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao remover serviço do prestador.", e);
        }
    }

    //consulta os serviços reais e seus preços no banco
    public List<ServicoDAO.ServicoPrestado> listarServicosCadastrados() {
        try {
            return servicoDAO.listarServicosPorPrestador(this.getId());
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao consultar serviços no PostgreSQL.", e);
        }
    }

    public List<Avaliacao> listarAvaliacoesRecebidas() {
        try {
            return avaliacaoDAO.listarPorAvaliado(this.getId());

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao consultar avaliações recebidas.", e);
        }
    }


    public void receberAvaliacao(Avaliacao avaliacao) {

        if (avaliacao == null ||
                avaliacao.getId() <= 0 ||
                avaliacao.getIdAvaliado() != this.getId()) {

            throw new IllegalArgumentException(
                    "Avaliação inválida ou não pertence a este prestador.");
        }

        try {
            Avaliacao salva = avaliacaoDAO.buscarPorId(
                    avaliacao.getId()
            );

            if (salva == null ||
                    salva.getIdAvaliado() != this.getId()) {

                throw new IllegalArgumentException(
                        "Avaliação não encontrada para este prestador.");
            }

            // A média é recalculada usando o banco
            calcularEAtualizarMedia();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao verificar avaliação recebida.", e);
        }
    }




    @Override
    public void exibirPerfil() {
        if (this.getId() > 0) {
            calcularEAtualizarMedia();
        }
        super.exibirPerfil();
        System.out.println("Profissão: " + profissao);
        System.out.println("Descrição: " + descricao);
        System.out.println("Cidade: " + cidade);
        System.out.println("Valor/hora: R$ " + valorHora);
        System.out.println("Serviços: ");

        if (this.getId() > 0) {
            for (ServicoDAO.ServicoPrestado item : listarServicosCadastrados()) {
                System.out.println("- " + item.servico().getNome() + " | Preço cobrado: R$ " + item.precoCobrado());
            }
        }
    }


    // Avaliar um contratante e salvar no banco
    public Avaliacao avaliarContratante(Contratante contratante, int nota, String comentario) {

        if (contratante == null) {
            throw new IllegalArgumentException("O contratante não pode ser nulo.");
        }

        // Verifica se os usuários possuem IDs válidos
        if (this.getId() <= 0 || contratante.getId() <= 0) {
            throw new IllegalArgumentException("Os usuários devem estar cadastrados no banco.");
        }

        try {
            Avaliacao avaliacao = new Avaliacao(
                    0,
                    this.getId(),
                    contratante.getId(),
                    nota,
                    comentario,
                    LocalDate.now()
            );

            // Salvar no banco e recuperar o ID
            int idGerado = avaliacaoDAO.inserir(avaliacao);
            return new Avaliacao(
                    idGerado,
                    avaliacao.getIdAutor(),
                    avaliacao.getIdAvaliado(),
                    avaliacao.getNota(),
                    avaliacao.getComentario(),
                    avaliacao.getData()
            );

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao salvar avaliação do contratante.", e);
        }
    }


    // Listar avaliações realizadas pelo prestador
    public List<Avaliacao> listarMinhasAvaliacoes() {

        try {
            return avaliacaoDAO.listarPorAvaliador(this.getId());

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Erro ao consultar avaliações realizadas.", e);
        }
    }
}