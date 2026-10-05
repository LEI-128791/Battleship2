package gui;

import battleship.Fleet;
import battleship.Game;
import battleship.IFleet;
import battleship.IPosition;
import battleship.IShip;
import battleship.Position;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletionException;

/**
 * Só interface: mostra o que o Game (AI -> jogador) e o servidor (jogador -> AI)
 * decidem. Não decide acertos; apenas desenha e conta estatísticas.
 */
public class BattleshipGUI extends Application {

    private static final int SHOTS_PER_TURN = 3;
    private static final int ENEMY_SHIPS = 11;

    private static final String STATUS_STYLE =
            "-fx-text-fill: #D9EAF2; -fx-font-size: 14px; -fx-font-weight: bold;";

    private final GameClient client = new GameClient();
    private final List<IPosition> selectedShots = new ArrayList<>();

    private IFleet playerFleet;
    private CallbackServer callbackServer;

    private BoardView playerBoard;
    private BoardView enemyBoard;

    // Tipo de navio de cada célula acertada (vem do servidor) para pintar o navio todo ao afundar.
    private String[][] enemyShipTypes = new String[BoardView.SIZE][BoardView.SIZE];

    private int shots;
    private int hits;
    private int enemyShipsRemaining = ENEMY_SHIPS;

    // Três flags independentes (antes havia só "myTurn", que podia ficar preso a false).
    private boolean registered; // o servidor aceitou o registo
    private boolean waiting;    // há um pedido ao servidor em curso
    private boolean gameOver;

    // Muda a cada jogo novo: respostas atrasadas de um jogo antigo são ignoradas.
    private int generation;

    private Label statusLabel;
    private Label shotsLabel;
    private Label hitsLabel;
    private Label shipsLabel;
    private Button fireButton;
    private TextField serverField;
    private TextField playerNameField;

    // ---------------------------------------------------------------- UI

    @Override
    public void start(Stage stage) {
        playerBoard = new BoardView(null);
        enemyBoard = new BoardView(this::onEnemyCellClicked);

        Label title = new Label("⚓ BATTLESHIP");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        playerNameField = new TextField("Player");
        playerNameField.setPrefWidth(130);
        serverField = new TextField("http://localhost:8080");
        serverField.setPrefWidth(210);

        HBox connection = new HBox(8,
                light("Player:"), playerNameField,
                light("Server:"), serverField);
        connection.setAlignment(Pos.CENTER);

        VBox top = new VBox(6, title, connection);
        top.setAlignment(Pos.CENTER);

        HBox boards = new HBox(25,
                boardBox("YOUR FLEET", playerBoard),
                boardBox("ENEMY WATERS", enemyBoard));
        boards.setAlignment(Pos.CENTER);

        shotsLabel = light("");
        hitsLabel = light("");
        shipsLabel = light("");
        HBox stats = new HBox(25, shotsLabel, hitsLabel, shipsLabel);
        stats.setAlignment(Pos.CENTER);

        statusLabel = new Label();
        statusLabel.setMinSize(620, 24);
        statusLabel.setAlignment(Pos.CENTER);

        fireButton = new Button("FIRE");
        fireButton.setPrefWidth(100);
        fireButton.setOnAction(e -> fire());

        Button newGame = new Button("NEW GAME");
        newGame.setPrefWidth(100);
        newGame.setOnAction(e -> newGame());

        HBox buttons = new HBox(10, fireButton, newGame);
        buttons.setAlignment(Pos.CENTER);

        VBox bottom = new VBox(8, stats, statusLabel, buttons);
        bottom.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(boards);
        root.setBottom(bottom);
        BorderPane.setMargin(top, new Insets(10));
        BorderPane.setMargin(bottom, new Insets(10));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #082B42, #0D4664, #092A40);");

        stage.setTitle("Battleship");
        stage.setScene(new Scene(root));
        stage.show();
        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());

