package jogo;

import java.util.ArrayList;
import java.util.List;

public class Tabuleiro {
    public static final int TAMANHO = 8;
    public static final String RESET = "\u001B[0m";
    public static final String DESTAQUE = "\u001B[33m";

    private TipoPeca[][] casas;
    private boolean vezBrancas;
    private int ultimoMovimentoX = -1;
    private int ultimoMovimentoY = -1;
    private List<String> historico;
    private List<TabuleiroObserver> observers = new ArrayList<>();

    public enum TipoPeca {
        VAZIO('·'),
        PEAO_BRANCO('b'),
        PEAO_PRETO('p'),
        DAMA_BRANCA('B'),
        DAMA_PRETA('P');

        private final char simbolo;

        TipoPeca(char simbolo) {
            this.simbolo = simbolo;
        }

        public char getSimbolo() {
            return simbolo;
        }

        public boolean isBranca() {
            return this == PEAO_BRANCO || this == DAMA_BRANCA;
        }

        public boolean isPreta() {
            return this == PEAO_PRETO || this == DAMA_PRETA;
        }

        public boolean isDama() {
            return this == DAMA_BRANCA || this == DAMA_PRETA;
        }
    }

    public Tabuleiro() {
        casas = new TipoPeca[TAMANHO][TAMANHO];
        vezBrancas = true;
        historico = new ArrayList<>();
        inicializarTabuleiro();
    }

