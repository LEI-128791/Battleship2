package gui;

import battleship.Fleet;
import battleship.Game;
import battleship.IFleet;
import battleship.IGame;
import battleship.IMove;
import battleship.IPosition;
import battleship.IShip;
import battleship.Messages;
import battleship.Position;
import battleship.PdfReportExporter;

import gui.GameView.Tone;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import java.io.File;
import java.nio.file.Path;

/**
 * Controlador da aplicação: liga o servidor (jogador -> AI), o Game existente (AI -> jogador)
 * e a vista. Não decide acertos; só desenha o que lhe dizem e conta estatísticas.
 */
public class BattleshipGUI extends Application {

    private static final int SHOTS_PER_TURN = 3;
    private static final int ENEMY_SHIPS = 11;
    /** Pausa entre rajadas na simulação (a consola usa 3000 ms). */
    private static final int SIMULATION_DELAY_MS = Integer.getInteger("battleship.sim.delay", 1200);

    private final GameClient client = new GameClient();
    private final List<IPosition> selectedShots = new ArrayList<>();

    private IFleet playerFleet;
    private Game playerGame;
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

    // Simulação local (comando "simula" da consola): null quando parada.
    private Timeline simulation;
    private int simulatedSalvos;

    // Cronómetro do turno do jogador.
    private Timeline turnTimer;
    private long turnStartNanos;

    // Muda a cada jogo novo: respostas atrasadas de um jogo antigo são ignoradas.
    private int generation;


    private Stage primaryStage;

    // Última mensagem da barra de estado, guardada como Supplier para ser re-traduzida.
    private Supplier<String> lastStatus = () -> "";
    private Tone lastTone = Tone.INFO;

    private final List<PdfReportExporter.MoveReport> playerMoveHistory =
            new ArrayList<>();

    // ---------------------------------------------------------------- Arranque

    @Override
    public void start(Stage stage) {
        playerBoard = new BoardView(null);
        enemyBoard = new BoardView(this::onEnemyCellClicked);
        this.primaryStage = stage;
        view = new GameView(playerBoard, enemyBoard, SHOTS_PER_TURN,
                this::newGame, this::fire, this::simulate, this::exportPdf,
                this::toggleLanguage);

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

        newGame(); // liga-se ao servidor logo no arranque
    }

    // ---------------------------------------------------------------- Idioma

    private void toggleLanguage() {
        Messages.setLanguage(Messages.getLanguage().equals("pt") ? "en" : "pt");
        view.refreshTexts();
        view.setStatus(lastStatus.get(), lastTone); // volta a traduzir a mensagem atual
    }

    // ---------------------------------------------------------------- Novo jogo

