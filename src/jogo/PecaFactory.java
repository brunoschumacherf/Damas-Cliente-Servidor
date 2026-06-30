package jogo;

import jogo.Tabuleiro.TipoPeca;

public class PecaFactory {
    public static TipoPeca criarPeca(boolean branca, boolean dama) {
        if (branca) {
            return dama ? TipoPeca.DAMA_BRANCA : TipoPeca.PEAO_BRANCO;
        } else {
            return dama ? TipoPeca.DAMA_PRETA : TipoPeca.PEAO_PRETO;
        }
    }
}