    private void inicializarTabuleiro() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                casas[i][j] = TipoPeca.VAZIO;
            }
        }

        // Peças brancas (linhas 0-2)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                if ((i + j) % 2 != 0) {
                    casas[i][j] = TipoPeca.PEAO_BRANCO;
                }
            }
        }

        // Peças pretas (linhas 5-7)
        for (int i = 5; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                if ((i + j) % 2 != 0) {
                    casas[i][j] = TipoPeca.PEAO_PRETO;
                }
            }
        }
    }

    public void addObserver(TabuleiroObserver observer) {
        observers.add(observer);
    }

    private void notificarObservers(String mensagem) {
        for (TabuleiroObserver observer : observers) {
            observer.atualizar(this, mensagem);
        }
    }

    public TipoPeca[][] getCasas() {
        return casas;
    }

    public boolean isVezBrancas() {
        return vezBrancas;
    }

    public boolean isVezPretas() {
        return !vezBrancas;
    }

    private boolean estaDentroTabuleiro(int x, int y) {
        return x >= 0 && x < TAMANHO && y >= 0 && y < TAMANHO;
    }

    public int verificarVencedor() {
        boolean brancasTemPecas = false;
        boolean pretasTemPecas = false;

        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                TipoPeca peca = casas[i][j];
                if (peca.isBranca()) {
                    brancasTemPecas = true;
                } else if (peca.isPreta()) {
                    pretasTemPecas = true;
                }
            }
        }

        if (!brancasTemPecas)
            return 2;
        if (!pretasTemPecas)
            return 1;

        boolean brancasTemMovimentos = false;
        boolean pretasTemMovimentos = false;

        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                TipoPeca peca = casas[i][j];
                if (peca.isBranca() && podeMover(i, j)) {
                    brancasTemMovimentos = true;
                } else if (peca.isPreta() && podeMover(i, j)) {
                    pretasTemMovimentos = true;
                }
            }
        }

        if (!brancasTemMovimentos)
            return 2;
        if (!pretasTemMovimentos)
            return 1;

        return 0;
    }

    private boolean podeMover(int x, int y) {
        TipoPeca peca = casas[x][y];
        int[][] direcoes;

        if (peca.isDama()) {
            direcoes = new int[][] { { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } };
        } else {
            direcoes = peca.isBranca() ? new int[][] { { 1, -1 }, { 1, 1 } } : new int[][] { { -1, -1 }, { -1, 1 } };
        }

        for (int[] dir : direcoes) {
            int novoX = x + dir[0];
            int novoY = y + dir[1];

            if (estaDentroTabuleiro(novoX, novoY) && casas[novoX][novoY] == TipoPeca.VAZIO) {
                return true;
            }

            int capturaX = x + 2 * dir[0];
            int capturaY = y + 2 * dir[1];
            if (estaDentroTabuleiro(capturaX, capturaY) && casas[capturaX][capturaY] == TipoPeca.VAZIO) {
                int meioX = x + dir[0];
                int meioY = y + dir[1];
                TipoPeca pecaMeio = casas[meioX][meioY];

                if (pecaMeio != TipoPeca.VAZIO &&
                        ((peca.isBranca() && pecaMeio.isPreta()) ||
                                (peca.isPreta() && pecaMeio.isBranca()))) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean validarMovimentoPeao(int xOrigem, int yOrigem, int xDestino, int yDestino,
            int dirX, int dirY, int distancia) {
        if (distancia != 1 && distancia != 2) {
            return false;
        }

        if (Math.abs(dirX) != Math.abs(dirY)) {
            return false;
        }

        if (vezBrancas) {
            if (dirX <= 0)
                return false;
        } else {
            if (dirX >= 0)
                return false;
        }

        if (distancia == 1) {
            return casas[xDestino][yDestino] == TipoPeca.VAZIO;
        } else if (distancia == 2) {
            return validarCapturaPeao(xOrigem, yOrigem, xDestino, yDestino, dirX, dirY);
        }

        return false;
    }

    private boolean validarCapturaPeao(int xOrigem, int yOrigem, int xDestino, int yDestino,
            int dirX, int dirY) {
        int xMeio = xOrigem + dirX / 2;
        int yMeio = yOrigem + dirY / 2;

        TipoPeca pecaMeio = casas[xMeio][yMeio];
        if (pecaMeio == TipoPeca.VAZIO ||
                (vezBrancas && !pecaMeio.isPreta()) ||
                (!vezBrancas && !pecaMeio.isBranca())) {
            return false;
        }
        return true;
    }

    public boolean validarMovimentoDama(int xOrigem, int yOrigem, int xDestino, int yDestino,
            int dirX, int dirY, int distancia) {
        if (Math.abs(dirX) != Math.abs(dirY)) {
            return false;
        }

        int passoX = dirX / distancia;
        int passoY = dirY / distancia;

        int pecasAdversarias = 0;
        for (int i = 1; i < distancia; i++) {
            int x = xOrigem + i * passoX;
            int y = yOrigem + i * passoY;
            TipoPeca casa = casas[x][y];

            if (casa != TipoPeca.VAZIO) {
                if ((vezBrancas && casa.isBranca()) ||
                        (!vezBrancas && casa.isPreta())) {
                    return false;
                }
                pecasAdversarias++;
            }
        }
        return pecasAdversarias <= 1;
    }

    public boolean validarMovimento(int xOrigem, int yOrigem, int xDestino, int yDestino) {
        if (!estaDentroTabuleiro(xOrigem, yOrigem) || !estaDentroTabuleiro(xDestino, yDestino)) {
            return false;
        }

        TipoPeca peca = casas[xOrigem][yOrigem];
        if (peca == TipoPeca.VAZIO) {
            return false;
        }

        if ((vezBrancas && !peca.isBranca()) ||
                (!vezBrancas && !peca.isPreta())) {
            return false;
        }

        if (casas[xDestino][yDestino] != TipoPeca.VAZIO) {
            return false;
        }

        int dirX = xDestino - xOrigem;
        int dirY = yDestino - yOrigem;
        int distancia = Math.max(Math.abs(dirX), Math.abs(dirY));

        if (peca.isDama()) {
            return validarMovimentoDama(xOrigem, yOrigem, xDestino, yDestino, dirX, dirY, distancia);
        } else {
            return validarMovimentoPeao(xOrigem, yOrigem, xDestino, yDestino, dirX, dirY, distancia);
        }
    }

    public boolean moverPeca(int xOrigem, int yOrigem, int xDestino, int yDestino) {
        TipoPeca peca = casas[xOrigem][yOrigem];
        boolean capturaRealizada = Math.abs(xDestino - xOrigem) == 2;
        boolean eraDamaAntes = peca.isDama();

        if (capturaRealizada) {
            int xCapturada = (xOrigem + xDestino) / 2;
            int yCapturada = (yOrigem + yDestino) / 2;
            casas[xCapturada][yCapturada] = TipoPeca.VAZIO;
            notificarObservers("Peça capturada!");
        }

        casas[xDestino][yDestino] = peca;
        casas[xOrigem][yOrigem] = TipoPeca.VAZIO;
        verificarCoroada(xDestino, yDestino);

        if (casas[xDestino][yDestino].isDama() && !eraDamaAntes) {
            notificarObservers("Peça coroada a Dama!");
        }

        if (capturaRealizada && podeCapturar(xDestino, yDestino)) {
            setUltimoMovimentoX(xDestino);
            setUltimoMovimentoY(yDestino);
            notificarObservers("Peça capturada!");
            notificarObservers(DESTAQUE + "Continue capturando com a peça em " +
                    (char) ('A' + yDestino) + (xDestino + 1) + RESET);
        } else {
            alternarTurno();
            setUltimoMovimentoX(-1);
            notificarObservers("Movimento realizado");
            notificarObservers("Turno alternado para " + (vezBrancas ? "brancas" : "pretas"));
        }

        return capturaRealizada;
    }

    private void verificarCoroada(int x, int y) {
        TipoPeca peca = casas[x][y];
        if ((peca == TipoPeca.PEAO_BRANCO && x == TAMANHO - 1) ||
                (peca == TipoPeca.PEAO_PRETO && x == 0)) {
            casas[x][y] = peca == TipoPeca.PEAO_BRANCO ? TipoPeca.DAMA_BRANCA : TipoPeca.DAMA_PRETA;
        }
    }

    private void alternarTurno() {
        vezBrancas = !vezBrancas;
    }

    public boolean podeCapturar(int x, int y) {
        TipoPeca peca = casas[x][y];
        int[][] direcoes;

        if (peca.isDama()) {
            direcoes = new int[][] { { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } };
        } else {
            direcoes = peca.isBranca() ? new int[][] { { 1, -1 }, { 1, 1 } } : new int[][] { { -1, -1 }, { -1, 1 } };
        }

        for (int[] dir : direcoes) {
            int xAlvo = x + 2 * dir[0];
            int yAlvo = y + 2 * dir[1];

            if (estaDentroTabuleiro(xAlvo, yAlvo) && validarMovimento(x, y, xAlvo, yAlvo)) {
                return true;
            }
        }
        return false;
    }

    public boolean verificarCapturaObrigatoria() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                TipoPeca peca = casas[i][j];
                if ((vezBrancas && peca.isBranca()) ||
                        (!vezBrancas && peca.isPreta())) {
                    if (podeCapturar(i, j)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void imprimirTabuleiro() {
    System.out.println("\n   A B C D E F G H");
    for (int i = 0; i < TAMANHO; i++) {
        System.out.printf("%d  ", i + 1);
        for (int j = 0; j < TAMANHO; j++) {
            System.out.print(casas[i][j].getSimbolo() + " ");
        }
        System.out.println();
    }
}


    public void mostrarContagem() {
        int brancas = 0, pretas = 0;
        for (TipoPeca[] linha : casas) {
            for (TipoPeca peca : linha) {
                if (peca.isBranca())
                    brancas++;
                else if (peca.isPreta())
                    pretas++;
            }
        }
        System.out.printf("Peças Brancas: %d | Peças Pretas: %d\n", brancas, pretas);
    }

    public void mostrarHistorico() {
        System.out.println("\nHistórico de Jogadas:");
        for (String jogada : historico) {
            System.out.println(jogada);
        }
    }

    public List<String> getHistorico() {
        return historico;
    }

    public static int[] converterCoordenada(String coord) {
        if (coord == null || coord.length() != 2)
            return null;

        try {
            int linha = Integer.parseInt(coord.substring(0, 1)) - 1;
            char colunaChar = Character.toUpperCase(coord.charAt(1));
            int coluna = colunaChar - 'A';

            if (linha < 0 || linha >= TAMANHO || coluna < 0 || coluna >= TAMANHO) {
                return null;
            }
            return new int[] { linha, coluna };
        } catch (Exception e) {
            return null;
        }
    }

    public void registrarJogada(String jogada) {
        historico.add((vezBrancas ? "Brancas" : "Pretas") + ": " + jogada);
    }

    public String serializar() {
        StringBuilder sb = new StringBuilder();
        sb.append(vezBrancas ? "1" : "0").append(";");

        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                sb.append(casas[i][j].name()).append(",");
            }
        }

        return sb.toString();
    }

    public static Tabuleiro desserializar(String estado) {
        String[] partes = estado.split(";");
        Tabuleiro tabuleiro = new Tabuleiro();

        tabuleiro.vezBrancas = partes[0].equals("1");

        String[] casasArray = partes[1].split(",");
        int index = 0;
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                tabuleiro.casas[i][j] = TipoPeca.valueOf(casasArray[index]);
                index++;
            }
        }

        return tabuleiro;
    }

    public int getUltimoMovimentoX() {
        return ultimoMovimentoX;
    }

    public void setUltimoMovimentoX(int ultimoMovimentoX) {
        this.ultimoMovimentoX = ultimoMovimentoX;
    }

    public int getUltimoMovimentoY() {
        return ultimoMovimentoY;
    }

    public void setUltimoMovimentoY(int ultimoMovimentoY) {
        this.ultimoMovimentoY = ultimoMovimentoY;
    }
}