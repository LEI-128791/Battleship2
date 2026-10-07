package gui;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Fábrica de cascos de navio. O desenho está centrado em (0,0) para rodar sem saltos. */
final class ShipArt {

    private ShipArt() { }

    static Group hull(int cells, boolean vertical, double cell, boolean wreck) {
        double length = cells * cell - (cells == 1 ? 14 : 8);
        double width = cell - 14;
        double h = length / 2;
        double t = width / 2;
        double bow = Math.min(16, length / 2.5);

        Polygon body = new Polygon(
                -h, -t + 3, -h + 5, -t, h - bow, -t, h, 0,
                h - bow, t, -h + 5, t, -h, t - 3);
        body.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, wreck ? Color.web("#4A4F54") : Color.web("#C5D0D8")),
                new Stop(1, wreck ? Color.web("#1E2226") : Color.web("#6B8394"))));
        body.setStroke(wreck ? Color.web("#0E1114") : Color.web("#2F4350"));
        body.setStrokeWidth(1.5);

        Rectangle deck = new Rectangle(-h + 6, -t + 4, Math.max(4, length - bow - 10), width - 8);
        deck.setArcWidth(6);
        deck.setArcHeight(6);
        deck.setFill(wreck ? Color.web("#2B3035") : Color.web("#DDE5EA", 0.6));

        Group ship = new Group(body, deck);
        if (cells >= 2) ship.getChildren().add(block(-length * 0.15, width * 0.5, length * 0.2, wreck));
        if (cells >= 3) ship.getChildren().add(funnel(length * 0.12, width * 0.17, wreck));
        if (cells >= 4) ship.getChildren().add(funnel(length * 0.28, width * 0.12, wreck));

        ship.setRotate(vertical ? 90 : 0);
        ship.setMouseTransparent(true);
        return ship;
    }

    /**
     * Casco para navios que NÃO são uma linha reta (ex.: o Galeão em T).
     * Segue exatamente as casas do navio, em coordenadas absolutas do tabuleiro.
     */
    static Group composite(List<int[]> cells, double cell, boolean wreck) {
        double size = cell - 8;
        Color outline = wreck ? Color.web("#0E1114") : Color.web("#2F4350");
        Color body = wreck ? Color.web("#3A4046") : Color.web("#A9BAC6");
        Color plank = wreck ? Color.web("#2B3035") : Color.web("#DDE5EA", 0.55);

        Set<String> occupied = new HashSet<>();
        for (int[] c : cells) {
            occupied.add(c[0] + "," + c[1]);
        }

        Group outlines = new Group();
        Group fills = new Group();
        Group details = new Group();
        int[] hub = cells.get(0);
        int best = -1;

        for (int[] c : cells) {
            double cx = (c[1] + 0.5) * cell;
            double cy = (c[0] + 0.5) * cell;

            outlines.getChildren().add(rect(cx - size / 2 - 1.5, cy - size / 2 - 1.5, size + 3, size + 3, 12, outline));
            fills.getChildren().add(rect(cx - size / 2, cy - size / 2, size, size, 10, body));
            details.getChildren().add(rect(cx - size / 2 + 4, cy - size / 2 + 4, size - 8, size - 8, 6, plank));

            int neighbours = 0;
            if (occupied.contains(c[0] + "," + (c[1] + 1))) {
                neighbours++;
                outlines.getChildren().add(rect(cx, cy - size / 2 - 1.5, cell, size + 3, 0, outline));
                fills.getChildren().add(rect(cx, cy - size / 2, cell, size, 0, body));
            }
            if (occupied.contains((c[0] + 1) + "," + c[1])) {
                neighbours++;
                outlines.getChildren().add(rect(cx - size / 2 - 1.5, cy, size + 3, cell, 0, outline));
                fills.getChildren().add(rect(cx - size / 2, cy, size, cell, 0, body));
            }
            if (occupied.contains(c[0] + "," + (c[1] - 1))) neighbours++;
            if (occupied.contains((c[0] - 1) + "," + c[1])) neighbours++;

            if (neighbours > best) {
                best = neighbours;
                hub = c;
            }
        }

        // Ponte de comando na casa central (a que tem mais vizinhas).
        double hx = (hub[1] + 0.5) * cell;
        double hy = (hub[0] + 0.5) * cell;
        Rectangle cabin = rect(hx - 8, hy - 6, 16, 12, 4, wreck ? Color.web("#1A1D20") : Color.web("#F2F5F7"));
        cabin.setStroke(wreck ? Color.web("#0E1114") : Color.web("#5C7482"));
        Circle funnel = new Circle(hx, hy, 3.5, wreck ? Color.web("#15181A") : Color.web("#8FA1AD"));

        Group ship = new Group(outlines, fills, details, cabin, funnel);
        ship.setMouseTransparent(true);
        return ship;
    }

    private static Rectangle rect(double x, double y, double w, double h, double arc, Color fill) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setArcWidth(arc);
        r.setArcHeight(arc);
        r.setFill(fill);
        return r;
    }

    private static Rectangle block(double centerX, double height, double w, boolean wreck) {
        Rectangle r = new Rectangle(centerX - w / 2, -height / 2, w, height);
        r.setArcWidth(4);
        r.setArcHeight(4);
        r.setFill(wreck ? Color.web("#1A1D20") : Color.web("#F2F5F7"));
        r.setStroke(wreck ? Color.web("#0E1114") : Color.web("#5C7482"));
        return r;
    }

    private static Circle funnel(double centerX, double radius, boolean wreck) {
        Circle c = new Circle(centerX, 0, radius);
        c.setFill(wreck ? Color.web("#15181A") : Color.web("#8FA1AD"));
        c.setStroke(wreck ? Color.web("#0E1114") : Color.web("#41586A"));
        return c;
    }
}