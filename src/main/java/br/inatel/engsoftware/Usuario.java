package br.inatel.engsoftware;
import java.util.Objects;

import br.inatel.engsoftware.dao.AvaliacaoDAO;
import br.inatel.engsoftware.dao.ServicoDAO;

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
    private ServicoDAO servicoDAO = new ServicoDAO();

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

    // ===================== INJEÇÃO DE DEPENDÊNCIA [MUDANÇA 1] =====================

    public void definirAvaliacaoDAO(AvaliacaoDAO avaliacaoDAO) {
        this.avaliacaoDAO = Objects.requireNonNull(avaliacaoDAO);
    }

    public void definirServicoDAO(ServicoDAO servicoDAO) {
        this.servicoDAO = Objects.requireNonNull(servicoDAO);
    }

    // protected: só as filhas usam o ServicoDAO (cada uma para uma coisa diferente)
    protected ServicoDAO getServicoDAO() {
        return servicoDAO;
    }

    public int getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

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

    public void exibirPerfil() {
        System.out.println("Perfil do Usuário");
        System.out.println("ID: " + id);
        System.out.println("Nome: " + nome);
        System.out.println("Email: " + email);
        System.out.println("Telefone: " + telefone);
        System.out.println("Nota: " + notamedia);
    }

    public void definirIdPersistido(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }
        this.id = id;
    }

}