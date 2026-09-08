package br.inatel.engsoftware;

import java.time.LocalDate;

public class Avaliacao {

    private int id;
    private int idAutor;
    private int idAvaliado;
    private int nota;
    private String comentario;
    private LocalDate data;

    public Avaliacao(int id,int idAutor, int idAvaliado, int nota, String comentario, LocalDate data) {
        this.id = id;
        this.idAutor = idAutor;
        this.idAvaliado = idAvaliado;
        this.nota = nota;
        this.comentario = comentario;
        this.data = data;
    }

    public int getId() { return id; }
    public int getIdAutor() { return idAutor; }
    public int getIdAvaliado() { return idAvaliado; }
    public int getNota() { return nota; }
    public String getComentario() { return comentario; }
    public LocalDate getData() { return data; }


    public boolean validar() {
        return nota >= 1 && nota <= 5
                && idAutor != idAvaliado
                && data != null;
    }

    public String formatar() {
        return "[" + data + "] Nota " + nota + "/5 - "
                + (comentario == null || comentario.isBlank() ? "sem comentario" : comentario);
    }
}
