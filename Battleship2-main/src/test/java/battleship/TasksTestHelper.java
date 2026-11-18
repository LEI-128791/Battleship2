package battleship;

/**
 * Metodos auxiliares usados pelo TasksTest para construir entradas de teste.
 *
 * Ficam numa classe a parte para manter a complexidade ciclomatica do
 * TasksTest abaixo do limiar do Qodana.
 */
final class TasksTestHelper {

	private TasksTestHelper() {
	}

	static char bearingChar(Compass bearing) {
		return switch (bearing) {
			case NORTH -> 'n';
			case SOUTH -> 's';
			case EAST -> 'e';
			case WEST -> 'o';
		};
	}

	/** Uma linha de entrada por navio, no formato lido por Tasks.readShip. */
	static String[] shipLines(IFleet fleet) {
		return fleet.getShips().stream()
				.map(s -> s.getCategory().toLowerCase() + " " + s.getPosition().getRow() + " "
						+ s.getPosition().getColumn() + " " + bearingChar(s.getBearing()))
				.toArray(String[]::new);
	}

	/** Entrada com uma frota completa e valida (11 navios). */
	static String fleetInput() {
		return String.join("\n", shipLines(Fleet.createRandom())) + "\n";
	}

	/** Comandos de disparo que cobrem as 100 casas do tabuleiro, 3 tiros de cada vez. */
	static String fireAtEveryCell(String fireCommand) {
		StringBuilder sb = new StringBuilder();
		StringBuilder line = new StringBuilder();
		int count = 0;
		for (char r = 'A'; r <= 'J'; r++) {
			for (int c = 1; c <= 10; c++) {
				line.append(' ').append(r).append(c);
				if (++count % Game.NUMBER_SHOTS == 0) {
					sb.append(fireCommand).append(line).append('\n');
					line.setLength(0);
				}
			}
		}
		if (line.length() > 0) {
			while (count % Game.NUMBER_SHOTS != 0) {
				line.append(" J10");
				count++;
			}
			sb.append(fireCommand).append(line).append('\n');
		}
		return sb.toString();
	}

	static int occurrences(String text, String part) {
		int n = 0;
		for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1))
			n++;
		return n;
	}
}
