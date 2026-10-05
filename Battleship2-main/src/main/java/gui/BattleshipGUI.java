package gui;

import battleship.Fleet;
import battleship.Game;
import battleship.IFleet;
import battleship.IPosition;
import battleship.IShip;
import battleship.Position;

import gui.GameView.Tone;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletionException;

/**
 * Controlador da aplicação: liga o servidor (jogador -> AI), o Game existente (AI -> jogador)
 * e a vista. Não decide acertos; só desenha o que lhe dizem e conta estatísticas.
 */
public class BattleshipGUI extends Application {

    private static final int SHOTS_PER_TURN = 3;
    private static final int ENEMY_SHIPS = 11;

    private final GameClient client = new GameClient();
    private final List<IPosition> selectedShots = new ArrayList<>();

    private IFleet playerFleet;
    private int playerShips;
    private CallbackServer callbackServer;

    private BoardView playerBoard;
    private BoardView enemyBoard;
    private GameView view;

    // Tipo de navio de cada célula acertada (vem do servidor) para revelar o navio ao afundar.
    private String[][] enemyShipTypes = new String[BoardView.SIZE][BoardView.SIZE];

    private int shots;
    private int hits;
    private int enemyShipsRemaining = ENEMY_SHIPS;

    private boolean registered; // o servidor aceitou o registo
    private boolean waiting;    // há um pedido ao servidor em curso
    private boolean gameOver;

    // Muda a cada jogo novo: respostas atrasadas de um jogo antigo são ignoradas.
    private int generation;

    // ---------------------------------------------------------------- Arranque

    @Override
    public void start(Stage stage) {
        playerBoard = new BoardView(null);
        enemyBoard = new BoardView(this::onEnemyCellClicked);
        view = new GameView(playerBoard, enemyBoard, SHOTS_PER_TURN, this::newGame, this::fire);

        Scene scene = new Scene(view.getRoot());
        URL css = getClass().getResource("battleship.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            System.err.println("[GUI] battleship.css not found (src/main/resources/gui/battleship.css)");
        }

        stage.setTitle("Battleship");
        stage.setScene(scene);
        stage.show();
        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());

