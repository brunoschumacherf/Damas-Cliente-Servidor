// =================== DamasServer.java ===================
package jogo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class DamasServer {
    private static final int PORT = 5555;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static Tabuleiro tabuleiro = new Tabuleiro();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor de Damas iniciado na porta " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + socket);

                ClientHandler clientThread = new ClientHandler(socket, tabuleiro);
                clients.add(clientThread);
                clientThread.start();

                if (clients.size() == 2) {
                    notifyGameStart();
                }
            }
        } catch (IOException e) {
            System.out.println("Erro no servidor: " + e.getMessage());
        }
    }

    private static void notifyGameStart() {
        clients.get(0).sendMessage("INICIO BRANCAS");
        clients.get(1).sendMessage("INICIO PRETAS");
        broadcastTabuleiro();
        broadcastMessage("TURNO BRANCAS");
    }

    public static void broadcastTabuleiro() {
        String estado = tabuleiro.serializar();
        for (ClientHandler client : clients) {
            client.sendMessage("TABULEIRO " + estado);
        }
    }

    public static void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Tabuleiro tabuleiro;

    public ClientHandler(Socket socket, Tabuleiro tabuleiro) {
        this.socket = socket;
        this.tabuleiro = tabuleiro;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Erro ao criar handler: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Comando recebido: " + inputLine);

                synchronized (tabuleiro) {
                    int vencedor = tabuleiro.verificarVencedor();
                    if (vencedor != 0) {
                        String ganhador = (vencedor == 1) ? "BRANCAS" : "PRETAS";
                        DamasServer.broadcastMessage("VITORIA " + ganhador);
                        break;
                    }

                    if (inputLine.startsWith("MOVIMENTO")) {
                        String[] partes = inputLine.split(" ");
                        if (partes.length < 3) {
                            sendMessage("ERRO Formato inválido! Use: '3A 4B'");
                            continue;
                        }
                        int[] origem = Tabuleiro.converterCoordenada(partes[1]);
                        int[] destino = Tabuleiro.converterCoordenada(partes[2]);

                        if (tabuleiro.validarMovimento(origem[0], origem[1], destino[0], destino[1])) {
                            tabuleiro.moverPeca(origem[0], origem[1], destino[0], destino[1]);
                            tabuleiro.registrarJogada(partes[1] + " " + partes[2]);

                            DamasServer.broadcastTabuleiro();

                            vencedor = tabuleiro.verificarVencedor();
                            if (vencedor != 0) {
                                String ganhador = (vencedor == 1) ? "BRANCAS" : "PRETAS";
                                DamasServer.broadcastMessage("VITORIA " + ganhador);
                                break;
                            } else {
                                DamasServer
                                        .broadcastMessage("TURNO " + (tabuleiro.isVezBrancas() ? "BRANCAS" : "PRETAS"));
                            }
                        } else {
                            sendMessage("ERRO Movimento inválido");
                        }

                    } else if (inputLine.equalsIgnoreCase("HIST")) {
                        List<String> historico = tabuleiro.getHistorico();
                        for (String jogada : historico) {
                            sendMessage("HIST " + jogada);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Erro no handler: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Erro ao fechar socket: " + e.getMessage());
            }
        }
    }

}