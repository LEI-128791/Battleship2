package gui;

import battleship.IGame;

/** Resultado de um tiro. Fica definido num só sítio (antes havia Strings espalhadas). */
public enum Outcome {
    INVALID, REPEATED, MISS, HIT, SUNK;

    /** Traduz o resultado do Game existente (usado no callback do AI). */
    public static Outcome of(IGame.ShotResult result) {
        if (!result.valid()) return INVALID;
        if (result.repeated()) return REPEATED;
        if (result.ship() == null) return MISS;
        return result.sunk() ? SUNK : HIT;
    }

    /** Traduz o texto JSON enviado pelo servidor. */
    public static Outcome parse(String text) {
        try {
            return valueOf(text);
        } catch (IllegalArgumentException e) {
            return INVALID;
        }
    }

    /** Foi um tiro válido que muda o tabuleiro? */
    public boolean isShotOnBoard() {
        return this == MISS || this == HIT || this == SUNK;
    }
}