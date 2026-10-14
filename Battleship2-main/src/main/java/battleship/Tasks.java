package battleship;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The type Tasks.
 */
public class Tasks {
	/**
	 * The constant LOGGER.
	 */
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * The constant GOODBYE_MESSAGE.
	 */
	private static final String GOODBYE_MESSAGE = "Bons ventos!";

	/**
	 * Strings to be used by the user
	 */
	private static String AJUDA;
	private static String GERAFROTA;
	private static String LEFROTA;
	private static String DESISTIR;
	private static String RAJADA;
	private static String TIROS;
	private static String MAPA;
	private static String STATUS;
	private static String SIMULA;
	private static String EXPORTAR_PDF;

	private static void loadCommands() {
		AJUDA = Messages.get("command.help");
		GERAFROTA = Messages.get("command.generate");
		LEFROTA = Messages.get("command.readFleet");
		DESISTIR = Messages.get("command.quit");
		RAJADA = Messages.get("command.fire");
		TIROS = Messages.get("command.shots");
		MAPA = Messages.get("command.map");
		STATUS = Messages.get("command.status");
		SIMULA = Messages.get("command.simulate");
		EXPORTAR_PDF = Messages.get("command.pdf");
	}

	/**
	 * This task also tests the fighting element of a round of three shots
	 */
	public static void menu() {

		Scanner in = new Scanner(System.in);

		System.out.print("Choose language (pt/en): ");
		String language = in.next();
		Messages.setLanguage(language);
		loadCommands();

		IFleet myFleet = null;
		IGame game = null;
		boolean gameFinished = false;

		menuHelp();

		System.out.print("> ");
		String command = in.next();

		while (!command.equals(DESISTIR)) {

			if (command.equals(GERAFROTA)) {
				myFleet = Fleet.createRandom();
				game = new Game(myFleet);
				game.printMyBoard(false, true);

			} else if (command.equals(LEFROTA)) {
				myFleet = buildFleet(in);
				game = new Game(myFleet);
				game.printMyBoard(false, true);

			} else if (command.equals(STATUS)) {
				if (myFleet != null)
					myFleet.printStatus();

			} else if (command.equals(MAPA)) {
				if (myFleet != null)
					game.printMyBoard(false, true);

			} else if (command.equals(RAJADA)) {
				if (game != null && !gameFinished) {
					game.readEnemyFire(in);
					myFleet.printStatus();
					game.printMyBoard(true, false);

					if (game.getRemainingShips() == 0) {
						game.over();
						gameFinished = true;
					}
				} else if (gameFinished) {
					System.out.println(Messages.get("game.finished"));
				}

			} else if (command.equals(SIMULA)) {
				if (game != null && !gameFinished) {
					while (game.getRemainingShips() > 0) {
						game.randomEnemyFire();
						myFleet.printStatus();
						game.printMyBoard(true, false);
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
					game.over();
					gameFinished = true;
				} else if (gameFinished) {
					System.out.println(Messages.get("game.finished"));
				}

			} else if (command.equals(TIROS)) {
				if (game != null)
					game.printMyBoard(true, true);

			} else if (command.equals(AJUDA)) {
				menuHelp();

			} else if (command.equals(EXPORTAR_PDF)) {
				if (game == null) {
					System.out.println(Messages.get("game.createFleet"));
				} else {
					Path outputPath = Path.of("battleship-history.pdf");
					try {
						Path actualPath = PdfReportExporter.export(
								game.getAlienMoves(), outputPath);
						System.out.println(Messages.get("pdf.export.success")
								+ " " + actualPath.toAbsolutePath());
					} catch (IOException e) {
						LOGGER.error(Messages.get("pdf.export.logError"), e);
						System.out.println(Messages.get("pdf.export.error")
								+ " " + e.getMessage());
					}
				}

			} else {
				System.out.println(Messages.get("game.unknownCommand"));
			}

			System.out.print("> ");
			command = in.next();
		}
		System.out.println(Messages.get("game.goodbye"));
	}

	/**
	 * This function provides help information about the menu commands.
	 */

	public static void menuHelp() {
		System.out.println("======================= "
				+ Messages.get("menu.title")
				+ " =======================");

		System.out.println(Messages.get("menu.intro"));
		System.out.println("- " + GERAFROTA + ": "
				+ Messages.get("menu.generate"));
		System.out.println("- " + LEFROTA + ": "
				+ Messages.get("menu.readFleet"));
		System.out.println("- " + STATUS + ": "
				+ Messages.get("menu.status"));
		System.out.println("- " + MAPA + ": "
				+ Messages.get("menu.map"));
		System.out.println("- " + RAJADA + ": "
				+ Messages.get("menu.fire"));
		System.out.println("- " + SIMULA + ": "
				+ Messages.get("menu.simulate"));
		System.out.println("- " + TIROS + ": "
				+ Messages.get("menu.shots"));
		System.out.println("- " + DESISTIR + ": "
				+ Messages.get("menu.quit"));
		System.out.println("- " + EXPORTAR_PDF + ": "
				+ Messages.get("menu.pdf"));

		System.out.println(
				"===============================================================");
	}

	/**
	 * This operation allows the build up of a fleet, given user data
	 *
	 * @param in The scanner to read from
	 * @return The fleet that has been built
	 */
	public static Fleet buildFleet(Scanner in) {

		assert in != null;

		Fleet fleet = new Fleet();
		int i = 0; // i represents the total of successfully created ships
		while (i < Fleet.FLEET_SIZE) {
			IShip s = readShip(in);
			if (s != null) {
				boolean success = fleet.addShip(s);
				if (success)
					i++;
				else
					LOGGER.info(Messages.get("fleet.creation.failure"),
							s.getCategory(), s.getBearing(), s.getPosition());
			} else {
				LOGGER.info(Messages.get("fleet.unknownShip"));
			}
		}
			LOGGER.info(Messages.get("fleet.added"), i);
		return fleet;
	}

	/**
	 * This operation reads data about a ship, build it and returns it
	 *
	 * @param in The scanner to read from
	 * @return The created ship based on the data that has been read
	 */
	public static Ship readShip(Scanner in) {

		assert in != null;

		String shipKind = in.next();
		Position pos = readPosition(in);
		char c = in.next().charAt(0);
		Compass bearing = Compass.charToCompass(c);
		return Ship.buildShip(shipKind, bearing, pos);
	}

	/**
	 * This operation allows reading a position in the map
	 *
	 * @param in The scanner to read from
	 * @return The position that has been read
	 */
	public static Position readPosition(Scanner in) {

		assert in != null;

		int row = in.nextInt();
		int column = in.nextInt();
		return new Position(row, column);
	}

	/**
	 * This operation allows reading a position in the map
	 *
	 * @param in The scanner to read from
	 * @return The classic position that has been read
	 */
	public static IPosition readClassicPosition(@NotNull Scanner in) {
		// Verifica se ainda há tokens disponíveis
		if (!in.hasNext()) {
			throw new IllegalArgumentException(
					Messages.get("position.none"));
		}

		String part1 = in.next(); // Primeiro token
		String part2 = null;

		if (in.hasNextInt()) {
			part2 = in.next(); // Segundo token, se disponível
		}

		String input = (part2 != null) ? part1 + part2 : part1;

		// Normalizar o input para tratar letras maiúsculas e minúsculas
		input = input.toUpperCase();

		// Verificar os dois formatos possíveis: compactos e com espaço
		if (input.matches("[A-Z]\\d+")) {
			char column = input.charAt(0); // Extrair a coluna
			int row = Integer.parseInt(input.substring(1)); // Extrair a linha
			return new Position(column, row);
		} else if (part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+")) {
			char column = part1.charAt(0); // Extrair a coluna
			int row = Integer.parseInt(part2); // Extrair a linha
			return new Position(column, row);
		} else {
			throw new IllegalArgumentException(
					Messages.get("position.invalid"));
		}
	}

}