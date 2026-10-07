package gui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

/**
 * Layout e widgets da janela (cabeçalho, painéis, barra de estado, controlos, banner final).
 * Não tem lógica de jogo: o BattleshipGUI diz-lhe o que mostrar.
 */
public class GameView {

    public enum Tone { INFO, WARN, ERROR, WIN, LOSE }

    private static final Color GOLD = Color.web("#FFD54A");
    private static final Color CYAN = Color.web("#4FC3F7");

    private final StackPane root = new StackPane();

    private final TextField playerNameField = new TextField("Player");
    private final TextField serverField = new TextField("http://localhost:8080");

    private final Label statusText = new Label();
    private final HBox statusBar = new HBox(statusText);

    private final Label shotsValue = new Label("0");
    private final Label hitsValue = new Label("0");
    private final Label accuracyValue = new Label("0%");

    private final Label playerAfloat = new Label();
    private final Label enemyAfloat = new Label();

    private final Circle[] pips;
    private final Button fireButton = new Button("FIRE");
    private final Button simulateButton = new Button("SIMULATE");

    private final VBox banner = new VBox(10);
    private final Label bannerTitle = new Label();
    private final Label bannerSubtitle = new Label();

    public GameView(BoardView player, BoardView enemy, int shotsPerTurn,
                    Runnable onNewGame, Runnable onFire, Runnable onSimulate) {

        pips = new Circle[shotsPerTurn];

        BorderPane content = new BorderPane();
        content.getStyleClass().add("root-pane");
        content.setPadding(new Insets(16, 20, 18, 20));
        content.setTop(header(onNewGame));

        HBox boards = new HBox(22,
                panel("YOUR FLEET", playerAfloat, player),
                panel("ENEMY WATERS", enemyAfloat, enemy));
        boards.setAlignment(Pos.CENTER);
        content.setCenter(boards);

        content.setBottom(bottom(onFire, onSimulate));

        buildBanner(onNewGame);
        root.getChildren().addAll(content, banner);

        setStatus("Press NEW GAME to start.", Tone.INFO);
    }

    // ------------------------------------------------------------ API

    public Parent getRoot() {
        return root;
    }

    public String serverUrl() {
        return serverField.getText();
    }

    public String playerName() {
        return playerNameField.getText();
    }

    public void setStatus(String text, Tone tone) {
        statusText.setText(text);
        statusBar.getStyleClass().setAll("status-bar", "status-" + tone.name().toLowerCase());
    }

    public void setFireEnabled(boolean enabled) {
        fireButton.setDisable(!enabled);
    }

    public void setSimulateEnabled(boolean enabled) {
        simulateButton.setDisable(!enabled);
    }

    /** Durante a simulação o botão passa a STOP. */
    public void setSimulateRunning(boolean running) {
        simulateButton.setText(running ? "STOP" : "SIMULATE");
    }

    public void setSelected(int count) {
        for (int i = 0; i < pips.length; i++) {
            pips[i].setFill(i < count ? GOLD : Color.TRANSPARENT);
        }
    }

    public void setStats(int shots, int hits) {
        shotsValue.setText(String.valueOf(shots));
        hitsValue.setText(String.valueOf(hits));
        accuracyValue.setText(shots == 0 ? "0%" : Math.round(100.0 * hits / shots) + "%");
    }

    public void setPlayerAfloat(int afloat, int total) {
        playerAfloat.setText("AFLOAT " + afloat + "/" + total);
    }

    public void setEnemyAfloat(int afloat, int total) {
        enemyAfloat.setText("AFLOAT " + afloat + "/" + total);
    }

    public void showBanner(boolean victory) {
        showBanner(victory ? "VICTORY" : "DEFEAT",
                victory ? "All enemy ships have been sunk." : "Your fleet has been destroyed.",
                victory ? "banner-win" : "banner-lose");
    }

