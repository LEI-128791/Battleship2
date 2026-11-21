package battleship;

import java.util.List;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Game.
 * Author: britoeabreu
 * Date: 2024-03-19
 * Time: 15:30
 * Cyclomatic Complexity for each method:
 * - Game (constructor): 1
 * - fire: 7
 * - getShots: 1
 * - getRepeatedShots: 1
 * - getInvalidShots: 1
 * - getHits: 1
 * - getSunkShips: 1
 * - getRemainingShips: 1
 * - validShot: 3
 * - repeatedShot: 2
 * - printBoard: 1
 * - printValidShots: 1
 * - printFleet: 1
 */
public class GameTest {

	private Game game;

	@BeforeEach
	void setUp() {
		game = new Game(new Fleet()); // Assuming Fleet is a concrete implementation of IFleet
	}

	@AfterEach
	void tearDown() {
		game = null;
	}

	@Test
	void constructor() {
		assertNotNull(game, "Game instance should not be null after construction.");
		assertNotNull(game.getAlienMoves(), "Shots list should not be null after initialization.");
		assertTrue(game.getAlienMoves().isEmpty(), "Shots list should be empty upon initialization.");
		assertEquals(0, game.getInvalidShots(), "Invalid shots count should be zero upon initialization.");
		assertEquals(0, game.getRepeatedShots(), "Repeated shots count should be zero upon initialization.");
		assertEquals(0, game.getHits(), "Hits count should be zero upon initialization.");
		assertEquals(0, game.getSunkShips(), "Sunk ships count should be zero upon initialization.");
	}

	@Test
	void fire2() {
		Position invalidPosition = new Position(-1, 5);
		game.fireSingleShot(invalidPosition, false);
		assertEquals(1, game.getInvalidShots(), "Invalid shots counter should increase for an invalid shot.");
	}

	@Test
	void fire3() {
		Position position = new Position(2, 3);
		game.fireSingleShot(position, false);
		game.fireSingleShot(position, true);
		assertEquals(1, game.getRepeatedShots(), "Repeated shots counter should increase for a repeated shot.");
	}

