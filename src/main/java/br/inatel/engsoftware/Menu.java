package br.inatel.engsoftware;

import br.inatel.engsoftware.dao.ServicoDAO;
import br.inatel.engsoftware.dao.UsuarioDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Menu {

    @FunctionalInterface
    private interface Acao {
        void executar() throws SQLException;
    }

    private final Scanner entrada = new Scanner(System.in);
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ServicoDAO servicoDAO = new ServicoDAO();
    private final CriptografiaService criptografia = new BCryptService();

    private Usuario logado = null;

    //  MENU PRINCIPAL

    public void executar() {
        boolean rodando = true;

        while (rodando) {
            System.out.println();
            System.out.println("========== MENU PRINCIPAL ==========");
            System.out.println("1 - Cadastrar usuario");
            System.out.println("2 - Fazer login");
            System.out.println("0 - Sair");

            int opcao = lerInteiro("Escolha uma opcao: ");

            switch (opcao) {
                case 1 -> menuCadastro();
                case 2 -> {
                    executarComTratamento(this::fazerLogin);
                    if (logado instanceof Contratante) {
                        menuContratante();
                    } else if (logado instanceof Prestador) {
                        menuPrestador();
                    }
                }
                case 0 -> rodando = false;
                default -> System.out.println("Opcao invalida.");
            }
        }

        System.out.println("Ate logo!");
    }

    //  CADASTRO

    private void menuCadastro() {
        boolean voltar = false;

        while (!voltar) {
            System.out.println();
            System.out.println("========== CADASTRAR USUARIO ==========");
            System.out.println("1 - Contratante");
            System.out.println("2 - Prestador");
            System.out.println("0 - Voltar");

            int opcao = lerInteiro("Escolha o tipo de usuario: ");

            switch (opcao) {
                case 1 -> executarComTratamento(this::cadastrarContratante);
                case 2 -> executarComTratamento(this::cadastrarPrestador);
                case 0 -> voltar = true;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    //  MENU DO CONTRATANTE

    private void menuContratante() {
        boolean voltar = false;

        while (!voltar) {
            System.out.println();
            System.out.println("========== MENU CONTRATANTE ==========");
            System.out.println("Logado como: " + logado.getNome());
            System.out.println("1 - Buscar prestador");
            System.out.println("2 - Avaliar prestador");
            System.out.println("3 - Ver meu perfil");
            System.out.println("4 - Ver avaliacoes recebidas e feitas");
            System.out.println("0 - Voltar (sair da conta)");

            int opcao = lerInteiro("Escolha uma opcao: ");

            switch (opcao) {
                case 1 -> executarComTratamento(this::buscarPrestador);
                case 2 -> executarComTratamento(this::avaliarPrestador);
                case 3 -> executarComTratamento(this::verPerfil);
                case 4 -> executarComTratamento(this::verAvaliacoes);
                case 0 -> {
                    logado = null;
                    voltar = true;
                    System.out.println("Voce saiu da conta.");
                }
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    // MENU DO PRESTADOR

    private void menuPrestador() {
        boolean voltar = false;

        while (!voltar) {
            System.out.println();
            System.out.println("========== MENU PRESTADOR ==========");
            System.out.println("Logado como: " + logado.getNome());
            System.out.println("1 - Ver meu perfil");
            System.out.println("2 - Cadastrar / listar servicos");
            System.out.println("3 - Avaliar contratante");
            System.out.println("4 - Ver avaliacoes recebidas e feitas");
            System.out.println("0 - Voltar (sair da conta)");

            int opcao = lerInteiro("Escolha uma opcao: ");

            switch (opcao) {
                case 1 -> executarComTratamento(this::verPerfil);
                case 2 -> executarComTratamento(this::cadastrarEListarServicos);
                case 3 -> executarComTratamento(this::avaliarContratante);
                case 4 -> executarComTratamento(this::verAvaliacoes);
                case 0 -> {
                    logado = null;
                    voltar = true;
                    System.out.println("Voce saiu da conta.");
                }
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    //  TRATAMENTO DE ERROS

    private void executarComTratamento(Acao acao) {
        boolean tentarNovamente;

        do {
            tentarNovamente = false;

            try {
                acao.executar();
            } catch (IllegalArgumentException e) {
                System.out.println("Dado invalido: " + e.getMessage());
                System.out.println("Digite novamente.");
                tentarNovamente = true;
            } catch (IllegalStateException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Erro no banco de dados: " + e.getMessage());
            }
        } while (tentarNovamente);
    }

    // AÇÕES: CADASTRO E LOGIN

    private void cadastrarContratante() throws SQLException {
        System.out.println("--- Cadastro de contratante ---");
        String nome = lerTexto("Nome: ");
        String email = lerTexto("Email: ");
        String senha = lerTexto("Senha: ");
        String telefone = lerTexto("Telefone: ");
        String cidade = lerTexto("Cidade: ");

        Contratante contratante = new Contratante(0, nome, email, senha, telefone, criptografia, cidade);
        usuarioDAO.inserir(contratante);

        System.out.println("Contratante cadastrado com ID " + contratante.getId() + ".");
    }

    private void cadastrarPrestador() throws SQLException {
        System.out.println("--- Cadastro de prestador ---");
        String nome = lerTexto("Nome: ");
        String email = lerTexto("Email: ");
        String senha = lerTexto("Senha: ");
        String telefone = lerTexto("Telefone: ");
        String cidade = lerTexto("Cidade: ");
        String profissao = lerTexto("Profissao: ");
        String descricao = lerTexto("Descricao: ");
        double valorHora = lerDouble("Valor por hora: ");

        Prestador prestador = new Prestador(0, nome, email, senha, telefone, criptografia,
                cidade, profissao, descricao, valorHora);
        usuarioDAO.inserir(prestador);

        System.out.println("Prestador cadastrado com ID " + prestador.getId() + ".");
    }

    private void fazerLogin() throws SQLException {
        System.out.println("--- Login ---");
        String email = lerTexto("Email: ");
        String senha = lerTexto("Senha: ");

        Usuario usuario = usuarioDAO.buscarPorEmail(email);

        if (usuario != null && usuario.login(email, senha)) {
            logado = usuario;
            System.out.println("Bem-vindo, " + usuario.getNome() + "!");
        } else {
            System.out.println("Email ou senha invalidos.");
        }
    }

    // AÇÕES DO CONTRATANTE

    private void buscarPrestador() {
        if (!(logado instanceof Contratante contratante)) {
            System.out.println("Acesso restrito a contratantes.");
            return;
        }

        List<Prestador> prestadores = buscarLista(contratante);
        if (prestadores.isEmpty()) {
            System.out.println("Nenhum prestador encontrado.");
            return;
        }

        exibirListaPrestadores(prestadores);
    }

    private void avaliarPrestador() {
        if (!(logado instanceof Contratante contratante)) {
            System.out.println("Acesso restrito a contratantes.");
            return;
        }

        List<Prestador> prestadores = buscarLista(contratante);
        if (prestadores.isEmpty()) {
            System.out.println("Nenhum prestador encontrado.");
            return;
        }

        exibirListaPrestadores(prestadores);
        int numero = lerInteiro("Numero do prestador: ");
        if (numero < 1 || numero > prestadores.size()) {
            throw new IllegalArgumentException("Escolha um numero da lista.");
        }

        Prestador escolhido = prestadores.get(numero - 1);
        int nota = lerInteiro("Nota (1 a 5): ");
        String comentario = lerTexto("Comentario: ");

        Avaliacao avaliacao = contratante.avaliarUsuario(escolhido, nota, comentario);
        System.out.println("Avaliacao registrada: " + avaliacao.formatar());
    }

    // AÇÕES DO PRESTADOR

    private void avaliarContratante() throws SQLException {
        if (!(logado instanceof Prestador prestador)) {
            System.out.println("Acesso restrito a prestadores.");
            return;
        }

        int idContratante = lerInteiro("ID do contratante: ");
        Usuario usuario = usuarioDAO.buscarPorId(idContratante);

        if (!(usuario instanceof Contratante contratante)) {
            throw new IllegalArgumentException("Contratante nao encontrado com esse ID.");
        }

        int nota = lerInteiro("Nota (1 a 5): ");
        String comentario = lerTexto("Comentario: ");

        Avaliacao avaliacao = prestador.avaliarContratante(contratante, nota, comentario);
        System.out.println("Avaliacao registrada: " + avaliacao.formatar());
    }

    private void cadastrarEListarServicos() throws SQLException {
        if (!(logado instanceof Prestador prestador)) {
            System.out.println("Acesso restrito a prestadores.");
            return;
        }

        List<Servico> servicos = servicoDAO.listarTodos();
        if (servicos.isEmpty()) {
            System.out.println("Nenhum servico cadastrado no sistema.");
            return;
        }

        for (Servico servico : servicos) {
            System.out.println("ID " + servico.getId() + " - " + servico.formatar());
        }

        int idServico = lerInteiro("ID do servico que voce oferece (0 para voltar): ");
        if (idServico == 0) {
            return;
        }

        double preco = lerDouble("Preco cobrado: ");
        prestador.cadastrarServico(idServico, preco);
        System.out.println("Servico cadastrado com sucesso.");
    }


    private void verPerfil() {
        logado.exibirPerfil();
    }

    private void verAvaliacoes() {
        System.out.println("--- Avaliacoes recebidas ---");
        for (Avaliacao avaliacao : logado.listarAvaliacoesRecebidas()) {
            System.out.println(avaliacao.formatar());
        }

        System.out.println("--- Avaliacoes feitas ---");
        for (Avaliacao avaliacao : logado.listarMinhasAvaliacoes()) {
            System.out.println(avaliacao.formatar());
        }
    }

    // AUXILIARES

    private List<Prestador> buscarLista(Contratante contratante) {
        String termo = lerTexto("Buscar por profissao ou servico: ");
        return contratante.buscarPrestador(termo);
    }

    private void exibirListaPrestadores(List<Prestador> prestadores) {
        for (int i = 0; i < prestadores.size(); i++) {
            Prestador p = prestadores.get(i);
            System.out.println((i + 1) + " - " + p.getNome() + " (" + p.getProfissao() + ")");
        }
    }

    //  ENTRADA DE DADOS

    private String lerTexto(String pergunta) {
        System.out.print(pergunta);
        return entrada.nextLine().trim();
    }

    private int lerInteiro(String pergunta) {
        while (true) {
            System.out.print(pergunta);
            try {
                return Integer.parseInt(entrada.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Digite um numero inteiro.");
            }
        }
    }

    private double lerDouble(String pergunta) {
        while (true) {
            System.out.print(pergunta);
            try {
                return Double.parseDouble(entrada.nextLine().trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Digite um numero valido (ex.: 150 ou 150,00).");
            }
        }
    }
}
