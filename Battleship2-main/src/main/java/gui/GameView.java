package gui;

import battleship.Messages;

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
import javafx.scene.control.Labeled;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Layout e widgets da janela (cabeçalho, painéis, barra de estado, controlos, banner final).
 * Não tem lógica de jogo: o BattleshipGUI diz-lhe o que mostrar.
 */
public class GameView {

    public enum Tone { INFO, WARN, ERROR, WIN, LOSE }

    private static final Color GOLD = Color.web("#FFD54A");
    private static final Color CYAN = Color.web("#4FC3F7");

    private final StackPane root = new StackPane();

    /** Tudo o que tem de voltar a ler o texto quando o idioma muda. */
    private final List<Runnable> translators = new ArrayList<>();

    private final TextField playerNameField = new TextField("Player");
    private final TextField serverField = new TextField("http://localhost:8080");

    private final Label statusText = new Label();
    private final HBox statusBar = new HBox(statusText);

    private final Label shotsValue = new Label("0");
    private final Label hitsValue = new Label("0");
    private final Label accuracyValue = new Label("0%");
    private final Label timerValue = new Label("00:00");
    private final Label lastMoveTimeValue = new Label("--");

    private final Label playerAfloat = new Label();
    private final Label enemyAfloat = new Label();
    private int playerAfloatCount;
    private int playerAfloatTotal;
    private int enemyAfloatCount;
    private int enemyAfloatTotal;

    private final Circle[] pips;
    private final Button fireButton = new Button();
    private final Button simulateButton = new Button();
    private final Button exportButton = new Button();
    private final Button languageButton = new Button();
    private boolean simulateRunning;

    private final VBox banner = new VBox(10);
    private final Label bannerTitle = new Label();
    private final Label bannerSubtitle = new Label();
    private Supplier<String> bannerTitleText = () -> "";
    private Supplier<String> bannerSubtitleText = () -> "";

    public GameView(BoardView player, BoardView enemy, int shotsPerTurn,
                    Runnable onNewGame, Runnable onFire,
                    Runnable onSimulate, Runnable onExportPdf,
                    Runnable onLanguage) {

        pips = new Circle[shotsPerTurn];

        BorderPane content = new BorderPane();
        content.getStyleClass().add("root-pane");
        content.setPadding(new Insets(16, 20, 18, 20));
        content.setTop(header(onNewGame, onLanguage));

        HBox boards = new HBox(22,
                panel("gui.yourFleet", playerAfloat, player),
                panel("gui.enemyWaters", enemyAfloat, enemy));
        boards.setAlignment(Pos.CENTER);
        content.setCenter(boards);

        content.setBottom(bottom(onFire, onSimulate, onExportPdf));

        buildBanner(onNewGame);
        root.getChildren().addAll(content, banner);

        setStatus(Messages.get("gui.pressNewGame"), Tone.INFO);
        refreshTexts();
    }

    // ------------------------------------------------------------ Idioma

    /** Escreve outra vez todos os textos fixos no idioma atual do Messages. */
    public void refreshTexts() {
        for (Runnable translator : translators) {
            translator.run();
        }
        languageButton.setText(Messages.getLanguage().equals("pt") ? "EN" : "PT");
        renderAfloat();
        updateSimulateText();
        bannerTitle.setText(bannerTitleText.get());
        bannerSubtitle.setText(bannerSubtitleText.get());
    }

    /** Liga um texto fixo a uma chave: fica traduzido já e volta a traduzir-se em refreshTexts(). */
    private <T extends Labeled> T bind(T node, String key) {
        Runnable translator = () -> node.setText(Messages.get(key));
        translator.run();
        translators.add(translator);
        return node;
    }