        newGame();
    }

    // ---------------------------------------------------------------- Novo jogo

    private void resetLocalGame() {
        generation++;

        if (callbackServer != null) {
            callbackServer.stop();
        }

        playerFleet = Fleet.createRandom();
        playerShips = playerFleet.getShips().size();
        callbackServer = new CallbackServer(new Game(playerFleet), this::showAiShot, this::showDefeat);

        try {
            callbackServer.start();
        } catch (IOException e) {
            System.err.println("[GUI] Cannot start callback server: " + e);
            error("Cannot start callback server: " + e.getMessage());
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
        for (IShip ship : playerFleet.getShips()) {
            playerBoard.addShip(cellsOf(ship), false);
        }

        view.hideBanner();
        updateStats();
        updateControls();
    }

    private void newGame() {
        resetLocalGame();
        final int gen = generation;
        waiting = true;
        info("Connecting to server...");

        client.register(view.serverUrl(), view.playerName(), callbackServer.getPort())
                .whenComplete((ignored, err) -> Platform.runLater(() -> {
                    if (gen != generation) {
                        return;
                    }
                    waiting = false;
                    if (err != null) {
                        System.err.println("[GUI] register failed: " + message(err));
                        error("Connection error: " + message(err));
                        return;
                    }
                    registered = true;
                    info("Game started. Select " + SHOTS_PER_TURN + " positions.");
                    updateControls();
                }));
    }

    // ---------------------------------------------------------------- Seleção e tiro

    private void onEnemyCellClicked(int row, int column) {
        if (gameOver) {
            return;
        }
        if (!registered) {
            warn("Not connected. Press NEW GAME first.");
            return;
        }
        if (waiting) {
            warn("Waiting for the server...");
            return;
        }

        CellState state = enemyBoard.get(row, column);

        // Clicar numa célula já selecionada desfaz a seleção.
        if (state == CellState.SELECTED) {
            enemyBoard.set(row, column, CellState.WATER);
            selectedShots.removeIf(p -> p.getRow() == row && p.getColumn() == column);
            info("Selected " + selectedShots.size() + "/" + SHOTS_PER_TURN);
            updateControls();
            return;
        }
        if (state != CellState.WATER) {
            warn("That position was already shot.");
            return;
        }
        if (selectedShots.size() >= SHOTS_PER_TURN) {
            warn("Already " + SHOTS_PER_TURN + " selected. Press FIRE or click one to undo.");
            return;
        }

        IPosition position = new Position(row, column);
        selectedShots.add(position);
        enemyBoard.set(row, column, CellState.SELECTED);
        info("Selected " + position.getClassicRow() + position.getClassicColumn()
                + " (" + selectedShots.size() + "/" + SHOTS_PER_TURN + ")");
        updateControls();
    }

    private void fire() {
        if (!canFire()) {
            return;
        }

        waiting = true;
        updateControls();
        info("Firing...");

        // O servidor responde pela mesma ordem em que enviámos os tiros.
        final List<IPosition> sent = new ArrayList<>(selectedShots);
        final int gen = generation;

        client.fire(sent).whenComplete((turn, err) -> Platform.runLater(() -> {
            if (gen != generation) {
                return; // resposta de um jogo anterior
            }
            try {
                handleTurn(sent, turn, err);
            } catch (Exception e) {
                e.printStackTrace();
                error("GUI error: " + e);
            } finally {
                waiting = false;
                updateControls();
            }
        }));
    }

    private void handleTurn(List<IPosition> sent, GameClient.TurnResult turn, Throwable err) {
        if (err != null) {
            System.err.println("[GUI] fire failed: " + message(err));
            if (!gameOver) {
                error(message(err)); // a seleção mantém-se para tentar de novo
            }
            return;
        }

        int hitCount = 0;
        int missCount = 0;
        int repeated = 0;
        String sunkName = null;

        List<GameClient.ShotResult> results = turn.results();
        for (int i = 0; i < results.size() && i < sent.size(); i++) {
            Outcome outcome = results.get(i).outcome();
            String shipType = results.get(i).shipType();
            int row = sent.get(i).getRow();
            int column = sent.get(i).getColumn();

            switch (outcome) {
                case REPEATED -> repeated++;
                case MISS -> {
                    shots++;
                    missCount++;
                }
                case HIT, SUNK -> {
                    shots++;
                    hits++;
                    hitCount++;
                    enemyShipTypes[row][column] = shipType;
                }
                default -> { }
            }

            CellState state = CellState.of(outcome);
            if (state != null) {
                enemyBoard.set(row, column, state);
            }
            if (outcome == Outcome.SUNK) {
                sunkName = shipType;
                sinkEnemyShip(row, column, shipType);
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

        String summary = hitCount + (hitCount == 1 ? " hit, " : " hits, ")
                + missCount + (missCount == 1 ? " miss" : " misses");
        if (sunkName != null) {
            summary = "Enemy " + sunkName + " sunk! " + summary;
        }
        if (repeated > 0) {
            summary += ", " + repeated + " already shot";
        }
        String next = summary + ". Select " + SHOTS_PER_TURN + " positions.";
        if (repeated > 0) {
            warn(next);
        } else {
            info(next);
        }
    }

    /** Revela o navio inimigo afundado: todas as células acertadas do mesmo tipo ligadas à última. */
    private void sinkEnemyShip(int row, int column, String type) {
        List<int[]> cells = new ArrayList<>();
        cells.add(new int[]{row, column});

        if (type != null && !type.isEmpty()) {
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
                        cells.add(new int[]{r, c});
                        queue.add(new int[]{r, c});
                    }
                }
            }
        }
        enemyBoard.addShip(cells, true);
    }

    // ---------------------------------------------------------------- Tiros do AI

    private void showAiShot(CallbackServer.ShotUpdate update) {
        CellState state = CellState.of(update.outcome());
        if (state != null) {
            playerBoard.set(update.row(), update.column(), state);
        }
        if (update.outcome() == Outcome.SUNK) {
            sinkPlayerShip(update.row(), update.column());
        }
        updateStats();
    }

    private void sinkPlayerShip(int row, int column) {
        IPosition target = new Position(row, column);
        for (IShip ship : playerFleet.getShips()) {
            if (ship.occupies(target)) {
                List<int[]> cells = cellsOf(ship);
                cells.forEach(c -> playerBoard.set(c[0], c[1], CellState.SUNK));
                playerBoard.addShip(cells, true);
                return;
            }
        }
    }

    // ---------------------------------------------------------------- Fim de jogo

    private void showVictory() {
        endGame(true, "VICTORY — all enemy ships sunk!", Tone.WIN);
    }

    private void showDefeat() {
        if (!gameOver) {
            endGame(false, "DEFEAT — your fleet has been destroyed.", Tone.LOSE);
        }
    }

    private void endGame(boolean victory, String text, Tone tone) {
        gameOver = true;
        selectedShots.clear();
        enemyBoard.clearSelection();
        updateControls();
        view.setStatus(text, tone);
        view.showBanner(victory);
    }

    // ---------------------------------------------------------------- Auxiliares

    private static List<int[]> cellsOf(IShip ship) {
        List<int[]> cells = new ArrayList<>();
        for (IPosition p : ship.getPositions()) {
            cells.add(new int[]{p.getRow(), p.getColumn()});
        }
        return cells;
    }

    private boolean canFire() {
        return registered && !waiting && !gameOver && selectedShots.size() == SHOTS_PER_TURN;
    }

    private void updateControls() {
        view.setFireEnabled(canFire());
        view.setSelected(selectedShots.size());
    }

    private void updateStats() {
        view.setStats(shots, hits);
        view.setEnemyAfloat(enemyShipsRemaining, ENEMY_SHIPS);
        view.setPlayerAfloat(playerFleet.getFloatingShips().size(), playerShips);
    }

    private void info(String text) {
        view.setStatus(text, Tone.INFO);
    }

    private void warn(String text) {
        view.setStatus(text, Tone.WARN);
    }

    private void error(String text) {
        view.setStatus(text, Tone.ERROR);
    }

    private String message(Throwable err) {
        Throwable cause = (err instanceof CompletionException && err.getCause() != null)
                ? err.getCause() : err;
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