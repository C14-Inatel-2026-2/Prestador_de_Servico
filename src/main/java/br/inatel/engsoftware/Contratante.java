package br.inatel.engsoftware;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Contratante extends Usuario {

    private String cidade;
    private List<Avaliacao> minhasAvaliacoes;

    //Usado para CADASTRAR um contratante novo (a senha vem em texto puro e vira hash)
    public Contratante(int id, String nome, String email, String senha,
                       String telefone, CriptografiaService criptografiaService,
                       String cidade) {
        this(id, nome, email, senha, telefone,
                criptografiaService, cidade, false);
    }

    //Usado para a persistencia dos dados no banco, USO EXCLUSIVO DO UsuarioDAO: carrega um contratante do banco.
    public Contratante(int id, String nome, String email, String senha, String telefone,
                       CriptografiaService criptografiaService, String cidade, boolean senhaJaCriptografada) {
        super(id, nome, email, senha, telefone, criptografiaService, senhaJaCriptografada );
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


            this.minhasAvaliacoes.add(avaliacao);
            return avaliacao;

        } catch (IllegalArgumentException e){
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
