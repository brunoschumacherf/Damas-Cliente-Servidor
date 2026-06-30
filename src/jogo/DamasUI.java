package jogo;

import java.io.IOException;
import java.util.Scanner;

public class DamasUI {
    private ClienteSocket cliente;
    private Scanner scanner;

    public static void main(String[] args) {
        try {
            ClienteSocket cliente = new ClienteSocket("localhost", 5555);
            DamasUI ui = new DamasUI(cliente);
            ui.iniciar();
        } catch (IOException e) {
            System.out.println("Erro ao conectar com o servidor: " + e.getMessage());
        }
    }

    public DamasUI(ClienteSocket cliente) {
        this.cliente = cliente;
        this.scanner = new Scanner(System.in);
    }

    public void iniciar() {
        System.out.println("Bem-vindo ao Jogo de Damas!");
        System.out.println("Instruções:");
        System.out.println("- Digite as jogadas no formato 'origem destino' (ex: '3A 4B')");
        System.out.println("- Comandos especiais: 'SAIR', 'HIST'");

        new Thread(() -> {
            try {
                String msg;
                while ((msg = cliente.receberMensagem()) != null) {
                    if (msg.startsWith("INICIO")) {
                        System.out.println("Você está jogando com as peças " + msg.split(" ")[1]);
                    } else if (msg.startsWith("TABULEIRO")) {
                        Tabuleiro tabuleiro = Tabuleiro.desserializar(msg.substring(9));
                        tabuleiro.imprimirTabuleiro();
                        tabuleiro.mostrarContagem();
                    } else if (msg.startsWith("TURNO")) {
                        System.out.println("Vez das: " + msg.substring(5));
                    } else if (msg.startsWith("ERRO")) {
                        System.out.println("Erro: " + msg.substring(5));
                    } else if (msg.startsWith("HIST")) {
                        System.out.println("Histórico: " + msg.substring(5));
                    } else if (msg.startsWith("VITORIA")) {
                        System.out.println("Fim de jogo! Vencedor: " + msg.split(" ")[1]);
                        System.exit(0);
                    }
                }
            } catch (IOException e) {
                System.out.println("Conexão com o servidor perdida.");
            }
        }).start();

        while (true) {
            processarTurno();
        }
    }

    private void processarTurno() {
        System.out.print("Sua jogada: ");
        String jogada = scanner.nextLine().trim();

        if (jogada.equalsIgnoreCase("SAIR")) {
            System.out.println("Jogo encerrado.");
            System.exit(0);
        } else if (jogada.equalsIgnoreCase("HIST")) {
            cliente.enviarMensagem("HIST");
        } else {
            String[] partes = jogada.split(" ");
            if (partes.length != 2) {
                System.out.println("Formato inválido! Use: '3A 4B'");
                return;
            }
            cliente.enviarMensagem("MOVIMENTO " + jogada);
        }
    }
}