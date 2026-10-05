package gui;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.util.function.BiConsumer;

/**
 * Componente puramente visual: desenha um tabuleiro 10x10 e guarda o estado
 * de cada célula. Não conhece regras do jogo nem rede.
 */
public class BoardView {

    public static final int SIZE = 10;
    private static final int CELL = 34;

    private static final Color WATER = Color.web("#1677A8");
    private static final Color GRID = Color.web("#0B3D5A");
    private static final Color SHIP = Color.web("#9FB3C0");
    private static final Color SELECTED = Color.web("#FFD54A");
    private static final Color MISS = Color.web("#EAF2F5");
    private static final Color HIT = Color.web("#E53935");
    private static final Color FIRE = Color.web("#FFB300");
    private static final Color SUNK = Color.web("#6B0F0F");

    private final GridPane grid = new GridPane();
    private final Rectangle[][] backgrounds = new Rectangle[SIZE][SIZE];
    private final StackPane[][] marks = new StackPane[SIZE][SIZE];
    private final CellState[][] states = new CellState[SIZE][SIZE];

    /** @param clickHandler null se o tabuleiro não for clicável */
    public BoardView(BiConsumer<Integer, Integer> clickHandler) {
        grid.setHgap(1);
        grid.setVgap(1);
        grid.setAlignment(Pos.CENTER);

        for (int i = 0; i < SIZE; i++) {
            grid.add(header(String.valueOf(i + 1)), i + 1, 0);
            grid.add(header(String.valueOf((char) ('A' + i))), 0, i + 1);
        }

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Rectangle bg = new Rectangle(CELL, CELL);
                bg.setArcWidth(6);
                bg.setArcHeight(6);
                // A borda fica DENTRO do quadrado: mudar a espessura não altera o tamanho.
                bg.setStrokeType(StrokeType.INSIDE);
                StackPane mark = new StackPane();
                mark.setMouseTransparent(true);
                StackPane cell = new StackPane(bg, mark);
                // Tamanho fixo: nada que se desenhe lá dentro consegue mexer no tabuleiro.
                cell.setMinSize(CELL, CELL);
                cell.setPrefSize(CELL, CELL);
                cell.setMaxSize(CELL, CELL);

                if (clickHandler != null) {
                    final int row = r, col = c;
                    cell.setCursor(Cursor.HAND);
                    // PRESSED em vez de CLICKED: o JavaFX descarta o 'click' se o rato mexer 1 pixel.
                    cell.setOnMousePressed(e -> clickHandler.accept(row, col));
                }

                backgrounds[r][c] = bg;
                marks[r][c] = mark;
                grid.add(cell, c + 1, r + 1);
                set(r, c, CellState.WATER);
            }
        }
    }

    public GridPane getGrid() {
        return grid;
    }

    public CellState get(int row, int column) {
        return states[row][column];
    }

    public void reset() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                set(r, c, CellState.WATER);
    }

    /** Devolve a água todas as células ainda "selecionadas" (e não disparadas). */
    public void clearSelection() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (states[r][c] == CellState.SELECTED)
                    set(r, c, CellState.WATER);
    }

    public void set(int row, int column, CellState state) {
        states[row][column] = state;

        Rectangle bg = backgrounds[row][column];
        StackPane mark = marks[row][column];
        mark.getChildren().clear();
        bg.setFill(WATER);
        bg.setStroke(GRID);
        bg.setStrokeWidth(1);

        switch (state) {
            case WATER -> { }
            case SHIP -> {
                bg.setFill(SHIP);
                bg.setStroke(Color.web("#5F7584"));
            }
            case SELECTED -> {
                bg.setStroke(SELECTED);
                bg.setStrokeWidth(3);
                Circle ring = new Circle(9, Color.TRANSPARENT);
                ring.setStroke(SELECTED);
                ring.setStrokeWidth(2);
                mark.getChildren().addAll(ring, new Circle(2.5, SELECTED));
            }
            case MISS -> mark.getChildren().add(new Circle(4, MISS));
            case SHOT -> {
                Circle ring = new Circle(8, Color.TRANSPARENT);
                ring.setStroke(MISS);
                ring.setStrokeWidth(2);
                mark.getChildren().add(ring);
            }
            case HIT -> mark.getChildren().addAll(new Circle(11, HIT), new Circle(5, FIRE));
            case SUNK -> {
                bg.setFill(SUNK);
                mark.getChildren().addAll(
                        cross(-8, -8, 8, 8),
                        cross(-8, 8, 8, -8));
            }
        }
    }

    private static Line cross(double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.WHITE);
        line.setStrokeWidth(3);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        return line;
    }

    private static Label header(String text) {
        Label label = new Label(text);
        label.setMinSize(CELL, CELL);
        label.setPrefSize(CELL, CELL);
        label.setMaxSize(CELL, CELL);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-text-fill: #B8D8E8; -fx-font-weight: bold;");
        return label;
    }
}