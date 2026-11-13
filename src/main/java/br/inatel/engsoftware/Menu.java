package br.inatel.engsoftware;

import br.inatel.engsoftware.dao.ServicoDAO;
import br.inatel.engsoftware.dao.UsuarioDAO;

import java.io.Console;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Menu {

    @FunctionalInterface
    private interface Acao {
        void executar() throws SQLException;
    }

    // Lançada quando o usuário digita 0 em um campo de texto, para cancelar a operação.
    private static class Cancelado extends RuntimeException {
        Cancelado() {
            super("operacao cancelada");
        }
    }

    // Lançada quando a entrada padrão acaba (por exemplo, execução sem terminal).
    private static class EntradaEncerrada extends RuntimeException {
        EntradaEncerrada() {
            super("entrada encerrada");
        }
    }

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int TAMANHO_MINIMO_SENHA = 6;
    private static final int ITENS_POR_PAGINA = 5;
    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final String LINHA = "=".repeat(52);

    private final Scanner entrada = new Scanner(System.in);
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ServicoDAO servicoDAO = new ServicoDAO();
    private final CriptografiaService criptografia = new BCryptService();

    private Usuario logado = null;

    //  MENU PRINCIPAL

    public void executar() {
        try {
            boolean rodando = true;

            while (rodando) {
                limparTela();
                cabecalho("Menu principal");
                System.out.println("Encontre prestadores e servicos de manutencao e obras.");
                System.out.println();
                System.out.println("1 - Cadastrar usuario");
                System.out.println("2 - Fazer login");
                System.out.println("0 - Sair");
                System.out.println();

                int opcao = lerOpcao("Escolha uma opcao: ");

                switch (opcao) {
                    case 1 -> menuCadastro();
                    case 2 -> {
                        executarAcao(this::fazerLogin);
                        if (logado instanceof Contratante) {
                            menuContratante();
                        } else if (logado instanceof Prestador) {
                            menuPrestador();
                        }
                    }
                    case 0 -> rodando = false;
                    default -> aviso("Opcao invalida. Escolha um numero da lista.");
                }
            }
        } catch (EntradaEncerrada e) {
            System.out.println();
            System.out.println("Entrada encerrada.");
        }

        System.out.println("Ate logo!");
    }


    //  CADASTRO

    private void menuCadastro() {
        boolean voltar = false;

        while (!voltar) {
            limparTela();
            cabecalho("Menu principal > Cadastrar usuario");
            System.out.println("1 - Contratante");
            System.out.println("2 - Prestador");
            System.out.println("0 - Voltar");
            System.out.println();

            int opcao = lerOpcao("Escolha o tipo de usuario: ");

            switch (opcao) {
                case 1 -> executarAcao(this::cadastrarContratante);
                case 2 -> executarAcao(this::cadastrarPrestador);
                case 0 -> voltar = true;
                default -> aviso("Opcao invalida.");
            }
        }
    }


    //  MENU DO CONTRATANTE


    private void menuContratante() {
        boolean voltar = false;

        while (!voltar) {
            limparTela();
            cabecalho("Contratante");
            System.out.println("1 - Buscar prestador");
            System.out.println("2 - Avaliar prestador");
            System.out.println("3 - Ver meu perfil");
            System.out.println("4 - Ver minhas avaliacoes");
            System.out.println("0 - Sair da conta");
            System.out.println();

            int opcao = lerOpcao("Escolha uma opcao: ");

            switch (opcao) {
                case 1 -> executarAcao(this::buscarPrestador);
                case 2 -> executarAcao(this::avaliarPrestador);
                case 3 -> executarAcao(this::verPerfil);
                case 4 -> executarAcao(this::verAvaliacoes);
                case 0 -> {
                    logado = null;
                    voltar = true;
                    sucesso("Voce saiu da conta.");
                    pausar();
                }
                default -> aviso("Opcao invalida.");
            }
        }
    }

    //  MENU DO PRESTADOR

    private void menuPrestador() {
        boolean voltar = false;

        while (!voltar) {
            limparTela();
            cabecalho("Prestador");
            System.out.println("1 - Ver meu perfil");
            System.out.println("2 - Meus servicos (listar e cadastrar)");
            System.out.println("3 - Avaliar contratante");
            System.out.println("4 - Ver minhas avaliacoes");
            System.out.println("0 - Sair da conta");
            System.out.println();

            int opcao = lerOpcao("Escolha uma opcao: ");

            switch (opcao) {
                case 1 -> executarAcao(this::verPerfil);
                case 2 -> executarAcao(this::gerenciarServicos);
                case 3 -> executarAcao(this::avaliarContratante);
                case 4 -> executarAcao(this::verAvaliacoes);
                case 0 -> {
                    logado = null;
                    voltar = true;
                    sucesso("Voce saiu da conta.");
                    pausar();
                }
                default -> aviso("Opcao invalida.");
            }
        }
    }
    //  TRATAMENTO DE ERROS E EXECUÇÃO DE AÇÕES
    // Executa uma ação do menu. Cada erro mostra uma mensagem clara e volta ao menu.
    // Os campos inválidos são pedidos de novo individualmente, dentro de cada ação.

    private void executarAcao(Acao acao) {
        try {
            acao.executar();
        } catch (Cancelado e) {
            aviso("Operacao cancelada.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            erro(e.getMessage());
        } catch (SQLException e) {
            erro(mensagemDoBanco(e));
        }
        pausar();
    }

    // Traduz erros do banco em mensagens que o usuário entende.
    private String mensagemDoBanco(SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            return "Este email ja esta cadastrado. Use outro email ou faca login.";
        }
        return "Nao foi possivel concluir a operacao no banco de dados. Tente novamente.";
    }

    //  AÇÕES: CADASTRO E LOGIN


    private void cadastrarContratante() throws SQLException {
        cabecalho("Cadastro de contratante");
        dica("Digite 0 em um campo de texto para cancelar.");

        String nome = lerObrigatorio("Nome: ");
        String email = lerEmail();
        String senha = lerSenhaNova();
        String telefone = lerTelefone();
        String cidade = lerObrigatorio("Cidade: ");

        confirmar("Confirmar cadastro de " + nome + "? (s/n): ");

        Contratante contratante = new Contratante(0, nome, email, senha, telefone, criptografia, cidade);
        usuarioDAO.inserir(contratante);

        sucesso("Contratante cadastrado com ID " + contratante.getId() + ". Agora faca login.");
    }

    private void cadastrarPrestador() throws SQLException {
        cabecalho("Cadastro de prestador");
        dica("Digite 0 em um campo de texto para cancelar.");

        String nome = lerObrigatorio("Nome: ");
        String email = lerEmail();
        String senha = lerSenhaNova();
        String telefone = lerTelefone();
        String cidade = lerObrigatorio("Cidade: ");
        String profissao = lerObrigatorio("Profissao (ex.: Encanador): ");
        String descricao = lerObrigatorio("Descricao dos servicos: ");
        double valorHora = lerValorPositivo("Valor por hora (R$): ");

        confirmar("Confirmar cadastro de " + nome + "? (s/n): ");

        Prestador prestador = new Prestador(0, nome, email, senha, telefone, criptografia,
                cidade, profissao, descricao, valorHora);
        usuarioDAO.inserir(prestador);

        sucesso("Prestador cadastrado com ID " + prestador.getId() + ". Agora faca login.");
    }

    private void fazerLogin() throws SQLException {
        cabecalho("Login");
        dica("Digite 0 no email para cancelar.");

        String email = lerCampo("Email: ");
        String senha = lerSenhaOculta("Senha: ");

        Usuario usuario = usuarioDAO.buscarPorEmail(email);

        if (usuario != null && usuario.login(email, senha)) {
            logado = usuario;
            sucesso("Bem-vindo, " + usuario.getNome() + "!");
        } else {
            erro("Email ou senha invalidos.");
        }
    }

    //  AÇÕES DO CONTRATANTE

    private void buscarPrestador() {
        if (!(logado instanceof Contratante contratante)) {
            erro("Acesso restrito a contratantes.");
            return;
        }

        cabecalho("Contratante > Buscar prestador");
        String termo = lerObrigatorio("Profissao ou servico (0 cancela): ");

        List<Prestador> prestadores = contratante.buscarPrestador(termo);
        if (prestadores.isEmpty()) {
            aviso("Nenhum prestador encontrado para \"" + termo + "\".");
            return;
        }

        System.out.println(prestadores.size() + " prestador(es) encontrado(s):");
        System.out.println();
        exibirPaginado(linhasPrestadores(prestadores));
    }

    private void avaliarPrestador() {
        if (!(logado instanceof Contratante contratante)) {
            erro("Acesso restrito a contratantes.");
            return;
        }

        cabecalho("Contratante > Avaliar prestador");
        String termo = lerObrigatorio("Buscar prestador por profissao ou servico (0 cancela): ");

        List<Prestador> prestadores = contratante.buscarPrestador(termo);
        if (prestadores.isEmpty()) {
            aviso("Nenhum prestador encontrado para \"" + termo + "\".");
            return;
        }

        System.out.println(prestadores.size() + " prestador(es) encontrado(s):");
        System.out.println();
        exibirPaginado(linhasPrestadores(prestadores));

        int numero = lerEscolha("Numero do prestador a avaliar (0 cancela): ", prestadores.size());
        Prestador escolhido = prestadores.get(numero - 1);

        System.out.println("Avaliando: " + escolhido.getNome());
        int nota = lerNota();
        String comentario = lerLinha("Comentario (opcional, Enter para pular): ");

        Avaliacao avaliacao = contratante.avaliarUsuario(escolhido, nota, comentario);
        sucesso("Avaliacao registrada: " + avaliacao.formatar());
    }


    //  AÇÕES DO PRESTADOR

    private void gerenciarServicos() throws SQLException {
        if (!(logado instanceof Prestador prestador)) {
            erro("Acesso restrito a prestadores.");
            return;
        }

        cabecalho("Prestador > Meus servicos");

        List<Servico> servicos = servicoDAO.listarTodos();
        if (servicos.isEmpty()) {
            aviso("Nenhum servico cadastrado no sistema ainda.");
            return;
        }

        List<ServicoDAO.ServicoPrestado> jaOferecidos = prestador.listarServicosCadastrados();
        if (!jaOferecidos.isEmpty()) {
            System.out.println("Servicos que voce ja oferece:");
            for (ServicoDAO.ServicoPrestado sp : jaOferecidos) {
                System.out.println("  ID " + sp.servico().getId() + " - " + sp.servico().getNome()
                        + " por " + moeda(sp.precoCobrado()));
            }
            System.out.println();
        }

        List<String> linhas = new ArrayList<>();
        for (Servico s : servicos) {
            String marca = estaOferecido(s, jaOferecidos) ? " [ja oferecido]" : "";
            linhas.add("ID " + s.getId() + " - " + s.getNome()
                    + " | " + s.getDescricao()
                    + " | sugerido " + moeda(s.getPrecoSugerido())
                    + marca);
        }
        exibirPaginado(linhas);

        Servico escolhido = escolherServico(servicos, jaOferecidos);
        double preco = lerPreco(escolhido.getPrecoSugerido());

        prestador.cadastrarServico(escolhido.getId(), preco);
        sucesso("Servico \"" + escolhido.getNome() + "\" cadastrado por " + moeda(preco) + ".");
    }

    private Servico escolherServico(List<Servico> servicos, List<ServicoDAO.ServicoPrestado> jaOferecidos) {
        while (true) {
            int id = lerInteiroCampo("ID do servico que voce oferece (0 cancela): ");
            Servico encontrado = null;
            for (Servico s : servicos) {
                if (s.getId() == id) {
                    encontrado = s;
                    break;
                }
            }

            if (encontrado == null) {
                aviso("ID nao encontrado na lista.");
            } else if (estaOferecido(encontrado, jaOferecidos)) {
                aviso("Voce ja oferece este servico. Escolha outro ID.");
            } else {
                return encontrado;
            }
        }
    }

    private boolean estaOferecido(Servico servico, List<ServicoDAO.ServicoPrestado> jaOferecidos) {
        for (ServicoDAO.ServicoPrestado sp : jaOferecidos) {
            if (sp.servico().getId() == servico.getId()) {
                return true;
            }
        }
        return false;
    }

    private void avaliarContratante() throws SQLException {
        if (!(logado instanceof Prestador prestador)) {
            erro("Acesso restrito a prestadores.");
            return;
        }

        cabecalho("Prestador > Avaliar contratante");
        dica("Peca ao contratante o ID dele, que aparece no cadastro.");

        int idContratante = lerInteiroCampo("ID do contratante (0 cancela): ");
        Usuario usuario = usuarioDAO.buscarPorId(idContratante);

        if (!(usuario instanceof Contratante contratante)) {
            throw new IllegalArgumentException("Nenhum contratante encontrado com este ID.");
        }

        System.out.println("Avaliando: " + contratante.getNome());
        int nota = lerNota();
        String comentario = lerLinha("Comentario (opcional, Enter para pular): ");

        Avaliacao avaliacao = prestador.avaliarContratante(contratante, nota, comentario);
        sucesso("Avaliacao registrada: " + avaliacao.formatar());
    }


    //  AÇÕES COMUNS


    private void verPerfil() {
        cabecalho("Meu perfil");
        logado.exibirPerfil();
    }

    private void verAvaliacoes() {
        cabecalho("Minhas avaliacoes");

        List<String> recebidas = formatarAvaliacoes(logado.listarAvaliacoesRecebidas());
        System.out.println("--- Recebidas (" + recebidas.size() + ") ---");
        if (recebidas.isEmpty()) {
            System.out.println("Nenhuma avaliacao recebida ainda.");
        } else {
            exibirPaginado(recebidas);
        }

        List<String> feitas = formatarAvaliacoes(logado.listarMinhasAvaliacoes());
        System.out.println();
        System.out.println("--- Feitas (" + feitas.size() + ") ---");
        if (feitas.isEmpty()) {
            System.out.println("Nenhuma avaliacao feita ainda.");
        } else {
            exibirPaginado(feitas);
        }
    }

    //  AUXILIARES DE EXIBIÇÃO


    private List<String> linhasPrestadores(List<Prestador> prestadores) {
        List<String> linhas = new ArrayList<>();
        for (int i = 0; i < prestadores.size(); i++) {
            Prestador p = prestadores.get(i);
            linhas.add((i + 1) + " - " + p.getNome()
                    + " | " + p.getProfissao()
                    + " | " + p.getCidade()
                    + " | " + moeda(p.getValorHora()) + "/h");
        }
        return linhas;
    }

    private List<String> formatarAvaliacoes(List<Avaliacao> avaliacoes) {
        List<String> linhas = new ArrayList<>();
        for (Avaliacao a : avaliacoes) {
            linhas.add(a.formatar());
        }
        return linhas;
    }

    // Mostra a lista em páginas. Enter avança, p volta e q sai.
    private void exibirPaginado(List<String> linhas) {
        int total = linhas.size();
        int totalPaginas = (total + ITENS_POR_PAGINA - 1) / ITENS_POR_PAGINA;
        int pagina = 0;

        while (true) {
            int inicio = pagina * ITENS_POR_PAGINA;
            int fim = Math.min(inicio + ITENS_POR_PAGINA, total);

            System.out.println("--- Pagina " + (pagina + 1) + " de " + totalPaginas + " ---");
            for (int i = inicio; i < fim; i++) {
                System.out.println(linhas.get(i));
            }

            if (totalPaginas <= 1) {
                System.out.println();
                return;
            }

            String resposta = lerLinha("[Enter] proxima  [p] anterior  [q] sair: ").toLowerCase(PT_BR);
            if (resposta.equals("q")) {
                System.out.println();
                return;
            } else if (resposta.equals("p")) {
                if (pagina > 0) {
                    pagina--;
                }
            } else if (pagina < totalPaginas - 1) {
                pagina++;
            } else {
                System.out.println();
                return;
            }
        }
    }

    private String moeda(double valor) {
        return String.format(PT_BR, "R$ %,.2f", valor);
    }

    private String tipoDe(Usuario usuario) {
        if (usuario instanceof Contratante) {
            return "Contratante";
        }
        if (usuario instanceof Prestador) {
            return "Prestador";
        }
        return "Usuario";
    }

    private void cabecalho(String titulo) {
        System.out.println();
        System.out.println(LINHA);
        System.out.println(" " + titulo);
        if (logado != null) {
            System.out.println(" Logado: " + logado.getNome() + " (" + tipoDe(logado) + ")");
        }
        System.out.println(LINHA);
    }

    private void aviso(String mensagem) {
        System.out.println("[AVISO] " + mensagem);
    }

    private void erro(String mensagem) {
        System.out.println("[ERRO] " + mensagem);
    }

    private void sucesso(String mensagem) {
        System.out.println("[OK] " + mensagem);
    }

    private void dica(String mensagem) {
        System.out.println("  " + mensagem);
        System.out.println();
    }

    private void limparTela() {
        try {
            if (System.getProperty("os.name", "").toLowerCase(PT_BR).contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (IOException e) {
            // Se não der para limpar a tela, o menu continua normalmente.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void pausar() {
        lerLinha("\nPressione Enter para continuar...");
    }

    //  ENTRADA DE DADOS

    // Lê uma linha. Se a entrada acabar, encerra o programa de forma limpa.
    private String lerLinha(String pergunta) {
        System.out.print(pergunta);
        try {
            return entrada.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new EntradaEncerrada();
        }
    }

    // Lê um campo de formulário. Digitar 0 cancela a operação.
    private String lerCampo(String pergunta) {
        String texto = lerLinha(pergunta);
        if (texto.equals("0")) {
            throw new Cancelado();
        }
        return texto;
    }

    // Pede o campo de novo enquanto ele estiver vazio, sem refazer o formulário.
    private String lerObrigatorio(String pergunta) {
        while (true) {
            String texto = lerCampo(pergunta);
            if (!texto.isEmpty()) {
                return texto;
            }
            aviso("Este campo e obrigatorio.");
        }
    }

    private String lerEmail() {
        while (true) {
            String email = lerCampo("Email: ");
            if (EMAIL.matcher(email).matches()) {
                return email;
            }
            aviso("Email invalido. Exemplo: nome@dominio.com");
        }
    }

    private String lerSenhaNova() {
        while (true) {
            String senha = lerSenhaOculta("Senha (minimo " + TAMANHO_MINIMO_SENHA + " caracteres): ");
            if (senha.length() < TAMANHO_MINIMO_SENHA) {
                aviso("A senha precisa ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
                continue;
            }

            String confirmacao = lerSenhaOculta("Confirme a senha: ");
            if (!senha.equals(confirmacao)) {
                aviso("As senhas nao conferem. Tente novamente.");
                continue;
            }
            return senha;
        }
    }

    // Esconde a senha
    private String lerSenhaOculta(String pergunta) {
        Console console = System.console();
        if (console != null) {
            System.out.print(pergunta);
            char[] senha = console.readPassword();
            return senha == null ? "" : new String(senha);
        }
        return lerLinha(pergunta);
    }

    private String lerTelefone() {
        while (true) {
            String telefone = lerCampo("Telefone com DDD (ex.: 35 99999-9999): ");
            int digitos = telefone.replaceAll("\\D", "").length();
            if (digitos == 10 || digitos == 11) {
                return telefone;
            }
            aviso("Telefone invalido. Informe DDD e numero (10 ou 11 digitos).");
        }
    }

    private int lerOpcao(String pergunta) {
        while (true) {
            String texto = lerLinha(pergunta);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                aviso("Digite apenas o numero da opcao.");
            }
        }
    }

    private int lerInteiroCampo(String pergunta) {
        while (true) {
            String texto = lerCampo(pergunta);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                aviso("Digite um numero inteiro.");
            }
        }
    }

    // Lê um número de uma lista de 1 até max. Digitar 0 cancela.
    private int lerEscolha(String pergunta, int max) {
        while (true) {
            int numero = lerInteiroCampo(pergunta);
            if (numero >= 1 && numero <= max) {
                return numero;
            }
            aviso("Escolha um numero entre 1 e " + max + ".");
        }
    }

    private int lerNota() {
        return lerEscolha("Nota (1 a 5, 0 cancela): ", 5);
    }

    private double lerValorPositivo(String pergunta) {
        while (true) {
            String texto = lerCampo(pergunta);
            Double valor = converterDecimal(texto);
            if (valor == null) {
                aviso("Digite um valor numerico, por exemplo 150 ou 150,00.");
            } else if (valor <= 0) {
                aviso("O valor deve ser maior que zero.");
            } else {
                return valor;
            }
        }
    }

    // Enter aceita o preço sugerido. Digitar 0 cancela.
    private double lerPreco(double sugerido) {
        while (true) {
            String texto = lerLinha("Preco cobrado (Enter para " + moeda(sugerido) + ", 0 cancela): ");
            if (texto.equals("0")) {
                throw new Cancelado();
            }
            if (texto.isEmpty()) {
                return sugerido;
            }
            Double valor = converterDecimal(texto);
            if (valor == null || valor < 0) {
                aviso("Digite um valor valido, por exemplo 150 ou 150,00.");
            } else {
                return valor;
            }
        }
    }

    private Double converterDecimal(String texto) {
        try {
            return Double.parseDouble(texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Pede confirmação. Resposta n (ou 0) cancela a operação.
    private void confirmar(String pergunta) {
        while (true) {
            String resposta = lerLinha(pergunta).toLowerCase(PT_BR);
            if (resposta.equals("s")) {
                return;
            }
            if (resposta.equals("n") || resposta.equals("0")) {
                throw new Cancelado();
            }
            aviso("Responda s para confirmar ou n para cancelar.");
        }
    }
}
