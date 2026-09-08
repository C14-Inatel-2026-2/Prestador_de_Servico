package br.inatel.engsoftware;

import java.util.ArrayList;
import java.util.List;

public class Prestador extends Usuario {

    private String profissao;
    private String descricao;
    private String cidade;
    private double valorHora;
    private final List<String> servicos;
    private final List<Avaliacao> avaliacoesRecebidas;

    public Prestador(int id, String nome, String email, String senha, String telefone,
                     CriptografiaService criptografiaService,
                     String profissao, String descricao, String cidade, double valorHora) {
        super(id, nome, email, senha, telefone, criptografiaService);
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
        return avaliacoesRecebidas;
    }

    public void receberAvaliacao(Avaliacao avaliacao) {
        // Verifica se não é nula, se passou na validação da própria avaliação e se pertence a este prestador
        if (avaliacao != null && avaliacao.validar() && avaliacao.getIdAvaliado() == this.getId()) {
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
}