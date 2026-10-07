package gui;

import battleship.IGame;
import battleship.IPosition;
import battleship.Position;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import javafx.application.Platform;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Recebe os tiros do AI e entrega-os ao Game existente.
 * Não decide nada: só traduz JSON <-> Game e avisa a GUI.
 */
public class CallbackServer {

    private static final int PORT = 8091;

    public record ShotUpdate(int row, int column, Outcome outcome) {}

    private final ObjectMapper mapper = new ObjectMapper();
    private final IGame game;
    private final Consumer<ShotUpdate> onShot;
    private final Runnable onDefeat;

    private HttpServer server;

    public CallbackServer(IGame game, Consumer<ShotUpdate> onShot, Runnable onDefeat) {
        this.game = game;
        this.onShot = onShot;
        this.onDefeat = onDefeat;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/game", this::handleShots);
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public int getPort() {
        return PORT;
    }

    private void handleShots(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {
            JsonNode shots = mapper.readTree(exchange.getRequestBody()).get("shots");

            if (shots == null || !shots.isArray()) {
                send(exchange, 400, "{\"error\":\"shots required\"}");
                return;
            }

            List<ShotUpdate> updates = new ArrayList<>();
            List<IPosition> firedThisTurn = new ArrayList<>();
            ArrayNode results = mapper.createArrayNode();

            for (JsonNode shot : shots) {
                IPosition position = new Position(shot.get("row").asText().charAt(0),
                        shot.get("column").asInt());

                // Mesmo critério do servidor: a mesma posição duas vezes no mesmo pedido é REPEATED.
                Outcome outcome = Outcome.of(game.fireSingleShot(position, firedThisTurn.contains(position)));
                firedThisTurn.add(position);

                ObjectNode json = results.addObject();
                json.put("row", String.valueOf(position.getClassicRow()));
                json.put("column", position.getClassicColumn());
                json.put("outcome", outcome.name());

                // Só tiros válidos mexem no desenho (evita índices fora do tabuleiro).
                if (outcome.isShotOnBoard()) {
                    updates.add(new ShotUpdate(position.getRow(), position.getColumn(), outcome));
                }
            }

            int remaining = game.getRemainingShips();

            ObjectNode response = mapper.createObjectNode();
            response.set("results", results);
            response.put("shipsRemaining", remaining);
            response.put("gameStatus", remaining == 0 ? "GAME_OVER" : "ONGOING");
            if (remaining == 0) {
                response.put("winner", "AI_WINS");
            }

            Platform.runLater(() -> {
                updates.forEach(onShot);
                if (remaining == 0) {
                    onDefeat.run();
                }
            });

            send(exchange, 200, response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            ObjectNode error = mapper.createObjectNode();
            error.put("error", String.valueOf(e.getMessage()));
            send(exchange, 500, error.toString());
        }
    }

    private void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(data);
        }
    }
}