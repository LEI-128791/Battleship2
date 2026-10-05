package gui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * Um tabuleiro desenhado em camadas (mar -> navios -> marcas -> cliques).
 * Só desenha: não conhece regras do jogo nem rede.
 */
public class BoardView {

    public static final int SIZE = 10;
    private static final double CELL = 36;
    private static final double FIELD = SIZE * CELL;
    private static final Color GOLD = Color.web("#FFD54A");

    private final GridPane root = new GridPane();
    private final Group shipLayer = new Group();
    private final Group[][] marks = new Group[SIZE][SIZE];
    private final Animation[][] animations = new Animation[SIZE][SIZE];
    private final CellState[][] states = new CellState[SIZE][SIZE];
    private final Map<String, Group> ships = new HashMap<>();
    private final Group hover = reticle(Color.WHITE);

    /** @param clickHandler null se o tabuleiro não for clicável */
    public BoardView(BiConsumer<Integer, Integer> clickHandler) {
        Pane field = new Pane();
        field.setMinSize(FIELD, FIELD);
        field.setPrefSize(FIELD, FIELD);
        field.setMaxSize(FIELD, FIELD);

        Rectangle sea = new Rectangle(FIELD, FIELD);
        sea.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1C86BC")), new Stop(1, Color.web("#0A4670"))));

        shipLayer.setMouseTransparent(true);

        Group markLayer = new Group();
        markLayer.setMouseTransparent(true);
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Group mark = new Group();
                mark.setLayoutX((c + 0.5) * CELL);
                mark.setLayoutY((r + 0.5) * CELL);
                marks[r][c] = mark;
                states[r][c] = CellState.WATER;
                markLayer.getChildren().add(mark);
            }
        }
        hover.setOpacity(0.6);
        hover.setVisible(false);
        markLayer.getChildren().add(hover);

        Rectangle frame = new Rectangle(FIELD, FIELD);
        frame.setFill(null);
        frame.setStroke(Color.web("#0A2F47"));
        frame.setStrokeWidth(3);
        frame.setStrokeType(StrokeType.INSIDE);
        frame.setMouseTransparent(true);

        field.getChildren().addAll(sea, decor(), shipLayer, markLayer, frame);
        if (clickHandler != null) {
            field.getChildren().add(hitLayer(clickHandler));
        }

        HBox columns = new HBox();
        VBox rows = new VBox();
        for (int i = 0; i < SIZE; i++) {
            columns.getChildren().add(axis(String.valueOf(i + 1), CELL, 22));
            rows.getChildren().add(axis(String.valueOf((char) ('A' + i)), 24, CELL));
        }
        root.add(columns, 1, 0);
        root.add(rows, 0, 1);
        root.add(field, 1, 1);
    }

    public Node getNode() {
        return root;
    }

    public CellState get(int row, int column) {
        return states[row][column];
    }

    public void reset() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                stop(r, c);
                marks[r][c].getChildren().clear();
                states[r][c] = CellState.WATER;
            }
        }
        shipLayer.getChildren().clear();
        ships.clear();
        hover.setVisible(false);
    }

    /** Devolve a água as células ainda "selecionadas" (e não disparadas). */
    public void clearSelection() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (states[r][c] == CellState.SELECTED)
                    set(r, c, CellState.WATER);
    }

    /** Desenha um navio (cells = {linha, coluna}). Chamar de novo com wreck=true troca-o por um destroço. */
    public void addShip(List<int[]> cells, boolean wreck) {
        int minR = SIZE, maxR = -1, minC = SIZE, maxC = -1;
        for (int[] cell : cells) {
            minR = Math.min(minR, cell[0]);
            maxR = Math.max(maxR, cell[0]);
            minC = Math.min(minC, cell[1]);
            maxC = Math.max(maxC, cell[1]);
        }

        // Chave = a primeira casa do navio (sempre pertence ao navio, ao contrário do canto da caixa).
        int[] first = cells.get(0);
        for (int[] cell : cells) {
            if (cell[0] < first[0] || (cell[0] == first[0] && cell[1] < first[1])) {
                first = cell;
            }
        }
        String key = first[0] + "," + first[1];
        Group old = ships.remove(key);
        if (old != null) {
            shipLayer.getChildren().remove(old);
        }

        // Linha reta completa -> casco com proa. Qualquer outra forma (ex.: Galeão em T) -> segue as casas.
        boolean straight = (minR == maxR || minC == maxC)
                && cells.size() == (maxR - minR + 1) * (maxC - minC + 1);

        Group hull;
        if (straight) {
            boolean vertical = maxR > minR;
            int length = vertical ? maxR - minR + 1 : maxC - minC + 1;
            hull = ShipArt.hull(length, vertical, CELL, wreck);
            hull.setLayoutX((minC + maxC + 1) / 2.0 * CELL);
            hull.setLayoutY((minR + maxR + 1) / 2.0 * CELL);
        } else {
            hull = ShipArt.composite(cells, CELL, wreck);
        }

        ships.put(key, hull);
        shipLayer.getChildren().add(hull);
    }

    public void set(int row, int column, CellState state) {
        CellState old = states[row][column];
        states[row][column] = state;
        stop(row, column);

        Group g = marks[row][column];
        g.getChildren().clear();

        switch (state) {
            case WATER -> { }
            case SELECTED -> {
                Rectangle tint = new Rectangle(-CELL / 2 + 2, -CELL / 2 + 2, CELL - 4, CELL - 4);
                tint.setFill(Color.web("#FFD54A", 0.18));
                tint.setStroke(Color.web("#FFD54A", 0.8));
                tint.setStrokeWidth(1.5);
                Group target = reticle(GOLD);
                RotateTransition spin = new RotateTransition(Duration.seconds(8), target);
                spin.setByAngle(360);
                spin.setInterpolator(Interpolator.LINEAR);
                spin.setCycleCount(Animation.INDEFINITE);
                g.getChildren().addAll(tint, target);
                animations[row][column] = spin;
                spin.play();
            }
            case MISS -> {
                Circle ring = new Circle(9);
                ring.setFill(null);
                ring.setStroke(Color.web("#FFFFFF", 0.45));
                ring.setStrokeWidth(1.5);
                g.getChildren().addAll(ring, new Circle(4.5, Color.web("#EAF6FF", 0.9)));
                if (old != CellState.MISS) pulse(g, Color.WHITE, 3.0, 650);
            }
            case SHOT -> {
                Circle ring = new Circle(8);
                ring.setFill(null);
                ring.setStroke(Color.web("#FFFFFF", 0.55));
                ring.setStrokeWidth(2);
                g.getChildren().add(ring);
            }
            case HIT -> {
                Circle blaze = new Circle(12);
                blaze.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                        new Stop(0.0, Color.web("#FFF4B8")), new Stop(0.35, Color.web("#FFB300")),
                        new Stop(0.75, Color.web("#E53935")), new Stop(1.0, Color.web("#E53935", 0.0))));
                blaze.setEffect(new DropShadow(10, Color.web("#FF6D00")));
                ScaleTransition flicker = new ScaleTransition(Duration.millis(420), blaze);
                flicker.setFromX(0.85);
                flicker.setFromY(0.85);
                flicker.setToX(1.1);
                flicker.setToY(1.1);
                flicker.setAutoReverse(true);
                flicker.setCycleCount(Animation.INDEFINITE);
                g.getChildren().add(blaze);
                animations[row][column] = flicker;
                flicker.play();
                if (old != CellState.HIT) pulse(g, Color.web("#FFB300"), 3.0, 600);
            }
            case SUNK -> {
                // X pequeno: tem de caber dentro do casco, mesmo na proa (que estreita) e no barquinho de 1 casa.
                g.getChildren().addAll(cross(-5, -5, 5, 5), cross(-5, 5, 5, -5));
                if (old != CellState.SUNK) pulse(g, Color.web("#FF5252"), 3.4, 800);
            }
        }
    }

    // ------------------------------------------------------------- helpers

    private void stop(int row, int column) {
        if (animations[row][column] != null) {
            animations[row][column].stop();
            animations[row][column] = null;
        }
    }

    private Group hitLayer(BiConsumer<Integer, Integer> click) {
        Group layer = new Group();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                final int row = r;
                final int col = c;
                Rectangle hit = new Rectangle(c * CELL, r * CELL, CELL, CELL);
                hit.setFill(Color.TRANSPARENT);
                hit.setCursor(Cursor.CROSSHAIR);
                hit.setOnMousePressed(e -> {
                    click.accept(row, col);
                    showHover(row, col);
                });
                hit.setOnMouseEntered(e -> showHover(row, col));
                hit.setOnMouseExited(e -> hover.setVisible(false));
                layer.getChildren().add(hit);
            }
        }
        return layer;
    }

    private void showHover(int row, int column) {
        hover.setVisible(states[row][column] == CellState.WATER);
        hover.setLayoutX((column + 0.5) * CELL);
        hover.setLayoutY((row + 0.5) * CELL);
    }

    /** Ondas suaves + linhas da grelha. */
    private static Group decor() {
        Group g = new Group();
        Random random = new Random(7);
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                double x = c * CELL + 6 + random.nextInt(8);
                double y = r * CELL + 10 + random.nextInt(14);
                QuadCurve wave = new QuadCurve(x, y, x + 6, y - 4, x + 12, y);
                wave.setFill(null);
                wave.setStroke(Color.web("#FFFFFF", 0.10));
                wave.setStrokeWidth(1.2);
                g.getChildren().add(wave);
            }
        }
        for (int i = 0; i <= SIZE; i++) {
            Line vertical = new Line(i * CELL, 0, i * CELL, FIELD);
            Line horizontal = new Line(0, i * CELL, FIELD, i * CELL);
            for (Line line : new Line[]{vertical, horizontal}) {
                line.setStroke(Color.web("#FFFFFF", 0.20));
                line.setStrokeWidth(1);
                g.getChildren().add(line);
            }
        }
        g.setMouseTransparent(true);
        return g;
    }

    private static Group reticle(Color color) {
        Circle ring = new Circle(11);
        ring.setFill(null);
        ring.setStroke(color);
        ring.setStrokeWidth(2);
        Group g = new Group(ring, new Circle(1.8, color));
        double[][] ticks = {{-16, 0, -7, 0}, {7, 0, 16, 0}, {0, -16, 0, -7}, {0, 7, 0, 16}};
        for (double[] t : ticks) {
            Line line = new Line(t[0], t[1], t[2], t[3]);
            line.setStroke(color);
            line.setStrokeWidth(2);
            g.getChildren().add(line);
        }
        return g;
    }

    private static Line cross(double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.web("#FF5252"));
        line.setStrokeWidth(2.5);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        return line;
    }

    /** Onda que se expande e desvanece (salpico / explosão). */
    private static void pulse(Group target, Color color, double scale, double millis) {
        Circle wave = new Circle(7);
        wave.setFill(null);
        wave.setStroke(color);
        wave.setStrokeWidth(2.5);
        target.getChildren().add(wave);

        ScaleTransition grow = new ScaleTransition(Duration.millis(millis), wave);
        grow.setFromX(0.4);
        grow.setFromY(0.4);
        grow.setToX(scale);
        grow.setToY(scale);
        FadeTransition fade = new FadeTransition(Duration.millis(millis), wave);
        fade.setFromValue(0.9);
        fade.setToValue(0);

        ParallelTransition both = new ParallelTransition(grow, fade);
        both.setOnFinished(e -> target.getChildren().remove(wave));
        both.play();
    }

    private static Label axis(String text, double width, double height) {
        Label label = new Label(text);
        label.getStyleClass().add("axis-label");
        label.setAlignment(Pos.CENTER);
        label.setMinSize(width, height);
        label.setPrefSize(width, height);
        label.setMaxSize(width, height);
        return label;
    }
}