    /** @param styleClass banner-win (dourado), banner-lose (vermelho) ou banner-neutral (azul) */
    public void showBanner(String title, String subtitle, String styleClass) {
        bannerTitle.setText(title);
        bannerTitle.getStyleClass().setAll("banner-title", styleClass);
        bannerSubtitle.setText(subtitle);
        banner.setOpacity(0);
        banner.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.millis(600), banner);
        fade.setToValue(1);
        fade.play();
    }

    public void hideBanner() {
        banner.setVisible(false);
    }

    // ------------------------------------------------------- Secções

    private Node header(Runnable onNewGame) {
        Label title = new Label("BATTLESHIP");
        title.getStyleClass().add("title");
        Label subtitle = new Label("NAVAL COMMAND CENTER");
        subtitle.getStyleClass().add("subtitle");

        HBox brand = new HBox(12, radarIcon(), new VBox(0, title, subtitle));
        brand.setAlignment(Pos.CENTER_LEFT);

        playerNameField.setPrefWidth(120);
        serverField.setPrefWidth(190);

        Button newGame = new Button("NEW GAME");
        newGame.getStyleClass().add("secondary-button");
        newGame.setOnAction(e -> onNewGame.run());

        HBox connection = new HBox(8, caption("PLAYER"), playerNameField,
                caption("SERVER"), serverField, newGame);
        connection.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(brand, spacer, connection);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 14, 0));
        return header;
    }

    private Node panel(String title, Label badge, BoardView board) {
        Label name = new Label(title);
        name.getStyleClass().add("panel-title");
        badge.getStyleClass().add("badge");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox head = new HBox(name, spacer, badge);
        head.setAlignment(Pos.CENTER);

        VBox panel = new VBox(10, head, board.getNode());
        panel.getStyleClass().add("panel");
        panel.setAlignment(Pos.TOP_CENTER);
        return panel;
    }

    private Node bottom(Runnable onFire, Runnable onSimulate) {
        statusText.getStyleClass().add("status-text");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setMinHeight(42);

        HBox stats = new HBox(22,
                stat("SHOTS", shotsValue), stat("HITS", hitsValue), stat("ACCURACY", accuracyValue));
        stats.setAlignment(Pos.CENTER_LEFT);

        HBox pipBox = new HBox(6);
        pipBox.setAlignment(Pos.CENTER);
        for (int i = 0; i < pips.length; i++) {
            pips[i] = new Circle(6, Color.TRANSPARENT);
            pips[i].setStroke(GOLD);
            pips[i].setStrokeWidth(2);
            pipBox.getChildren().add(pips[i]);
        }

        fireButton.getStyleClass().add("fire-button");
        fireButton.setDisable(true);
        fireButton.setOnAction(e -> onFire.run());

        simulateButton.getStyleClass().add("secondary-button");
        simulateButton.setOnAction(e -> onSimulate.run());

        HBox fireBox = new HBox(10, pipBox, fireButton, simulateButton);
        fireBox.setAlignment(Pos.CENTER);

        Region left = new Region();
        Region right = new Region();
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox controls = new HBox(stats, left, fireBox, right, legend());
        controls.setAlignment(Pos.CENTER);

        VBox bottom = new VBox(12, statusBar, controls);
        bottom.setPadding(new Insets(14, 0, 0, 0));
        return bottom;
    }

    private void buildBanner(Runnable onNewGame) {
        bannerSubtitle.getStyleClass().add("banner-subtitle");

        Button again = new Button("NEW GAME");
        again.getStyleClass().add("fire-button");
        again.setOnAction(e -> onNewGame.run());

        Button view = new Button("VIEW BOARDS");
        view.getStyleClass().add("secondary-button");
        view.setOnAction(e -> banner.setVisible(false));

        HBox buttons = new HBox(10, again, view);
        buttons.setAlignment(Pos.CENTER);

        banner.getChildren().addAll(bannerTitle, bannerSubtitle, buttons);
        banner.getStyleClass().add("banner");
        banner.setAlignment(Pos.CENTER);
        banner.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        banner.setVisible(false);
    }

    // ------------------------------------------------------- Pequenos blocos

    private Node radarIcon() {
        Line sweep = new Line(0, 0, 0, -16);
        sweep.setStroke(CYAN);
        sweep.setStrokeWidth(2);
        sweep.setStrokeLineCap(StrokeLineCap.ROUND);

        Group radar = new Group(ring(16), ring(10), ring(4), sweep);
        RotateTransition spin = new RotateTransition(Duration.seconds(4), sweep);
        spin.setByAngle(360);
        spin.setInterpolator(Interpolator.LINEAR);
        spin.setCycleCount(RotateTransition.INDEFINITE);
        spin.play();

        // Pane com tamanho fixo: a rotação não faz o layout "saltar".
        Pane icon = new Pane(radar);
        radar.setLayoutX(20);
        radar.setLayoutY(20);
        icon.setMinSize(40, 40);
        icon.setPrefSize(40, 40);
        icon.setMaxSize(40, 40);
        return icon;
    }

    private static Circle ring(double radius) {
        Circle c = new Circle(radius);
        c.setFill(null);
        c.setStroke(Color.web("#4FC3F7", 0.6));
        c.setStrokeWidth(1.5);
        return c;
    }

    private static Label caption(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-caption");
        return label;
    }

    private static VBox stat(String caption, Label value) {
        Label label = new Label(caption);
        label.getStyleClass().add("stat-caption");
        value.getStyleClass().add("stat-value");
        return new VBox(1, label, value);
    }

    private HBox legend() {
        Circle hit = new Circle(6, Color.web("#FFB300"));
        Circle miss = new Circle(4, Color.web("#EAF6FF"));
        Group sunk = new Group(cross(-4, -4, 4, 4), cross(-4, 4, 4, -4));
        Circle target = new Circle(5, Color.TRANSPARENT);
        target.setStroke(GOLD);
        target.setStrokeWidth(2);
        Circle shot = new Circle(5, Color.TRANSPARENT);
        shot.setStroke(Color.web("#FFFFFF", 0.7));
        shot.setStrokeWidth(2);

        HBox legend = new HBox(12,
                item(hit, "Hit"), item(miss, "Miss"), item(sunk, "Sunk"),
                item(target, "Target"), item(shot, "Already shot"));
        legend.setAlignment(Pos.CENTER_RIGHT);
        return legend;
    }

    private static Line cross(double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.web("#FF5252"));
        line.setStrokeWidth(2);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        return line;
    }

    private static HBox item(Node icon, String text) {
        StackPane holder = new StackPane(icon);
        holder.setMinSize(16, 16);
        holder.setMaxSize(16, 16);
        Label label = new Label(text);
        label.getStyleClass().add("legend-text");
        HBox box = new HBox(5, holder, label);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }
}