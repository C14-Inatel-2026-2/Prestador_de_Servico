package br.inatel.engsoftware;

import java.util.ArrayList;
import java.util.List;

import br.inatel.engsoftware.dao.ServicoDAO;
import java.sql.SQLException;
import java.util.Objects;


public class Prestador extends Usuario {

    private String profissao;
    private String descricao;
    private double valorHora;
    private final List<String> servicos;

    private ServicoDAO servicoDAO = new ServicoDAO();

    void definirServicoDAO(ServicoDAO servicoDAO) {
        this.servicoDAO = Objects.requireNonNull(servicoDAO);
    }

    //Usado para CADASTRAR um prestador novo (a senha vem em texto puro e vira hash)
    public Prestador(int id, String nome, String email, String senha,
                     String telefone, CriptografiaService criptografiaService,String cidade,
                     String profissao, String descricao,double valorHora) {
        this(id, nome, email, senha, telefone, criptografiaService, cidade, profissao, descricao, valorHora, false);
    }

    //Usado para a persistencia dos dados no banco, USO EXCLUSIVO DO UsuarioDAO: carrega um prestador do banco.
    public Prestador(int id, String nome, String email, String senha, String telefone,
                     CriptografiaService criptografiaService, String cidade,
                     String profissao, String descricao, double valorHora, boolean senhaJaCriptografada) {
        super(id, nome, email, senha, telefone, criptografiaService, cidade, senhaJaCriptografada);
        this.profissao = profissao;
        this.descricao = descricao;
        this.valorHora = valorHora;
        this.servicos = new ArrayList<>();
    }

    public String getProfissao() {
        return profissao;
    }

    public void setProfissao(String profissao) {
        this.profissao = profissao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getValorHora() {
        return valorHora;
    }

    public void setValorHora(double valorHora) {
        this.valorHora = valorHora;
    }

    public List<String> getServicos() {
        return servicos;
    }

    public void cadastrarServico(String servico) {
        if (servico != null && !servico.isBlank() && !servicos.contains(servico)) {
            servicos.add(servico);
        }
    }

    // Associa um serviço ao prestador no banco de dados
    public void cadastrarServico(int idServico, double precoCobrado) {
        validarServicoDoPrestador(idServico);

        if (!Double.isFinite(precoCobrado) || precoCobrado < 0) {
            throw new IllegalArgumentException(
                    "O preço cobrado deve ser válido e não negativo.");
        }

        executarNoBanco("Erro ao cadastrar serviço do prestador.",
                () -> servicoDAO.associarPrestador(this.getId(), idServico, precoCobrado));
    }


    // Remover associação entre prestador e serviço
    public boolean removerServico(int idServico) {

        validarServicoDoPrestador(idServico);

        return consultarNoBanco("Erro ao remover serviço do prestador.",
                () -> servicoDAO.desassociarPrestador(this.getId(), idServico));
    }

    private void validarServicoDoPrestador(int idServico) {
        if (this.getId() <= 0) {
            throw new IllegalStateException(
                    "O prestador precisa estar cadastrado.");
        }
        if (idServico <= 0) {
            throw new IllegalArgumentException(
                    "O ID do serviço deve ser positivo.");
        }
    }

    //consulta os serviços reais e seus preços no banco
    public List<ServicoDAO.ServicoPrestado> listarServicosCadastrados() {
        return consultarNoBanco("Erro ao consultar serviços no banco.",
                () -> servicoDAO.listarServicosPorPrestador(this.getId()));
    }

    // Operação no banco que devolve um valor. Existe porque a SQLException é checada
    // e não pode ser lançada de dentro de uma lambda comum (Supplier, Function...).
    @FunctionalInterface
    private interface ConsultaNoBanco<T> {
        T executar() throws SQLException;
    }

    // Operação no banco que não devolve nada.
    @FunctionalInterface
    private interface AcaoNoBanco {
        void executar() throws SQLException;
    }

    // Regra única de erro de banco do Prestador: SQLException vira IllegalStateException
    // com uma mensagem que diz o que estava sendo feito.
    private <T> T consultarNoBanco(String mensagemDeErro, ConsultaNoBanco<T> consulta) {
        try {
            return consulta.executar();
        } catch (SQLException e) {
            throw new IllegalStateException(mensagemDeErro, e);
        }
    }

    private void executarNoBanco(String mensagemDeErro, AcaoNoBanco acao) {
        consultarNoBanco(mensagemDeErro, () -> {
            acao.executar();
            return null;
        });
    }

    public Avaliacao avaliarContratante(Contratante contratante, int nota, String comentario) {
        return registrarAvaliacao(contratante, nota, comentario);
    }


    @Override
    public void exibirPerfil() {
        super.exibirPerfil();
        System.out.println("Profissão: " + profissao);
        System.out.println("Descrição: " + descricao);
        System.out.println("Valor/hora: R$ " + valorHora);
        System.out.println("Serviços: ");
        imprimirServicosCadastrados();
    }

    private void imprimirServicosCadastrados() {
        if (this.getId() <= 0) {
            return;
        }
        try {
            for (ServicoDAO.ServicoPrestado item : listarServicosCadastrados()) {
                System.out.println("- " + item.servico().getNome() + " | Preço cobrado: R$ " + item.precoCobrado());
            }
        } catch (IllegalStateException e) {
            System.out.println("(Aviso: não foi possível carregar os serviços agora.)");
        }
    }
}