	@Test
	void repeatedShot1() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		Position position = new Position(2, 3);
		assertTrue(game.repeatedShot(position), "Position (2,3) should be marked as repeated after firing.");
	}

	@Test
	void repeatedShot2() {
		Position position = new Position(2, 3);
		assertFalse(game.repeatedShot(position), "Position (2,3) should not be marked as repeated before firing.");
	}

	@Test
	void getAlienMoves() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		assertEquals(1, game.getAlienMoves().size(), "Shots list should contain one shot after firing once.");
	}

	@Test
	void getRemainingShips() {
		IFleet fleet = game.getMyFleet();
		Ship ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		Ship ship2 = new Frigate(Compass.EAST, new Position(5, 5));

		fleet.addShip(ship1);
		assertEquals(1, game.getRemainingShips(), "Just one ship was created!");
		fleet.addShip(ship2);
		assertEquals(2, game.getRemainingShips(), "Two ships were created!");
		ship2.sink();
		assertEquals(1, game.getRemainingShips(), "Remaining ships count should be 1 after sinking one of two ships.");
	}

	@Test
	void printBoard() {
		// Barge a afundar em (1,1) para cobrir os marcadores de navio e de adjacentes
		Ship barge = new Barge(Compass.NORTH, new Position(1, 1));
		game.getMyFleet().addShip(barge);

		// Um tiro no navio, um na água e um fora do tabuleiro
		captureOutput(() -> game.fireShots(
				List.of(new Position(1, 1), new Position(5, 5), new Position(-1, 5))));

		String full = captureOutput(() ->
				BoardPrinter.printBoard(game.getMyFleet(), game.getAlienMoves(), true, true));
		assertTrue(full.contains("*"), "Hit on a ship should be marked with '*'.");
		assertTrue(full.contains("o"), "Shot in the water should be marked with 'o'.");

		String noShots = captureOutput(() ->
				BoardPrinter.printBoard(game.getMyFleet(), game.getAlienMoves(), false, false));
		assertFalse(noShots.contains("*"), "Shots should not be shown when showShots is false.");
		assertTrue(noShots.contains("#") || noShots.contains("-"), "Ship should be drawn.");
	}

	@Test
	void jsonShots() {
		List<IPosition> shots = List.of(new Position(0, 0), new Position(2, 3));
		String json = captureOutput(() -> Game.jsonShots(shots));
		String result = Game.jsonShots(shots);

		assertTrue(result.startsWith("["), "JSON should be an array.");
		assertTrue(result.contains("A"), "Row 0 should be serialized as classic row 'A'.");
		assertTrue(json.contains(result.trim()), "JSON should also be printed to the console.");
	}

	@Test
	void getMyFleet() {
		assertNotNull(game.getMyFleet());
	}

	@Test
	void getAlienFleet() {
		assertNotNull(game.getAlienFleet());
	}

	@Test
	void getMyMoves() {
		assertNotNull(game.getMyMoves());
		assertTrue(game.getMyMoves().isEmpty());
	}

	@Test
	void randomEnemyFire() {
		String result = game.randomEnemyFire();
		assertNotNull(result);
		assertEquals(1, game.getAlienMoves().size(), "randomEnemyFire should register one move.");
	}

	@Test
	void randomEnemyFireFewCandidatesLeft() {
		// 33 jogadas usam 99 das 100 posições; a 34.ª tem só 1 candidata (ramo "else")
		for (int i = 0; i < 34; i++)
			captureOutput(() -> game.randomEnemyFire());
		assertEquals(34, game.getAlienMoves().size());
	}

	@Test
	void readEnemyFire() {
		java.util.Scanner in = new java.util.Scanner("A3 B4 C5\n");
		String result = game.readEnemyFire(in);

		assertNotNull(result);
		assertEquals(1, game.getAlienMoves().size(), "One move should be registered.");
		assertEquals(3, game.getAlienMoves().get(0).getShots().size());
	}

	@Test
	void readEnemyFireSeparateTokens() {
		java.util.Scanner in = new java.util.Scanner("A 3 B 4 C 5\n");
		game.readEnemyFire(in);
		assertEquals(1, game.getAlienMoves().size());
	}

	@Test
	void readEnemyFireWrongNumberOfShots() {
		java.util.Scanner in = new java.util.Scanner("A3 B4\n");
		assertThrows(IllegalArgumentException.class, () -> game.readEnemyFire(in));
	}

	@Test
	void fireShotsWrongNumberOfShots() {
		List<IPosition> two = List.of(new Position(1, 1), new Position(2, 2));
		assertThrows(IllegalArgumentException.class, () -> game.fireShots(two));
	}

	@Test
	void readEnemyFireIncompletePosition() {
		java.util.Scanner in = new java.util.Scanner("A\n");
		assertThrows(IllegalArgumentException.class, () -> game.readEnemyFire(in));
	}


	@Test
	void printMyBoard() {
		String output = captureOutput(() -> game.printMyBoard(true, true));
		assertFalse(output.isEmpty());
	}

	@Test
	void printAlienBoard() {
		String output = captureOutput(() -> game.printAlienBoard(true, true));
		assertFalse(output.isEmpty(), "Alien board should print something.");
	}

	@Test
	void over() {
		String output = captureOutput(() -> game.over());
		assertTrue(output.contains("+-----"), "over() should print the farewell frame.");
	}
	@Test
	void fireSingleShotHitAndSink() {
		game.getMyFleet().addShip(new Barge(Compass.NORTH, new Position(1, 1)));
		game.fireSingleShot(new Position(1, 1), false);
		assertEquals(1, game.getHits());
		assertEquals(1, game.getSunkShips());
		assertEquals(0, game.getRemainingShips());
	}

	@Test
	void fireSingleShotWater() {
		game.fireSingleShot(new Position(5, 5), false);
		assertEquals(0, game.getHits());
		assertEquals(0, game.getInvalidShots());
		assertEquals(0, game.getRepeatedShots());
	}

	@Test
	void jsonShotsEmpty() {
		String result = Game.jsonShots(List.of());
		assertTrue(result.startsWith("["));
	}

	private String captureOutput(Runnable action) {
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		java.io.PrintStream original = System.out;
		System.setOut(new java.io.PrintStream(out));
		try {
			action.run();
		} finally {
			System.setOut(original);
		}
		return out.toString();
	}
	@Test
	void printBoardFloatingShip() {
		game.getMyFleet().addShip(new Frigate(Compass.EAST, new Position(5, 5)));
		String out = captureOutput(() ->
				BoardPrinter.printBoard(game.getMyFleet(), game.getAlienMoves(), true, true));
		assertTrue(out.contains("#"), "Floating ship should be drawn.");
	}

	@Test
	void printBoardShotOnAdjacentOfSunkShip() {
		game.getMyFleet().addShip(new Barge(Compass.NORTH, new Position(1, 1)));
		// (1,1) afunda o barge; (1,2) é adjacente
		captureOutput(() -> game.fireShots(
				List.of(new Position(1, 1), new Position(1, 2), new Position(5, 5))));
		String out = captureOutput(() ->
				BoardPrinter.printBoard(game.getMyFleet(), game.getAlienMoves(), true, false));
		assertTrue(out.contains("o"), "Shot on adjacent tile should be marked as water.");
	}

	@Test
	void fireSingleShotRepeatedFromPreviousMove() {
		captureOutput(() -> game.fireShots(
				List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5))));
		game.fireSingleShot(new Position(2, 3), false);
		assertEquals(1, game.getRepeatedShots());
	}

	@Test
	void fireSingleShotHitNotSunk() {
		game.getMyFleet().addShip(new Frigate(Compass.EAST, new Position(5, 5)));
		game.fireSingleShot(new Position(5, 5), false);
		assertEquals(1, game.getHits());
		assertEquals(0, game.getSunkShips());
		assertEquals(1, game.getRemainingShips());
	}

	@Test
	void randomEnemyFireDuplicatePicks() throws InterruptedException {
		// Com 2 ou 4 candidatas, o sorteio repete posições com probabilidade alta.
		// Repetimos várias vezes (com seeds diferentes) para cobrir esse ramo.
		for (int i = 0; i < 20; i++) {
			Game two = gameWithCandidatesLeft(2);
			captureOutput(() -> two.randomEnemyFire());
			assertEquals(34, two.getAlienMoves().size());

			Game four = gameWithCandidatesLeft(4);
			captureOutput(() -> four.randomEnemyFire());
			assertEquals(33, four.getAlienMoves().size());

			Thread.sleep(2); // o Random usa currentTimeMillis como seed
		}
	}

	// Cria um jogo em que só restam 'left' posições por disparar
	private Game gameWithCandidatesLeft(int left) {
		Game g = new Game(new Fleet());
		List<IPosition> all = new java.util.ArrayList<>();
		for (int r = 0; r < Game.BOARD_SIZE; r++)
			for (int c = 0; c < Game.BOARD_SIZE; c++)
				all.add(new Position(r, c));

		List<IPosition> toFire = new java.util.ArrayList<>(all.subList(0, all.size() - left));
		while (toFire.size() % Game.NUMBER_SHOTS != 0)
			toFire.add(all.get(0)); // completa a última jogada com um tiro repetido

		for (int i = 0; i < toFire.size(); i += Game.NUMBER_SHOTS) {
			List<IPosition> move = new java.util.ArrayList<>(
					toFire.subList(i, i + Game.NUMBER_SHOTS));
			captureOutput(() -> g.fireShots(move));
		}
		return g;
	}

	@Test
	void nullArguments() {
		assertThrows(AssertionError.class, () -> game.repeatedShot(null));
		assertThrows(AssertionError.class, () -> game.fireSingleShot(null, false));
		assertThrows(AssertionError.class, () -> game.fireShots(null));
		assertThrows(AssertionError.class, () -> game.readEnemyFire(null));
		assertThrows(AssertionError.class, () -> Game.jsonShots(null));
		assertThrows(AssertionError.class, () -> BoardPrinter.printBoard(null, game.getAlienMoves(), true, true));
		assertThrows(AssertionError.class, () -> BoardPrinter.printBoard(game.getMyFleet(), null, true, true));
	}

	@Test
	void jsonShotsSerializationError() {
		try (org.mockito.MockedConstruction<com.fasterxml.jackson.databind.ObjectMapper> mocked =
					 org.mockito.Mockito.mockConstruction(
							 com.fasterxml.jackson.databind.ObjectMapper.class,
							 (mock, ctx) -> org.mockito.Mockito.when(
											 mock.writeValueAsString(org.mockito.Mockito.any()))
									 .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("boom") {}))) {
			assertThrows(RuntimeException.class,
					() -> Game.jsonShots(List.of(new Position(0, 0))));
		}
	}

}