        resetLocalGame();
        setStatus("Press NEW GAME to start.");
    }

    private VBox boardBox(String title, BoardView board) {
        Label label = light(title);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox box = new VBox(6, label, board.getGrid());
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 10;");
        return box;
    }

    private Label light(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        return label;
    }

    // ------------------------------------------------------- Novo jogo

    private void resetLocalGame() {
        generation++;
        if (callbackServer != null) {
            callbackServer.stop();
        }

        playerFleet = Fleet.createRandom();
        callbackServer = new CallbackServer(new Game(playerFleet), this::showAiShot, this::showDefeat);

        try {
            callbackServer.start();
        } catch (IOException e) {
            System.err.println("[GUI] Cannot start callback server: " + e);
            setStatus("Cannot start callback server: " + e.getMessage());
        }

        selectedShots.clear();
        shots = 0;
        hits = 0;
        enemyShipsRemaining = ENEMY_SHIPS;
        registered = false;
        waiting = false;
        gameOver = false;

        playerBoard.reset();
        enemyBoard.reset();
        enemyShipTypes = new String[BoardView.SIZE][BoardView.SIZE];
        drawFleet();
        updateStats();
        updateFireButton();
    }

    private void newGame() {
        resetLocalGame();
        final int gen = generation;
        waiting = true;
        setStatus("Connecting to server...");

        client.register(serverField.getText(), playerNameField.getText(), callbackServer.getPort())
                .whenComplete((ignored, error) -> Platform.runLater(() -> {
                    if (gen != generation) {
                        return;
                    }
                    waiting = false;
                    if (error != null) {
                        System.err.println("[GUI] register failed: " + message(error));
                        setStatus("Connection error: " + message(error));
                        return;
                    }
                    registered = true;
                    setStatus("Game started. Select " + SHOTS_PER_TURN + " positions.");
                    updateFireButton();
                }));
    }

    private void drawFleet() {
        for (IShip ship : playerFleet.getShips()) {
            for (IPosition p : ship.getPositions()) {
                playerBoard.set(p.getRow(), p.getColumn(), CellState.SHIP);
            }
        }
    }

    // ------------------------------------------------- Seleção e tiro

    private void onEnemyCellClicked(int row, int column) {
        // Nunca ignorar um clique em silêncio: dizer porquê.
        if (gameOver) {
            return;
        }
        if (!registered) {
            setStatus("Not connected. Press NEW GAME first.");
            return;
        }
        if (waiting) {
            setStatus("Waiting for the server...");
            return;
        }

        CellState state = enemyBoard.get(row, column);

        // Clicar numa célula já selecionada desfaz a seleção.
        if (state == CellState.SELECTED) {
            enemyBoard.set(row, column, CellState.WATER);
            selectedShots.removeIf(p -> p.getRow() == row && p.getColumn() == column);
            setStatus("Selected " + selectedShots.size() + "/" + SHOTS_PER_TURN);
            updateFireButton();
            return;
        }

        if (state != CellState.WATER) {
            setStatus("That position was already shot.");
            return;
        }
        if (selectedShots.size() >= SHOTS_PER_TURN) {
            setStatus("Already " + SHOTS_PER_TURN + " selected. Press FIRE or click one to undo.");
            return;
        }

        IPosition position = new Position(row, column);
        selectedShots.add(position);
        enemyBoard.set(row, column, CellState.SELECTED);
        setStatus("Selected " + position.getClassicRow() + position.getClassicColumn()
                + " (" + selectedShots.size() + "/" + SHOTS_PER_TURN + ")");
        updateFireButton();
    }

    private void fire() {
        if (!canFire()) {
            return;
        }

        waiting = true;
        updateFireButton();
        setStatus("Firing...");

        // O servidor responde pela mesma ordem em que enviámos os tiros.
        final List<IPosition> sent = new ArrayList<>(selectedShots);
        final int gen = generation;

        client.fire(sent).whenComplete((turn, error) -> Platform.runLater(() -> {
            if (gen != generation) {
                return; // resposta de um jogo anterior
            }
            try {
                handleTurn(sent, turn, error);
            } catch (Exception e) {
                // Antes, uma exceção aqui era engolida e a GUI ficava "morta".
                e.printStackTrace();
                setStatus("GUI error: " + e);
            } finally {
                waiting = false;      // nunca fica preso
                updateFireButton();
            }
        }));
    }

    private void handleTurn(List<IPosition> sent, GameClient.TurnResult turn, Throwable error) {
        if (error != null) {
            System.err.println("[GUI] fire failed: " + message(error));
            if (!gameOver) {
                setStatus(message(error)); // a seleção mantém-se para tentar de novo
            }
            return;
        }

        System.out.println("[GUI] results=" + turn.results()
                + " shipsRemaining=" + turn.shipsRemaining()
                + " status=" + turn.gameStatus());

        int repeated = 0;
        List<GameClient.ShotResult> results = turn.results();

        // O servidor responde pela mesma ordem em que enviámos: resultado i = tiro i.
        for (int i = 0; i < results.size() && i < sent.size(); i++) {
            Outcome outcome = results.get(i).outcome();
            String shipType = results.get(i).shipType();
            int row = sent.get(i).getRow();
            int column = sent.get(i).getColumn();

            if (outcome == Outcome.REPEATED) {
                repeated++;
            } else if (outcome.isShotOnBoard()) {
                shots++;
                if (outcome != Outcome.MISS) {
                    hits++;
                }
            }

            CellState state = CellState.of(outcome);
            if (state != null) {
                enemyBoard.set(row, column, state);
            }

            if (outcome == Outcome.HIT || outcome == Outcome.SUNK) {
                enemyShipTypes[row][column] = shipType;
            }
            if (outcome == Outcome.SUNK) {
                markWholeShipSunk(row, column, shipType);
            }
        }

        enemyBoard.clearSelection();
        selectedShots.clear();

        if (turn.shipsRemaining() >= 0) {
            enemyShipsRemaining = turn.shipsRemaining();
        }
        updateStats();

        // O servidor chama o callback do AI ANTES de responder: se o AI já ganhou,
        // os nossos tiros ficam pintados mas o ecrã de derrota mantém-se.
        if (gameOver) {
            return;
        }

        if (turn.gameOver()) {
            if (turn.playerWon()) {
                showVictory();
            } else {
                showDefeat();
            }
            return;
        }

        setStatus(repeated > 0
                ? repeated + " position(s) had already been shot. Select " + SHOTS_PER_TURN + " positions."
                : "Select " + SHOTS_PER_TURN + " positions.");
    }

    /** Quando um navio afunda, pinta como afundadas todas as células acertadas desse navio. */
    private void markWholeShipSunk(int row, int column, String type) {
        if (type == null || type.isEmpty()) {
            return;
        }
        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{row, column});

        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                int r = cell[0] + d[0];
                int c = cell[1] + d[1];
                if (r < 0 || c < 0 || r >= BoardView.SIZE || c >= BoardView.SIZE) {
                    continue;
                }
                if (enemyBoard.get(r, c) == CellState.HIT && type.equals(enemyShipTypes[r][c])) {
                    enemyBoard.set(r, c, CellState.SUNK);
                    queue.add(new int[]{r, c});
                }
            }
        }
    }

    // ------------------------------------------------ Tiros do AI

    private void showAiShot(CallbackServer.ShotUpdate update) {
        System.out.println("[GUI] AI shot: " + update);

        CellState state = CellState.of(update.outcome());

        if (state != null) {
            playerBoard.set(update.row(), update.column(), state);
        }
    }

    // ------------------------------------------------ Fim de jogo

    private void showVictory() {
        endGame("🏆 VICTORY — ALL ENEMY SHIPS SUNK!", "#FFD54A");
    }

    private void showDefeat() {
        if (!gameOver) {
            endGame("☠ DEFEAT — YOUR FLEET HAS BEEN DESTROYED", "#FF6B6B");
        }
    }

    private void endGame(String text, String color) {
        gameOver = true;
        selectedShots.clear();
        enemyBoard.clearSelection();
        updateFireButton();
        statusLabel.setText(text);
        statusLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
    }

    // ------------------------------------------------ Auxiliares

    private boolean canFire() {
        return registered && !waiting && !gameOver && selectedShots.size() == SHOTS_PER_TURN;
    }

    private void updateStats() {
        shotsLabel.setText("SHOTS: " + shots);
        hitsLabel.setText("HITS: " + hits);
        shipsLabel.setText("ENEMY SHIPS REMAINING: " + enemyShipsRemaining);
    }

    private void updateFireButton() {
        fireButton.setDisable(!canFire());
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
        statusLabel.setStyle(STATUS_STYLE);
    }

    private String message(Throwable error) {
        Throwable cause = (error instanceof CompletionException && error.getCause() != null)
                ? error.getCause() : error;
        return String.valueOf(cause.getMessage());
    }

    @Override
    public void stop() {
        if (callbackServer != null) {
            callbackServer.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
