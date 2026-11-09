package br.inatel.engsoftware;

import java.util.ArrayList;
import java.util.List;

import br.inatel.engsoftware.dao.AvaliacaoDAO;
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

    public void calcularEAtualizarMedia(){
        if(avaliacoesRecebidas.isEmpty()){
            super.atualizarNotaMedia(0.0);
            return;
        }

        double soma = 0.0;

        for (Avaliacao avaliacao : avaliacoesRecebidas) {
            soma += avaliacao.getNota();
        }

       super.atualizarNotaMedia(soma/avaliacoesRecebidas.size());

    }

    public void removerServico(String servico) {
        servicos.remove(servico);
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

        if (avaliacao != null && avaliacao.getIdAvaliado() == this.getId()) {
            avaliacoesRecebidas.add(avaliacao);
            calcularEAtualizarMedia();
        } else {
            // Fail-Fast: Lança erro se alguém tentar injetar uma avaliação inválida ou de outra pessoa
            throw new IllegalArgumentException("Avaliação inválida ou não pertence a este prestador.");
        }
    }

    @Override
    public void exibirPerfil() {
        super.exibirPerfil();
        System.out.println("Profissão: " + profissao);
        System.out.println("Descrição: " + descricao);
        System.out.println("Cidade: " + cidade);
        System.out.println("Valor/hora: R$ " + valorHora);
        System.out.println("Serviços: " + servicos);
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
            //Criar a avaliação
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