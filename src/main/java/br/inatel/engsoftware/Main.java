package br.inatel.engsoftware;

import br.inatel.engsoftware.dao.ServicoDAO;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Projeto de Engenharia de Software configuração realizada");

        ServicoDAO dao = new ServicoDAO();

        try {
            // 1. Criar serviço
            Servico servico = new Servico(
                    0,
                    "Instalacao eletrica",
                    "Instalacao e manutencao residencial",
                    150.00
            );

            // 2. Inserir no banco
            //dao.inserir(servico);  Comentado para não inserir registros repetidos no Deploy

            System.out.println(
                    "Servico cadastrado! ID: " + servico.getId()
            );

            // 3. Buscar no banco
            Servico encontrado = dao.buscarPorId(servico.getId());

            if (encontrado != null) {
                System.out.println(
                        "Servico encontrado: " + encontrado.formatar()
                );
            }

            // 4. Listar todos
            for (Servico item : dao.listarTodos()) {
                System.out.println(item.formatar());
            }

        } catch (SQLException e) {
            System.err.println(
                    "Erro no banco de dados: " + e.getMessage()
            );
        }
    }
}