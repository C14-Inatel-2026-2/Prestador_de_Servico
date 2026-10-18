package br.inatel.engsoftware;

public class Servico {

    private int id;
    private String nome;
    private String descricao;
    private double precoSugerido;

    public Servico(int id, String nome, String descricao, double precoSugerido) {
        if (precoSugerido < 0) {
            throw new IllegalArgumentException("O preço sugerido não pode ser negativo.");
        }
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.precoSugerido = precoSugerido;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public double getPrecoSugerido() { return precoSugerido; }
    public void setPrecoSugerido(double precoSugerido) {
        if (precoSugerido < 0) {
            throw new IllegalArgumentException("O preço sugerido não pode ser negativo.");
        }
        this.precoSugerido = precoSugerido;
    }

    public boolean validar() {
        return nome != null && !nome.isBlank() &&
                precoSugerido >= 0;
    }

    public String formatar() {
        return nome + " - R$ " + precoSugerido;
    }
}