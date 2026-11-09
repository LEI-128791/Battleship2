package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.text.MessageFormat;

import java.util.*;

/**
 * Shot
 *
 * @author Your Name
 * Date: 20/02/2026
 * Time: 19:39
 */
public class Move implements IMove {

	//-------------------------------------------------------------------
	private final int number;
	private final List<IPosition> shots;
	private final List<IGame.ShotResult> shotResults;

	//-------------------------------------------------------------------
	public Move(int moveNumber, List<IPosition> moveShots, List<IGame.ShotResult> moveResults) {
		this.number = moveNumber;
		this.shots = moveShots;
		this.shotResults = moveResults;
	}
	private static String msg(String key, Object... args) {
		return MessageFormat.format(Messages.get(key), args);
	}
	private static String categoryName(String category) {
		return Messages.get("category." + category.toLowerCase());
	}

	@Override
	public String toString() {
		return "Move{" +
				"number=" + number +
				", shots=" + shots.size() +
				", results=" + shotResults.size() +
				'}';
	}

	@Override
	public int getNumber() {
		return this.number;
	}

	@Override
	public List<IPosition> getShots() {
		return this.shots;
	}

	@Override
	public List<IGame.ShotResult> getShotResults() {
		return this.shotResults;
	}

	/**
	 * Processes the results of enemy fire on the game board, analyzing the outcomes of shots,
	 * such as valid shots, repeated shots, missed shots, hits on ships, and sunk ships. It can
	 * also display a detailed summary of the shot results if verbose mode is activated.
	 *
	 * @param verbose a boolean indicating whether a detailed summary should be printed to the console
	 *                for the processed enemy fire data.
	 * @return a JSON-formatted string that encapsulates the results, including counts of valid shots,
	 *         repeated shots, missed shots, shots outside the game board, and details of hits and
	 *         sunk ships.
	 */
	@Override
	public String processEnemyFire(boolean verbose) {

		int validShots = 0;
		int repeatedShots = 0;
		int missedShots = 0;

		Map<String, Integer> sunkBoatsCount = new HashMap<>(); // Rastrear quantos navios de cada tipo afundaram
		Map<String, Integer> hitsPerBoat = new HashMap<>();


		// Processar cada resultado de tiro
		for (IGame.ShotResult result : this.shotResults) {
			if (!result.valid()) {
				// Tiro inválido - apenas ignorar
				continue;
			}

			if (result.repeated())
				repeatedShots++; // tiro repetido
			else {
				// Tiro válido
				validShots++;
				if (result.ship() == null)
					missedShots++; // Tiro na água
				else{
					String boatName = result.ship().getCategory();
					hitsPerBoat.put(boatName, hitsPerBoat.getOrDefault(boatName, 0) + 1);
					if (result.sunk())
						sunkBoatsCount.put(boatName, sunkBoatsCount.getOrDefault(boatName, 0) + 1); // Contar barcos do mesmo tipo afundados
				}
			}
		}

		// Determinar número de tiros fora do tabuleiro
		int outsideShots = Game.NUMBER_SHOTS - validShots - repeatedShots;

		if (verbose) {
			// Construção da mensagem de saída
			StringBuilder output = new StringBuilder();

			if (validShots == 0 && repeatedShots > 0) {
				output.append(msg("move.repeated", repeatedShots));
			} else {
				if (validShots > 0) {
					output.append(msg("move.validShots", validShots)).append(": ");
				}

				// Barcos afundados (conta múltiplos do mesmo tipo)
				for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
					output.append(msg("move.sunk", entry.getValue(), categoryName(entry.getKey()))).append(" + ");
				}

				// Acertos em barcos ainda a flutuar
				for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
					if (!sunkBoatsCount.containsKey(entry.getKey())) {
						output.append(msg("move.hits", entry.getValue(), categoryName(entry.getKey()))).append(" + ");
					}
				}

				if (missedShots > 0) {
					output.append(msg("move.missed", missedShots));
				} else if (!sunkBoatsCount.isEmpty() || !hitsPerBoat.isEmpty()) {
					output.setLength(output.length() - 2); // Remover o "+" final
				}

				if (repeatedShots > 0) {
					if (validShots > 0) {
						output.append(", ");
					}
					output.append(msg("move.repeated", repeatedShots));
				}
			}

			// Tiros fora do tabuleiro
			if (outsideShots > 0) {
				if (!output.isEmpty()) {
					output.append(", ");
				}
				output.append(msg("move.outside", outsideShots));
			}

			System.out.println(msg("move.header", this.number, output));
		}

		// Criar o mapa para o JSON
		Map<String, Object> response = new LinkedHashMap<>();
		response.put(Messages.get("json.validShots"), validShots);
		response.put(Messages.get("json.outsideShots"), outsideShots);
		response.put(Messages.get("json.repeatedShots"), repeatedShots);
		response.put(Messages.get("json.missedShots"), missedShots);

		// Criar a lista de barcos afundados
		List<Map<String, Object>> sunkBoats = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
			Map<String, Object> boat = new LinkedHashMap<>();
			boat.put(Messages.get("json.type"), categoryName(entry.getKey()));
			boat.put(Messages.get("json.count"), entry.getValue());
			sunkBoats.add(boat);
		}
		response.put(Messages.get("json.sunkBoats"), sunkBoats);

		// Criar a lista de acertos em barcos que não foram afundados
		List<Map<String, Object>> boatHits = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
			if (!sunkBoatsCount.containsKey(entry.getKey())) {
				Map<String, Object> boat = new LinkedHashMap<>();
				boat.put(Messages.get("json.type"), categoryName(entry.getKey()));
				boat.put(Messages.get("json.hits"), entry.getValue());
				boatHits.add(boat);
			}
		}
		response.put(Messages.get("json.hitsOnBoats"), boatHits);

		// Serializar o JSON utilizando Jackson
		String jsonString;

		// Serializar os tiros gerados em JSON usando a biblioteca Jackson
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		try {
			jsonString = objectMapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Erro ao serializar o JSON dos resultados da jogada", e);
		}

		System.out.println(jsonString);
		System.out.println();

		// Retornar o JSON
		return jsonString;
	}
}