package br.inatel.engsoftware;
import java.util.Objects;

import br.inatel.engsoftware.dao.AvaliacaoDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public abstract class Usuario {

    private int id;
    private String nome;
    private String email;
    private String senhaHash;
    private String telefone;
    private String cidade;
    private double notamedia;

    private final CriptografiaService criptografiaService;

    // estavam repetido em contratante e prestador
    private AvaliacaoDAO avaliacaoDAO = new AvaliacaoDAO();


    public Usuario(int id, String nome, String email, String senha, String telefone, CriptografiaService criptografiaService,String cidade) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.criptografiaService = criptografiaService;
        this.cidade = cidade;
        this.senhaHash = this.criptografiaService.gerarHash(senha);
    }

    protected Usuario(int id, String nome, String email, String senha, String telefone, CriptografiaService criptografiaService,String cidade ,boolean senhaJaCriptografada) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.cidade = cidade;
        this.criptografiaService = Objects.requireNonNull(criptografiaService);
        this.senhaHash = senhaJaCriptografada
                ? Objects.requireNonNull(senha)
                : this.criptografiaService.gerarHash(senha);
    }

    void definirAvaliacaoDAO(AvaliacaoDAO avaliacaoDAO) {
        this.avaliacaoDAO = Objects.requireNonNull(avaliacaoDAO);
    }

    public int getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.nome = cidade; }

    public double getNotamedia() { return notamedia; }

    public String getSenhaHash() { return senhaHash; }

    public boolean login(String emailDigitado, String senhaDigitada) {
        boolean emailCorreto = this.email.equals(emailDigitado);
        boolean senhaCorreta = this.criptografiaService.verificarSenha(senhaDigitada, this.senhaHash);
        return emailCorreto && senhaCorreta;
    }

    public boolean alterarSenha(String senhaAtual, String novaSenha) {
        if (!this.criptografiaService.verificarSenha(senhaAtual, this.senhaHash)) {
            return false;
        }
        this.senhaHash = this.criptografiaService.gerarHash(novaSenha);
        return true;
    }

    public void atualizarNotaMedia(double notamedia) {
        // NaN passava pela checagem antiga (NaN < 0 e NaN > 5 são ambos false)
        if (Double.isNaN(notamedia) || notamedia < 0 || notamedia > 5) {
            throw new IllegalArgumentException("A nota média deve estar entre 0 e 5.");
        }
        this.notamedia = notamedia;
    }

    public void definirIdPersistido(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }
        this.id = id;
    }

    protected final Avaliacao registrarAvaliacao(Usuario avaliado, int nota, String comentario) {
        if (avaliado == null) {
            throw new IllegalArgumentException("O usuário avaliado não pode ser nulo.");
        }
        if (this.id <= 0 || avaliado.getId() <= 0) {
            throw new IllegalArgumentException("Os usuários precisam estar cadastrados.");
        }

        // Fora do try: dado inválido faz a Avaliacao lançar IllegalArgumentException
        // com a lista de erros, e nada vai para o banco.
        Avaliacao avaliacao = new Avaliacao(0, this.id, avaliado.getId(),
                nota, comentario, LocalDate.now());

        int idGerado;
        try {
            idGerado = avaliacaoDAO.inserir(avaliacao);
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao salvar avaliação no banco de dados.", e);
        }

        Avaliacao salva = new Avaliacao(idGerado, avaliacao.getIdAutor(), avaliacao.getIdAvaliado(),
                avaliacao.getNota(), avaliacao.getComentario(), avaliacao.getData());

        //  a avaliação já está no banco; se só a média falhar, a mensagem diz isso
        try {
            avaliado.receberAvaliacao(salva);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("A avaliação foi salva (id " + idGerado
                    + "), mas não foi possível atualizar a média do avaliado.", e);
        }
        return salva;
    }

    //estava só no Prestador; agora o Contratante também tem
    public void receberAvaliacao(Avaliacao avaliacao) {
        if (avaliacao == null || avaliacao.getId() <= 0 || avaliacao.getIdAvaliado() != this.id) {
            throw new IllegalArgumentException("Avaliação inválida ou não pertence a este usuário.");
        }

        Avaliacao salva;
        try {
            salva = avaliacaoDAO.buscarPorId(avaliacao.getId());
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao verificar avaliação recebida.", e);
        }
        if (salva == null || salva.getIdAvaliado() != this.id) {
            throw new IllegalArgumentException("Avaliação não encontrada para este usuário.");
        }

        calcularEAtualizarMedia();
    }

    public void calcularEAtualizarMedia() {
        if (this.id <= 0) {
            throw new IllegalStateException("O usuário precisa estar cadastrado.");
        }
        try {
            atualizarNotaMedia(avaliacaoDAO.calcularMedia(this.id));
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao calcular a nota média.", e);
        }
    }

    public List<Avaliacao> listarAvaliacoesRecebidas() {
        try {
            return avaliacaoDAO.listarPorAvaliado(this.id);
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao consultar avaliações recebidas.", e);
        }
    }

    public List<Avaliacao> listarMinhasAvaliacoes() {
        try {
            return avaliacaoDAO.listarPorAvaliador(this.id);
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao consultar avaliações realizadas.", e);
        }
    }

    public void exibirPerfil() {
        if (this.id > 0) {
            try {
                calcularEAtualizarMedia();
            } catch (IllegalStateException e) {
                System.out.println("(Aviso: não foi possível atualizar a nota média agora.)");
            }
        }
        System.out.println("Perfil do Usuário");
        System.out.println("ID: " + id);
        System.out.println("Nome: " + nome);
        System.out.println("Email: " + email);
        System.out.println("Telefone: " + telefone);
        System.out.println("Cidade: " + cidade);
        System.out.println("Nota: " + notamedia);
    }
}