    private void resetLocalGame() {
        generation++;
        stopSimulation();


        if (callbackServer != null) {
            callbackServer.stop();
        }

        playerFleet = Fleet.createRandom();
        playerShips = playerFleet.getShips().size();
        playerGame = new Game(playerFleet);
        callbackServer = new CallbackServer(playerGame, this::showAiShot, this::showDefeat);

        try {
            callbackServer.start();
        } catch (IOException e) {
            System.err.println("[GUI] Cannot start callback server: " + e);
            error(() -> Messages.get("gui.callbackError", e.getMessage()));
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
        playerMoveHistory.clear();
        enemyShipTypes = new String[BoardView.SIZE][BoardView.SIZE];
        for (IShip ship : playerFleet.getShips()) {
            playerBoard.addShip(cellsOf(ship), false);
        }

        view.hideBanner();
        stopTurnTimer();      // <-- novo
        view.setTimer(0);     // <-- novo
        updateStats();
        updateControls();
    }

    private void newGame() {
        resetLocalGame();
        final int gen = generation;
        waiting = true;
        info(() -> Messages.get("gui.connecting"));

        client.register(view.serverUrl(), view.playerName(), callbackServer.getPort())
                .whenComplete((ignored, err) -> Platform.runLater(() -> {
                    if (gen != generation) {
                        return;
                    }
                    waiting = false;
                    if (err != null) {
                        final String msg = message(err);
                        System.err.println("[GUI] register failed: " + msg);
                        error(() -> Messages.get("gui.connectionError", msg));
                        return;
                    }
                    registered = true;
                    info(() -> Messages.get("gui.gameStarted", SHOTS_PER_TURN));
                    startTurnTimer();
                    updateControls();
                }));
    }

    // ---------------------------------------------------------------- Exportar PDF

    private void exportPdf() {
        if (playerMoveHistory.isEmpty()) {
            warn(() -> Messages.get("gui.noMoves"));
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle(Messages.get("gui.exportTitle"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        Messages.get("gui.pdfFilter"), "*.pdf"));
        chooser.setInitialFileName("battleship-history.pdf");

        File selectedFile = chooser.showSaveDialog(primaryStage);

        if (selectedFile == null) {
            return;
        }

        try {
            Path actualPath = PdfReportExporter.exportHistory(
                    playerMoveHistory, selectedFile.toPath());

            info(() -> Messages.get("pdf.export.success")
                    + " " + actualPath.toAbsolutePath());
        } catch (IOException e) {
            error(() -> Messages.get("pdf.export.error") + " " + e.getMessage());
        }
    }


    // ---------------------------------------------------------------- Seleção e tiro

    private void onEnemyCellClicked(int row, int column) {
        if (gameOver) {
            return;
        }
        if (simulation != null) {
            warn(() -> Messages.get("gui.simRunning"));
            return;
        }
        if (!registered) {
            warn(() -> Messages.get("gui.notConnected"));
            return;
        }
        if (waiting) {
            warn(() -> Messages.get("gui.waitingServer"));
            return;
        }

        CellState state = enemyBoard.get(row, column);

        // Clicar numa célula já selecionada desfaz a seleção.
        if (state == CellState.SELECTED) {
            enemyBoard.set(row, column, CellState.WATER);
            selectedShots.removeIf(p -> p.getRow() == row && p.getColumn() == column);
            final int count = selectedShots.size();
            info(() -> Messages.get("gui.selectedCount", count, SHOTS_PER_TURN));
            updateControls();
            return;
        }
        if (state != CellState.WATER) {
            warn(() -> Messages.get("gui.alreadyShot"));
            return;
        }
        if (selectedShots.size() >= SHOTS_PER_TURN) {
            warn(() -> Messages.get("gui.alreadySelected", SHOTS_PER_TURN));
            return;
        }

        IPosition position = new Position(row, column);
        selectedShots.add(position);
        enemyBoard.set(row, column, CellState.SELECTED);
        final String coordinate = "" + position.getClassicRow() + position.getClassicColumn();
        final int count = selectedShots.size();
        info(() -> Messages.get("gui.selectedPos", coordinate, count, SHOTS_PER_TURN));
        updateControls();
    }

    private void fire() {
        if (!canFire()) {
            return;
        }

        waiting = true;
        updateControls();
        info(() -> Messages.get("gui.firing"));

        // O servidor responde pela mesma ordem em que enviámos os tiros.
        final List<IPosition> sent = new ArrayList<>(selectedShots);
        final int gen = generation;
        final double turnSeconds = elapsedSeconds();

        client.fire(sent).whenComplete((turn, err) -> Platform.runLater(() -> {
            if (gen != generation) {
                return; // resposta de um jogo anterior
            }
            try {
                handleTurn(sent, turn, err);
                if (err == null) {
                    view.setLastMoveTime(turnSeconds);
                    if (!gameOver) {
                        startTurnTimer();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                final String description = String.valueOf(e);
                error(() -> Messages.get("gui.guiError", description));
            } finally {
                waiting = false;
                updateControls();
            }
        }));
    }

    private void handleTurn(List<IPosition> sent, GameClient.TurnResult turn, Throwable err) {
        if (err != null) {
            final String msg = message(err);
            System.err.println("[GUI] fire failed: " + msg);
            if (!gameOver) {
                error(() -> msg); // a seleção mantém-se para tentar de novo
            }
            return;
        }
        recordPlayerTurn(sent, turn.results());

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

        final int h = hitCount;
        final int m = missCount;
        final int rep = repeated;
        final String sunk = sunkName;
        Supplier<String> next = () -> {
            String summary = Messages.get("gui.turnSummary", h, m);
            if (sunk != null) {
                summary = Messages.get("gui.enemySunk", sunk) + " " + summary;
            }
            if (rep > 0) {
                summary += ", " + Messages.get("gui.repeated", rep);
            }
            return summary + " " + Messages.get("gui.selectPositions", SHOTS_PER_TURN);
        };
        if (repeated > 0) {
            warn(next);
        } else {
            info(next);
        }
    }


    private void recordPlayerTurn(
            List<IPosition> sent,
            List<GameClient.ShotResult> results) {

        List<PdfReportExporter.ShotReport> reportShots =
                new ArrayList<>();

        for (int i = 0; i < sent.size(); i++) {
            IPosition position = sent.get(i);

            String coordinate = ""
                    + position.getClassicRow()
                    + position.getClassicColumn();

            String outcome = i < results.size()
                    ? describeShotResult(results.get(i))
                    : Messages.get("pdf.result.unavailable");

            reportShots.add(
                    new PdfReportExporter.ShotReport(coordinate, outcome));
        }

        int moveNumber = playerMoveHistory.size() + 1;

        playerMoveHistory.add(
                new PdfReportExporter.MoveReport(moveNumber, reportShots));
    }

    /** O histórico fica no idioma em que foi gerado (é guardado como String). */
    private String describeShotResult(GameClient.ShotResult result) {
        String shipType = result.shipType();
        boolean hasType = shipType != null && !shipType.isBlank();

        return switch (result.outcome()) {
            case INVALID -> Messages.get("pdf.shot.invalid");
            case REPEATED -> Messages.get("pdf.shot.repeated");
            case MISS -> Messages.get("pdf.shot.water");
            case HIT -> hasType
                    ? Messages.get("pdf.shot.hitType", shipType)
                    : Messages.get("pdf.shot.hit");
            case SUNK -> hasType
                    ? Messages.get("pdf.shot.sunkType", shipType)
                    : Messages.get("pdf.shot.sunk");
        };
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
        endGame(true, () -> Messages.get("gui.victory"), Tone.WIN);
    }

    private void showDefeat() {
        if (!gameOver) {
            endGame(false, () -> Messages.get("gui.defeat"), Tone.LOSE);
        }
    }

    private void endGame(boolean victory, Supplier<String> text, Tone tone) {
        gameOver = true;
        stopTurnTimer();
        selectedShots.clear();
        enemyBoard.clearSelection();
        updateControls();
        status(text, tone);
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

    // ---------------------------------------------------------------- Simulação

    /**
     * Igual ao "simula" do Main/Tasks: um inimigo aleatório dispara rajadas de 3 tiros contra a
     * MINHA frota (Game.randomEnemyFire) até afundar tudo. Corre só localmente, sem servidor.
     * Carregar outra vez no botão (STOP) pára a simulação.
     */
    private void simulate() {
        if (simulation != null) {
            stopSimulation();
            info(() -> Messages.get("gui.simStopped"));
            return;
        }

        resetLocalGame(); // frota nova, tabuleiros limpos, sem ligação ao servidor
        simulatedSalvos = 0;
        view.setSimulateRunning(true);
        info(() -> Messages.get("gui.simStarted"));

        simulation = new Timeline(new KeyFrame(Duration.millis(SIMULATION_DELAY_MS), e -> simulationStep()));
        simulation.setCycleCount(Animation.INDEFINITE);
        simulation.play();
    }

    private void simulationStep() {
        if (playerGame.getRemainingShips() == 0) {
            finishSimulation();
            return;
        }

        playerGame.randomEnemyFire(); // aplica 3 tiros aleatórios à frota (como na consola)
        simulatedSalvos++;

        // O Game guardou a jogada: lemos dela o que aconteceu a cada tiro.
        List<IMove> moves = playerGame.getAlienMoves();
        IMove move = moves.get(moves.size() - 1);

        int hitCount = 0;
        int missCount = 0;
        String sunkName = null;

        for (int i = 0; i < move.getShots().size() && i < move.getShotResults().size(); i++) {
            IPosition shot = move.getShots().get(i);
            IGame.ShotResult result = move.getShotResults().get(i);
            Outcome outcome = Outcome.of(result);

            if (!outcome.isShotOnBoard()) {
                continue; // repetido / inválido
            }
            shots++;
            if (outcome == Outcome.MISS) {
                missCount++;
            } else {
                hits++;
                hitCount++;
            }
            if (outcome == Outcome.SUNK) {
                sunkName = result.ship().getCategory();
            }
            showAiShot(new CallbackServer.ShotUpdate(shot.getRow(), shot.getColumn(), outcome));
        }

        final int salvo = simulatedSalvos;
        final int h = hitCount;
        final int m = missCount;
        final String sunk = sunkName;
        info(() -> {
            String summary = Messages.get("gui.turnSummary", h, m);
            return sunk == null
                    ? Messages.get("gui.simSalvo", salvo, summary)
                    : Messages.get("gui.simSalvoSunk", salvo, sunk, summary);
        });

        if (playerGame.getRemainingShips() == 0) {
            finishSimulation();
        }
    }

    private void finishSimulation() {
        stopSimulation();
        gameOver = true;
        updateControls();
        final int salvos = simulatedSalvos;
        status(() -> Messages.get("gui.simComplete.status", salvos), Tone.INFO);
        view.showBanner(() -> Messages.get("gui.simComplete.title"),
                () -> Messages.get("gui.simComplete.subtitle", salvos),
                "banner-neutral");
    }

    private void stopSimulation() {
        if (simulation != null) {
            simulation.stop();
            simulation = null;
        }
        if (view != null) {
            view.setSimulateRunning(false);
        }
    }
    private double elapsedSeconds() {
        return (System.nanoTime() - turnStartNanos) / 1_000_000_000.0;
    }

    private void startTurnTimer() {
        stopTurnTimer();
        turnStartNanos = System.nanoTime();
        view.setTimer(0);
        turnTimer = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> view.setTimer((long) elapsedSeconds())));
        turnTimer.setCycleCount(Animation.INDEFINITE);
        turnTimer.play();
    }

    private void stopTurnTimer() {
        if (turnTimer != null) {
            turnTimer.stop();
            turnTimer = null;
        }
    }

    private boolean canFire() {
        return registered && !waiting && !gameOver && selectedShots.size() == SHOTS_PER_TURN;
    }

    private void updateControls() {
        view.setFireEnabled(canFire());
        view.setSimulateEnabled(!waiting);
        view.setSelected(selectedShots.size());
        view.setExportEnabled(!playerMoveHistory.isEmpty());
    }

    private void updateStats() {
        view.setStats(shots, hits);
        view.setEnemyAfloat(enemyShipsRemaining, ENEMY_SHIPS);
        view.setPlayerAfloat(playerFleet.getFloatingShips().size(), playerShips);
    }

    // ---------------------------------------------------------------- Barra de estado

    private void status(Supplier<String> text, Tone tone) {
        lastStatus = text;
        lastTone = tone;
        view.setStatus(text.get(), tone);
    }

    private void info(Supplier<String> text) {
        status(text, Tone.INFO);
    }

    private void warn(Supplier<String> text) {
        status(text, Tone.WARN);
    }

    private void error(Supplier<String> text) {
        status(text, Tone.ERROR);
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