    private Label label(String key, String styleClass) {
        Label label = bind(new Label(), key);
        label.getStyleClass().add(styleClass);
        return label;
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

    public void setExportEnabled(boolean enabled) {
        exportButton.setDisable(!enabled);
    }

    /** Durante a simulação o botão passa a STOP. */
    public void setSimulateRunning(boolean running) {
        simulateRunning = running;
        updateSimulateText();
    }

    private void updateSimulateText() {
        simulateButton.setText(Messages.get(simulateRunning ? "gui.stop" : "gui.simulate"));
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

    public void setTimer(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        timerValue.setText(
                String.format("%02d:%02d", minutes, remainingSeconds)
        );
    }

    public void setLastMoveTime(double seconds) {
        lastMoveTimeValue.setText(
                String.format("%.2f s", seconds)
        );
    }

    public void setPlayerAfloat(int afloat, int total) {
        playerAfloatCount = afloat;
        playerAfloatTotal = total;
        renderAfloat();
    }

    public void setEnemyAfloat(int afloat, int total) {
        enemyAfloatCount = afloat;
        enemyAfloatTotal = total;
        renderAfloat();
    }

    private void renderAfloat() {
        if (playerAfloatTotal > 0) {
            playerAfloat.setText(Messages.get("gui.afloat", playerAfloatCount, playerAfloatTotal));
        }
        if (enemyAfloatTotal > 0) {
            enemyAfloat.setText(Messages.get("gui.afloat", enemyAfloatCount, enemyAfloatTotal));
        }
    }

    public void showBanner(boolean victory) {
        String prefix = victory ? "gui.banner.victory" : "gui.banner.defeat";
        showBanner(() -> Messages.get(prefix + ".title"),
                () -> Messages.get(prefix + ".subtitle"),
                victory ? "banner-win" : "banner-lose");
    }

    /**
     * Os textos são Supplier para poderem ser reescritos se o idioma mudar com o banner visível.
     *
     * @param styleClass banner-win (dourado), banner-lose (vermelho) ou banner-neutral (azul)
     */
    public void showBanner(Supplier<String> title, Supplier<String> subtitle, String styleClass) {
        bannerTitleText = title;
        bannerSubtitleText = subtitle;
        bannerTitle.setText(title.get());
        bannerTitle.getStyleClass().setAll("banner-title", styleClass);
        bannerSubtitle.setText(subtitle.get());
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

    private Node header(Runnable onNewGame, Runnable onLanguage) {
        Label title = new Label("BATTLESHIP");
        title.getStyleClass().add("title");
        Label subtitle = label("gui.subtitle", "subtitle");

        HBox brand = new HBox(12, radarIcon(), new VBox(0, title, subtitle));
        brand.setAlignment(Pos.CENTER_LEFT);

        playerNameField.setPrefWidth(120);
        serverField.setPrefWidth(190);

        Button newGame = bind(new Button(), "gui.newGame");
        newGame.getStyleClass().add("secondary-button");
        newGame.setOnAction(e -> onNewGame.run());

        languageButton.getStyleClass().add("secondary-button");
        languageButton.setOnAction(e -> onLanguage.run());

        HBox connection = new HBox(8,
                label("gui.player", "field-caption"), playerNameField,
                label("gui.server", "field-caption"), serverField,
                newGame, languageButton);
        connection.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(brand, spacer, connection);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 14, 0));
        return header;
    }

    private Node panel(String titleKey, Label badge, BoardView board) {
        Label name = label(titleKey, "panel-title");
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

    private Node bottom(Runnable onFire, Runnable onSimulate,
                        Runnable onExportPdf) {
        statusText.getStyleClass().add("status-text");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setMinHeight(42);

        HBox stats = new HBox(22,
                stat("gui.shots", shotsValue),
                stat("gui.hits", hitsValue),
                stat("gui.accuracy", accuracyValue),
                stat("gui.turnTime", timerValue),
                stat("gui.lastTurn", lastMoveTimeValue));
        stats.setAlignment(Pos.CENTER_LEFT);

        HBox pipBox = new HBox(6);
        pipBox.setAlignment(Pos.CENTER);
        for (int i = 0; i < pips.length; i++) {
            pips[i] = new Circle(6, Color.TRANSPARENT);
            pips[i].setStroke(GOLD);
            pips[i].setStrokeWidth(2);
            pipBox.getChildren().add(pips[i]);
        }

        bind(fireButton, "gui.fire");
        fireButton.getStyleClass().add("fire-button");
        fireButton.setDisable(true);
        fireButton.setOnAction(e -> onFire.run());

        // O texto deste botão depende de simulateRunning: é escrito em refreshTexts().
        simulateButton.getStyleClass().add("secondary-button");
        simulateButton.setOnAction(e -> onSimulate.run());

        bind(exportButton, "gui.exportPdf");
        exportButton.getStyleClass().add("secondary-button");
        exportButton.setDisable(true);
        exportButton.setOnAction(e -> onExportPdf.run());

        HBox fireBox = new HBox(
                10, pipBox, fireButton, simulateButton, exportButton);
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

        Button again = bind(new Button(), "gui.newGame");
        again.getStyleClass().add("fire-button");
        again.setOnAction(e -> onNewGame.run());

        Button view = bind(new Button(), "gui.viewBoards");
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

    private VBox stat(String captionKey, Label value) {
        Label label = label(captionKey, "stat-caption");
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
                item(hit, "gui.legend.hit"), item(miss, "gui.legend.miss"),
                item(sunk, "gui.legend.sunk"), item(target, "gui.legend.target"),
                item(shot, "gui.legend.alreadyShot"));
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

    private HBox item(Node icon, String textKey) {
        StackPane holder = new StackPane(icon);
        holder.setMinSize(16, 16);
        holder.setMaxSize(16, 16);
        Label label = label(textKey, "legend-text");
        HBox box = new HBox(5, holder, label);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }
}