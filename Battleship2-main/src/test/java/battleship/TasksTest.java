package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios da classe Tasks (menu de consola e leitura de dados).
 *
 * O menu le de System.in e escreve em System.out, por isso os testes
 * substituem ambos por fluxos em memoria. O comando "simula" usa
 * Thread.sleep(3000); marcar a thread como interrompida faz o sleep
 * terminar logo, sem esperar.
 */
class TasksTest {

	private static final InputStream ORIGINAL_IN = System.in;
	private static final PrintStream ORIGINAL_OUT = System.out;

	private ByteArrayOutputStream out;
	private Set<String> pdfsBefore;

	@BeforeEach
	void setUp() throws IOException {
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
		pdfsBefore = listHistoryPdfs();
	}

	@AfterEach
	void tearDown() throws IOException {
		System.setIn(ORIGINAL_IN);
		System.setOut(ORIGINAL_OUT);
		Thread.interrupted(); // limpa a interrupcao usada nos testes do "simula"
		Messages.setLanguage("pt");
		for (String name : listHistoryPdfs()) {
			if (!pdfsBefore.contains(name))
				Files.deleteIfExists(Path.of(name));
		}
	}

	// ------------------------------------------------------------------
	// Auxiliares
	// ------------------------------------------------------------------

	private static Set<String> listHistoryPdfs() throws IOException {
		Set<String> names = new HashSet<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of("."), "battleship-history*.pdf")) {
			for (Path p : stream)
				names.add(p.getFileName().toString());
		}
		return names;
	}

	/** Corre o menu com o texto dado como entrada e devolve tudo o que foi escrito. */
	private String runMenu(String input) {
		System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
		Tasks.menu();
		return out.toString(StandardCharsets.UTF_8);
	}

	private static char bearingChar(Compass bearing) {
		return switch (bearing) {
			case NORTH -> 'n';
			case SOUTH -> 's';
			case EAST -> 'e';
			case WEST -> 'o';
		};
	}

	/** Uma linha de entrada por navio, no formato lido por Tasks.readShip. */
	private static String[] shipLines(IFleet fleet) {
		return fleet.getShips().stream()
				.map(s -> s.getCategory().toLowerCase() + " " + s.getPosition().getRow() + " "
						+ s.getPosition().getColumn() + " " + bearingChar(s.getBearing()))
				.toArray(String[]::new);
	}

	/** Entrada com uma frota completa e valida (11 navios). */
	private static String fleetInput() {
		return String.join("\n", shipLines(Fleet.createRandom())) + "\n";
	}

	/** Comandos de disparo que cobrem as 100 casas do tabuleiro, 3 tiros de cada vez. */
	private static String fireAtEveryCell(String fireCommand) {
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

	private static int occurrences(String text, String part) {
		int n = 0;
		for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1))
			n++;
		return n;
	}

	// ------------------------------------------------------------------
	// readPosition / readShip / buildFleet
	// ------------------------------------------------------------------

	@Test
	@DisplayName("readPosition lê linha e coluna numéricas")
	void readPosition() {
		Position p = Tasks.readPosition(new Scanner("3 4"));
		assertEquals(3, p.getRow(), "Linha deve ser 3");
		assertEquals(4, p.getColumn(), "Coluna deve ser 4");
	}

	@Test
	@DisplayName("readShip constrói o navio descrito na entrada")
	void readShipKnownKind() {
		Ship ship = Tasks.readShip(new Scanner("barca 3 4 n"));
		assertInstanceOf(Barge.class, ship, "Deve criar uma barca");
		assertEquals(3, ship.getPosition().getRow(), "Linha da barca");
		assertEquals(4, ship.getPosition().getColumn(), "Coluna da barca");
		assertEquals(Compass.NORTH, ship.getBearing(), "Rumo da barca");
	}

	@Test
	@DisplayName("readShip devolve null para um tipo de navio desconhecido")
	void readShipUnknownKind() {
		assertNull(Tasks.readShip(new Scanner("submarino 3 4 n")),
				"Tipo desconhecido nao cria navio");
	}

	@Test
	@DisplayName("buildFleet lê uma frota completa de 11 navios")
	void buildFleetFullFleet() {
		Fleet fleet = Tasks.buildFleet(new Scanner(fleetInput()));
		assertEquals(IFleet.FLEET_SIZE, fleet.getShips().size(), "A frota deve ter 11 navios");
	}

	@Test
	@DisplayName("buildFleet ignora navios desconhecidos e navios que colidem")
	void buildFleetSkipsInvalidShips() {
		String[] lines = shipLines(Fleet.createRandom());
		StringBuilder input = new StringBuilder("submarino 0 0 n\n");
		input.append(lines[0]).append('\n');
		input.append(lines[0]).append('\n'); // repetido: colide com o anterior
		for (int i = 1; i < lines.length; i++)
			input.append(lines[i]).append('\n');

		Fleet fleet = Tasks.buildFleet(new Scanner(input.toString()));
		assertEquals(IFleet.FLEET_SIZE, fleet.getShips().size(),
				"Mesmo com entradas invalidas, a frota acaba completa");
	}

	@Test
	@DisplayName("readPosition, readShip e buildFleet rejeitam scanner nulo")
	void readersRejectNullScanner() {
		assertThrows(AssertionError.class, () -> Tasks.readPosition(null));
		assertThrows(AssertionError.class, () -> Tasks.readShip(null));
		assertThrows(AssertionError.class, () -> Tasks.buildFleet(null));
	}

	// ------------------------------------------------------------------
	// readClassicPosition
	// ------------------------------------------------------------------

	@Test
	@DisplayName("readClassicPosition aceita o formato compacto 'A3'")
	void readClassicPositionCompact() {
		IPosition p = Tasks.readClassicPosition(new Scanner("A3"));
		assertEquals(0, p.getRow(), "Linha A -> 0");
		assertEquals(2, p.getColumn(), "Coluna 3 -> 2");
	}

	@Test
	@DisplayName("readClassicPosition aceita o formato com espaço 'B 4'")
	void readClassicPositionWithSpace() {
		IPosition p = Tasks.readClassicPosition(new Scanner("B 4"));
		assertEquals(1, p.getRow(), "Linha B -> 1");
		assertEquals(3, p.getColumn(), "Coluna 4 -> 3");
	}

	@Test
	@DisplayName("readClassicPosition aceita letras minúsculas")
	void readClassicPositionLowerCase() {
		IPosition p = Tasks.readClassicPosition(new Scanner("c7"));
		assertEquals(2, p.getRow(), "Linha c -> 2");
		assertEquals(6, p.getColumn(), "Coluna 7 -> 6");
	}

	@Test
	@DisplayName("readClassicPosition sem dados lança IllegalArgumentException")
	void readClassicPositionEmpty() {
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("")));
	}

	@Test
	@DisplayName("readClassicPosition com letra seguida de texto lança IllegalArgumentException")
	void readClassicPositionLetterWithoutNumber() {
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("A x")));
	}

	@Test
	@DisplayName("readClassicPosition com dois números lança IllegalArgumentException")
	void readClassicPositionTwoNumbers() {
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(new Scanner("11 12")));
	}

	// ------------------------------------------------------------------
	// menuHelp e menu
	// ------------------------------------------------------------------

	@Test
	@DisplayName("O menu mostra a ajuda com todos os comandos em português")
	void menuShowsHelpInPortuguese() {
		String output = runMenu("pt\ndesisto\n");
		assertTrue(output.contains(Messages.get("menu.title")), "Deve mostrar o titulo da ajuda");
		for (String key : new String[] { "command.generate", "command.readFleet", "command.status", "command.map",
				"command.fire", "command.simulate", "command.shots", "command.quit", "command.pdf" })
			assertTrue(output.contains(Messages.get(key)), "A ajuda deve listar " + key);
		assertTrue(output.contains(Messages.get("game.goodbye")), "Deve despedir-se ao sair");
	}

	@Test
	@DisplayName("O menu funciona em inglês")
	void menuWorksInEnglish() {
		Messages.setLanguage("en");
		String quit = Messages.get("command.quit");
		String help = Messages.get("command.help");
		String output = runMenu("en\n" + help + "\n" + quit + "\n");
		assertEquals("en", Messages.getLanguage(), "A lingua deve ficar em ingles");
		assertTrue(occurrences(output, Messages.get("menu.title")) >= 2, "A ajuda deve aparecer 2 vezes");
		assertTrue(output.contains(Messages.get("game.goodbye")), "Deve despedir-se ao sair");
	}

	@Test
	@DisplayName("Um comando desconhecido mostra a mensagem de erro")
	void menuUnknownCommand() {
		String output = runMenu("pt\nxpto\ndesisto\n");
		assertTrue(output.contains(Messages.get("game.unknownCommand")), "Deve avisar do comando desconhecido");
	}

	@Test
	@DisplayName("Sem frota, estado, mapa, tiros, rajada e simula não fazem nada")
	void menuCommandsWithoutFleet() {
		String output = runMenu("pt\nestado\nmapa\ntiros\nrajada\nsimula\ndesisto\n");
		assertFalse(output.contains(Messages.get("game.unknownCommand")), "Os comandos sao validos");
		assertFalse(output.contains(Messages.get("game.finished")), "O jogo nao comecou");
	}

	@Test
	@DisplayName("exportapdf sem frota pede para criar uma frota primeiro")
	void menuExportPdfWithoutFleet() {
		String output = runMenu("pt\nexportapdf\ndesisto\n");
		assertTrue(output.contains(Messages.get("game.createFleet")), "Deve pedir para criar a frota");
	}

	@Test
	@DisplayName("gerafrota cria a frota e estado, mapa e tiros mostram o tabuleiro")
	void menuRandomFleetAndViews() {
		String output = runMenu("pt\ngerafrota\nestado\nmapa\ntiros\ndesisto\n");
		assertTrue(output.contains(Messages.get("legend.title")), "tiros mostra a legenda do tabuleiro");
		assertFalse(output.contains(Messages.get("game.unknownCommand")), "Os comandos sao validos");
	}

	@Test
	@DisplayName("lefrota lê uma frota da entrada e mostra o mapa")
	void menuReadFleet() {
		String output = runMenu("pt\nlefrota\n" + fleetInput() + "mapa\ndesisto\n");
		assertFalse(output.contains(Messages.get("game.unknownCommand")), "Os comandos sao validos");
		assertTrue(output.contains(Messages.get("game.goodbye")), "O menu deve terminar normalmente");
	}

	@Test
	@DisplayName("rajada dispara 3 tiros e o jogo continua")
	void menuFireOneSalvo() {
		String output = runMenu("pt\ngerafrota\nrajada A1 B2 C3\ndesisto\n");
		assertFalse(output.contains(Messages.get("game.finished")), "Com 3 tiros o jogo nao acaba");
	}

	@Test
	@DisplayName("Depois de afundar tudo com rajadas, rajada e simula dizem que o jogo terminou")
	void menuFireUntilGameOver() {
		String input = "pt\ngerafrota\n" + fireAtEveryCell("rajada")
				+ "rajada A1 A2 A3\nsimula\ndesisto\n";
		String output = runMenu(input);
		// A frota e aleatoria: o jogo pode acabar antes da ultima rajada, por isso
		// o aviso aparece pelo menos 2 vezes (rajada final + simula).
		assertTrue(occurrences(output, Messages.get("game.finished")) >= 2,
				"rajada e simula devem avisar que o jogo terminou");
	}

	@Test
	@DisplayName("simula joga um jogo completo sem esperar")
	void menuSimulate() {
		Thread.currentThread().interrupt(); // o Thread.sleep(3000) deixa de esperar
		String output = runMenu("pt\ngerafrota\nsimula\nsimula\ndesisto\n");
		assertTrue(output.contains(Messages.get("game.finished")),
				"Um segundo simula deve avisar que o jogo terminou");
		assertTrue(output.contains(Messages.get("game.goodbye")), "O menu deve terminar normalmente");
	}

	@Test
	@DisplayName("simula espera 3 segundos entre rajadas e termina quando a thread é interrompida")
	void menuSimulateWaitsBetweenSalvos() throws InterruptedException {
		System.setIn(new ByteArrayInputStream("pt\ngerafrota\nsimula\ndesisto\n".getBytes(StandardCharsets.UTF_8)));
		Thread worker = new Thread(Tasks::menu);
		worker.start();
		Thread.sleep(3500); // a primeira espera (3 s) termina normalmente
		worker.interrupt(); // a segunda espera e interrompida; as seguintes deixam de esperar
		worker.join(30000);
		assertFalse(worker.isAlive(), "A simulacao deve terminar depois da interrupcao");
		assertTrue(out.toString(StandardCharsets.UTF_8).contains(Messages.get("game.goodbye")),
				"O menu deve terminar normalmente");
	}

	@Test
	@DisplayName("Tasks pode ser instanciada")
	void constructor() {
		assertNotNull(new Tasks(), "A instancia nao deve ser nula");
	}

	@Test
	@DisplayName("exportapdf com jogo criado exporta o histórico")
	void menuExportPdf() {
		String output = runMenu("pt\ngerafrota\nrajada A1 B2 C3\nexportapdf\ndesisto\n");
		assertTrue(output.contains(Messages.get("pdf.export.success")), "Deve confirmar a exportacao");
	}
}
