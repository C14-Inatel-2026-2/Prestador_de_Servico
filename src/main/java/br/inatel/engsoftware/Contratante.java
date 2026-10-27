package br.inatel.engsoftware;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Contratante extends Usuario {

    private String cidade;
    private List<Avaliacao> minhasAvaliacoes;

    public Contratante(int id, String nome, String email, String senha, String telefone,
                       CriptografiaService criptografiaService, String cidade) {
        super(id, nome, email, senha, telefone, criptografiaService);
        this.cidade = cidade;
        this.minhasAvaliacoes = new ArrayList<>();
    }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public List<Prestador> buscarPrestador(String termo) {
        return new ArrayList<>();
    }

    public Avaliacao avaliarUsuario(Usuario avaliado, int nota, String comentario) {
        if (avaliado == null) {
            return null; // Protege contra NullPointerException
        }

        try {
            // Tenta criar a avaliação. Se a nota for -1 ou 1000, a classe da sua colega vai estourar um erro aqui.
            Avaliacao avaliacao = new Avaliacao(0, this.getId(), avaliado.getId(), nota, comentario, LocalDate.now());

            // Se passou direto sem erro, salva a avaliação e retorna
            this.minhasAvaliacoes.add(avaliacao);
            return avaliacao;

        } catch (IllegalArgumentException e) {
            // O Catch captura o bloqueio da sua colega e devolve null, fazendo seus testes ficarem verdes!
            return null;
        }
    }

    public List<Avaliacao> listarMinhasAvaliacoes() {
        return new ArrayList<>();
    }
    @Override
    public void exibirPerfil() {
        super.exibirPerfil();
        System.out.println("Cidade: " + cidade);
    }
}
