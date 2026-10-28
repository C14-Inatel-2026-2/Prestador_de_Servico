package br.inatel.engsoftware;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Avaliacao {
    public static final int NOTA_MINIMA = 1;
    public static final int NOTA_MAXIMA = 5;
    public static final int TAMANHO_MAXIMO_COMENTARIO = 500;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int id;
    private int idAutor;
    private int idAvaliado;
    private int nota;
    private String comentario;
    private LocalDate data;


    public Avaliacao(int id,int idAutor, int idAvaliado, int nota, String comentario, LocalDate data) {
        List<String> erros = coletarErros(idAutor, idAvaliado, nota, comentario, data);
        if (!erros.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", erros));
        }
        this.id = id;
        this.idAutor = idAutor;
        this.idAvaliado = idAvaliado;
        this.nota = nota;
        this.comentario = comentario;
        this.data = data;
    }

    private static List<String> coletarErros(int idAutor, int idAvaliado, int nota, String comentario, LocalDate data) {
        List<String> erros = new ArrayList<>();

        if (nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            erros.add("Nota deve estar entre " + NOTA_MINIMA + " e " + NOTA_MAXIMA);
        }
        if (idAutor <= 0) {
            erros.add("Id do autor deve ser positivo");
        }
        if (idAvaliado <= 0) {
            erros.add("Id do avaliado deve ser positivo");
        }
        if (idAutor == idAvaliado) {
            erros.add("Um usuário não pode avaliar a si mesmo");
        }
        if (data == null) {
            erros.add("Data é obrigatória");
        } else if (data.isAfter(LocalDate.now())) {
            erros.add("Data não pode estar no futuro");
        } else if(data.isBefore(LocalDate.now())){
            erros.add("Data não pode estar no passado");
        }
        if (comentario != null && comentario.length() > TAMANHO_MAXIMO_COMENTARIO) {
            erros.add("Comentário deve ter no máximo " + TAMANHO_MAXIMO_COMENTARIO + " caracteres");
        }
        return erros;
    }

    public int getId() { return id; }
    public int getIdAutor() { return idAutor; }
    public int getIdAvaliado() { return idAvaliado; }
    public int getNota() { return nota; }
    public String getComentario() { return comentario; }
    public LocalDate getData() { return data; }

    public String formatar() {
        String texto = (comentario == null || comentario.isBlank()) ? "sem comentario" : comentario.strip();// tira os espaços das extremidades
        return "[" + data.format(FORMATO_DATA) + "] Nota " + nota + "/" + NOTA_MAXIMA + " - " + texto;
    }
}
