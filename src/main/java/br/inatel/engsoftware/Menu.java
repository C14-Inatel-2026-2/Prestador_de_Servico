package br.inatel.engsoftware;

import br.inatel.engsoftware.dao.ServicoDAO;
import br.inatel.engsoftware.dao.UsuarioDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Menu {

    private final Scanner entrada = new Scanner(System.in);
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ServicoDAO servicoDAO = new ServicoDAO();
    private final CriptografiaService criptografia = new BCryptService();

    private Usuario logado = null;

    public void executar() {
        boolean rodando = true;

        while (rodando) {
            exibirMenu();
            int opcao = lerInteiro("Escolha uma opcao: ");

            if (opcao == 0) {
                rodando = false;
            } else {
                executarOpcao(opcao);
            }
        }

        System.out.println("Ate logo!");
    }

    private void exibirMenu() {
        System.out.println();
        System.out.println("====== MENU ======");
        System.out.println("Usuario logado: " + (logado == null ? "ninguem" : logado.getNome()));
        System.out.println("1 - Cadastrar contratante");
        System.out.println("2 - Cadastrar prestador");
        System.out.println("3 - Login");
        System.out.println("4 - Ver meu perfil");
        System.out.println("5 - (contratante) Buscar prestador");
        System.out.println("6 - (contratante) Avaliar prestador");
        System.out.println("7 - (prestador) Avaliar contratante");
        System.out.println("8 - (prestador) Cadastrar / listar servicos");
        System.out.println("9 - Ver avaliacoes recebidas e feitas");
        System.out.println("0 - Sair");
    }

    // Tratamento de erros:
    // IllegalArgumentException: mostra a mensagem e pede os dados de novo.
    // IllegalStateException: mostra a mensagem e volta ao menu.
    // SQLException: mostra a mensagem de erro do banco e volta ao menu.
    // NumberFormatException: tratada em lerInteiro e lerDouble, que pedem de novo.
    private void executarOpcao(int opcao) {
        boolean tentarNovamente;

        do {
            tentarNovamente = false;

            try {
                switch (opcao) {
                    case 1 -> cadastrarContratante();
                    case 2 -> cadastrarPrestador();
                    case 3 -> login();
                    case 4 -> verPerfil();
                    case 5 -> buscarPrestador();
                    case 6 -> avaliarPrestador();
                    case 7 -> avaliarContratante();
                    case 8 -> cadastrarEListarServicos();
                    case 9 -> verAvaliacoes();
                    default -> System.out.println("Opcao invalida.");
                }
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

    private void login() throws SQLException {
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

    private void verPerfil() {
        if (logado == null) {
            System.out.println("Faca login para ver o perfil.");
            return;
        }
        logado.exibirPerfil();
    }

    private void buscarPrestador() {
        if (!(logado instanceof Contratante contratante)) {
            System.out.println("Faca login como contratante para buscar prestadores.");
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
            System.out.println("Faca login como contratante para avaliar prestadores.");
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

    private void avaliarContratante() throws SQLException {
        if (!(logado instanceof Prestador prestador)) {
            System.out.println("Faca login como prestador para avaliar contratantes.");
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
            System.out.println("Faca login como prestador para gerenciar servicos.");
            return;
        }

        servicoDAO.listarTodos().forEach(
                s -> System.out.println("ID " + s.getId() + " - " + s.formatar()));

        int idServico = lerInteiro("ID do servico que voce oferece (0 para voltar): ");
        if (idServico == 0) {
            return;
        }

        double preco = lerDouble("Preco cobrado: ");
        prestador.cadastrarServico(idServico, preco);
        System.out.println("Servico cadastrado com sucesso.");
    }

    private void verAvaliacoes() {
        if (logado == null) {
            System.out.println("Faca login para ver as avaliacoes.");
            return;
        }

        System.out.println("--- Avaliacoes recebidas ---");
        for (Avaliacao avaliacao : logado.listarAvaliacoesRecebidas()) {
            System.out.println(avaliacao.formatar());
        }

        System.out.println("--- Avaliacoes feitas ---");
        for (Avaliacao avaliacao : logado.listarMinhasAvaliacoes()) {
            System.out.println(avaliacao.formatar());
        }
    }

    // Auxiliares

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

    // Entrada de dados

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
