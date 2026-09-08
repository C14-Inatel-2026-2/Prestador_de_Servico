package br.inatel.engsoftware;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Contratante extends Usuario {

    private String cidade;

    public Contratante(int id, String nome, String email, String senha, String telefone, CriptografiaService criptografiaService, String cidade) {
        super(id, nome, email, senha, telefone, criptografiaService);
        this.cidade = cidade;
    }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public List<Prestador> buscarPrestador(String termo) {
        return new ArrayList<>();
    }
    public Avaliacao avaliarUsuario(Usuario avaliado, int nota, String comentario) {
        if (avaliado == null || avaliado.getId()== this.getId()) {
            return null;
        }
        Avaliacao avaliacao = new Avaliacao(0,this.getId(), avaliado.getId(), nota, comentario, LocalDate.now());
        if (!avaliacao.validar()) {
            return null;
        }
        return avaliacao;
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
