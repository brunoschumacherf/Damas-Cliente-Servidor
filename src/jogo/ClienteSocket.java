package jogo;

import java.io.*;
import java.net.*;

public class ClienteSocket {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClienteSocket(String endereco, int porta) throws IOException {
        socket = new Socket(endereco, porta);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void enviarMensagem(String msg) {
        out.println(msg);
    }

    public String receberMensagem() throws IOException {
        return in.readLine();
    }

    public BufferedReader getInput() {
        return in;
    }
}