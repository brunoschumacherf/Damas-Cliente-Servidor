package jogo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class DamasClient {
    private static final String SERVER_ADDRESS = "10.0.0.139";
    private static final int PORT = 5555;
    private static String meuTime;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {
            
            System.out.println("Conectado ao servidor de Damas");

            // Thread para receber mensagens do servidor
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        if (serverMessage.startsWith("INICIO")) {
                            meuTime = serverMessage.split(" ")[1];
                            System.out.println("Você está jogando com as peças " + meuTime);
                        } 
                        else if (serverMessage.startsWith("TABULEIRO")) {
                            String estado = serverMessage.substring(9);
                            Tabuleiro tabuleiro = Tabuleiro.desserializar(estado);
                            tabuleiro.imprimirTabuleiro();
                        }
                        else if (serverMessage.startsWith("TURNO")) {
                            System.out.println("Vez das: " + serverMessage.substring(5));
                        }
                        else if (serverMessage.startsWith("ERRO")) {
                            System.out.println(serverMessage.substring(5));
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Conexão com o servidor perdida");
                    System.exit(0);
                }
            }).start();

            // Loop principal para enviar comandos
            while (true) {
                String comando = scanner.nextLine();
                if (comando.equalsIgnoreCase("SAIR")) {
                    break;
                }
                out.println("MOVIMENTO " + comando);
            }

        } catch (IOException e) {
            System.out.println("Não foi possível conectar ao servidor: " + e.getMessage());
        }
    }
}