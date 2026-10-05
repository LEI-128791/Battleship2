package gui;

/** Estados possíveis de uma célula do tabuleiro (só serve para desenhar). */
public enum CellState {
    WATER, SELECTED, MISS, HIT, SUNK,
    /** Já disparada (o servidor disse REPEATED), sem saber se foi água ou acerto. */
    SHOT;

    /** Como se desenha cada resultado de tiro (null = não desenha nada). */
    public static CellState of(Outcome outcome) {
        return switch (outcome) {
            case MISS -> CellState.MISS;
            case HIT -> CellState.HIT;
            case SUNK -> CellState.SUNK;
            case REPEATED -> CellState.SHOT;
            case INVALID -> null;
        };
    }
}