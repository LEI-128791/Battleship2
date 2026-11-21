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
	private static final String PROMPT = "> ";
	private static final String PDF_FILE_NAME = "battleship-history.pdf";

	/**
	 * This task also tests the fighting element of a round of three shots
	 */
	public static void menu() {

		Scanner in = new Scanner(System.in);

		System.out.print("Choose language (pt/en): ");
		String language = in.next();
		Messages.setLanguage(language);
		MenuCommands.load();

		IFleet myFleet = null;
		IGame game = null;
		boolean gameFinished = false;

		menuHelp();

		System.out.print(PROMPT);
		String command = in.next();

		while (!command.equals(MenuCommands.DESISTIR)) {

			if (command.equals(MenuCommands.GERAFROTA)) {
				myFleet = Fleet.createRandom();
				game = new Game(myFleet);
				game.printMyBoard(false, true);

			} else if (command.equals(MenuCommands.LEFROTA)) {
				myFleet = buildFleet(in);
				game = new Game(myFleet);
				game.printMyBoard(false, true);

			} else if (command.equals(MenuCommands.STATUS)) {
				if (myFleet != null)
					myFleet.printStatus();

			} else if (command.equals(MenuCommands.MAPA)) {
				if (myFleet != null)
					game.printMyBoard(false, true);

			} else if (command.equals(MenuCommands.RAJADA)) {
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

			} else if (command.equals(MenuCommands.SIMULA)) {
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

			} else if (command.equals(MenuCommands.TIROS)) {
				if (game != null)
					game.printMyBoard(true, true);

			} else if (command.equals(MenuCommands.AJUDA)) {
				menuHelp();

			} else if (command.equals(MenuCommands.EXPORTAR_PDF)) {
				if (game == null) {
					System.out.println(Messages.get("game.createFleet"));
				} else {
					exportarHistorico(game, Path.of(PDF_FILE_NAME));
				}

			} else {
				System.out.println(Messages.get("game.unknownCommand"));
			}

			System.out.print(PROMPT);
			command = in.next();
		}
		System.out.println(Messages.get("game.goodbye"));
	}

	private static void exportarHistorico(IGame game, Path outputPath) {
		try {
            System.out.println(Messages.get("pdf.export.success")
					+ " " + PdfReportExporter.export(
                            game.getAlienMoves(), outputPath).toAbsolutePath());
		} catch (IOException e) {
			LOGGER.error(Messages.get("pdf.export.logError"), e);
			System.out.println(Messages.get("pdf.export.error")
					+ " " + e.getMessage());
		}
	}

	/**
	 * This function provides help information about the menu commands.
	 */

	public static void menuHelp() {
		System.out.println("======================= "
				+ Messages.get("menu.title")
				+ " =======================");

		System.out.println(Messages.get("menu.intro"));
		System.out.println("- " + MenuCommands.GERAFROTA + ": "
				+ Messages.get("menu.generate"));
		System.out.println("- " + MenuCommands.LEFROTA + ": "
				+ Messages.get("menu.readFleet"));
		System.out.println("- " + MenuCommands.STATUS + ": "
				+ Messages.get("menu.status"));
		System.out.println("- " + MenuCommands.MAPA + ": "
				+ Messages.get("menu.map"));
		System.out.println("- " + MenuCommands.RAJADA + ": "
				+ Messages.get("menu.fire"));
		System.out.println("- " + MenuCommands.SIMULA + ": "
				+ Messages.get("menu.simulate"));
		System.out.println("- " + MenuCommands.TIROS + ": "
				+ Messages.get("menu.shots"));
		System.out.println("- " + MenuCommands.DESISTIR + ": "
				+ Messages.get("menu.quit"));
		System.out.println("- " + MenuCommands.EXPORTAR_PDF + ": "
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
		} else {
			throw new IllegalArgumentException(
					Messages.get("position.invalid"));
		}
	}

}