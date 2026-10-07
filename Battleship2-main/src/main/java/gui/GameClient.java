package gui;

import battleship.IPosition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Toda a comunicação (HTTP + JSON) com o servidor. A GUI não sabe nada de JSON. */
public class GameClient {

    /** Resultado de UM tiro; vem pela mesma ordem em que os tiros foram enviados. */
    public record ShotResult(Outcome outcome, String shipType) {}

    public record TurnResult(List<ShotResult> results,
                             int shipsRemaining,
                             String gameStatus,
                             String winner) {

        public boolean gameOver() {
            return "GAME_OVER".equals(gameStatus);
        }

        public boolean playerWon() {
            return "STUDENT_WINS".equals(winner);
        }
    }

    private final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private volatile String baseUrl;
    private volatile String gameId;

    public CompletableFuture<Void> register(String serverUrl, String playerName, int callbackPort) {
        baseUrl = serverUrl.trim().replaceAll("/+$", "");
        gameId = null;

        ObjectNode body = mapper.createObjectNode();
        body.put("playerName", playerName.isBlank() ? "Player" : playerName.trim());
        body.put("callbackUrl", "http://localhost:" + callbackPort);

        return post("/register", body)
                .thenAccept(json -> gameId = json.get("gameId").asText());
    }

    public CompletableFuture<TurnResult> fire(List<IPosition> positions) {
        ArrayNode shots = mapper.createArrayNode();
        for (IPosition p : positions) {
            ObjectNode shot = shots.addObject();
            shot.put("row", String.valueOf(p.getClassicRow()));
            shot.put("column", p.getClassicColumn());
        }

        ObjectNode body = mapper.createObjectNode();
        body.set("shots", shots);

        return post("/game/" + gameId + "/shots", body).thenApply(this::parseTurn);
    }

    private TurnResult parseTurn(JsonNode json) {
        List<ShotResult> results = new ArrayList<>();

        for (JsonNode r : json.path("results")) {
            results.add(new ShotResult(
                    Outcome.parse(r.path("outcome").asText()),
                    r.path("shipType").asText("")));
        }

        return new TurnResult(results,
                json.path("shipsRemaining").asInt(-1),
                json.path("gameStatus").asText(""),
                json.path("winner").asText(""));
    }

    private CompletableFuture<JsonNode> post(String path, JsonNode body) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        throw new IllegalStateException("Server error: " + response.body());
                    }
                    try {
                        return mapper.readTree(response.body());
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException("Invalid server response", e);
                    }
                });
    }
}