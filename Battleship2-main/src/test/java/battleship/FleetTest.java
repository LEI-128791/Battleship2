package battleship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FleetTest {

	private Fleet fleet;

	@BeforeEach
	void setUp() {
		fleet = new Fleet();
	}

	@Test
	void testConstructor() {
		assertNotNull(fleet);
		assertTrue(fleet.getShips().isEmpty());
	}

	@Test
	void testCreateRandom() {
		IFleet randomFleet = Fleet.createRandom();

		assertNotNull(randomFleet);
		assertEquals(Fleet.FLEET_SIZE, randomFleet.getShips().size());
	}

	@Test
	void testAddShip1() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));

		assertTrue(fleet.addShip(ship));
		assertEquals(1, fleet.getShips().size());
	}

	@Test
	void testAddShip2() {
		// A condição <= permite adicionar um navio quando o tamanho é FLEET_SIZE.
		for (int i = 0; i <= Fleet.FLEET_SIZE; i++) {
			int row = 1 + 2 * (i / 4);
			int col = 1 + 2 * (i % 4);
			IShip ship = new Barge(Compass.NORTH, new Position(row, col));

			assertTrue(fleet.addShip(ship), "Expected barge " + (i + 1) + " to be added.");
		}

		assertEquals(Fleet.FLEET_SIZE + 1, fleet.getShips().size());

		int nextIndex = Fleet.FLEET_SIZE + 1;
		int row = 1 + 2 * (nextIndex / 4);
		int col = 1 + 2 * (nextIndex % 4);
		IShip extraShip = new Barge(Compass.NORTH, new Position(row, col));

		assertTrue(extraShip.isInsideBoard());
		assertFalse(fleet.addShip(extraShip));
		assertEquals(Fleet.FLEET_SIZE + 1, fleet.getShips().size());
	}

	@Test
	void testAddShip3() {
		IShip outside = new Barge(Compass.NORTH, new Position(99, 99));

		assertFalse(fleet.addShip(outside));
		assertTrue(fleet.getShips().isEmpty());
	}

	@Test
	void testAddShip4() {
		IShip first = new Barge(Compass.NORTH, new Position(1, 1));
		IShip overlapping = new Barge(Compass.NORTH, new Position(1, 1));

		assertTrue(fleet.addShip(first));
		assertFalse(fleet.addShip(overlapping));
		assertEquals(1, fleet.getShips().size());
	}

	@Test
	void testGetShips() {
		assertTrue(fleet.getShips().isEmpty());

		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		assertTrue(fleet.addShip(ship));

		assertEquals(1, fleet.getShips().size());
		assertSame(ship, fleet.getShips().get(0));
	}

	@Test
	void testGetShipsLike() {
		IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
		IShip caravel = new Caravel(Compass.NORTH, new Position(4, 4));
		assertTrue(fleet.addShip(barge));
		assertTrue(fleet.addShip(caravel));

		List<IShip> barges = fleet.getShipsLike("Barca");

		assertEquals(1, barges.size());
		assertSame(barge, barges.get(0));
		assertTrue(fleet.getShipsLike("Categoria inexistente").isEmpty());
	}

	@Test
	void testGetFloatingShips() {
		IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
		IShip caravel = new Caravel(Compass.NORTH, new Position(4, 4));
		assertTrue(fleet.addShip(barge));
		assertTrue(fleet.addShip(caravel));
		assertEquals(2, fleet.getFloatingShips().size());

		barge.getPositions().get(0).shoot();

		List<IShip> floating = fleet.getFloatingShips();
		assertEquals(1, floating.size());
		assertSame(caravel, floating.get(0));
	}

	@Test
	void testGetSunkShips() {
		IShip sunk = new Barge(Compass.NORTH, new Position(1, 1));
		IShip floating = new Barge(Compass.NORTH, new Position(4, 4));
		assertTrue(fleet.addShip(sunk));
		assertTrue(fleet.addShip(floating));
		assertTrue(fleet.getSunkShips().isEmpty());

		sunk.getPositions().get(0).shoot();

		List<IShip> sunkShips = fleet.getSunkShips();
		assertEquals(1, sunkShips.size());
		assertSame(sunk, sunkShips.get(0));
	}

	@Test
	void testShipAt() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		assertTrue(fleet.addShip(ship));

		assertSame(ship, fleet.shipAt(new Position(1, 1)));
		assertNull(fleet.shipAt(new Position(5, 5)));
	}

	@Test
	void testCollisionRisk() throws Exception {
		Method method = Fleet.class.getDeclaredMethod("collisionRisk", IShip.class);
		method.setAccessible(true);

		IShip first = new Barge(Compass.NORTH, new Position(1, 1));
		IShip overlapping = new Barge(Compass.NORTH, new Position(1, 1));
		IShip separate = new Barge(Compass.NORTH, new Position(5, 5));
		assertTrue(fleet.addShip(first));

		assertTrue((Boolean) method.invoke(fleet, overlapping));
		assertFalse((Boolean) method.invoke(fleet, separate));
	}

	@Test
	void testPrintShipsByCategory() {
		IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
		IShip caravel = new Caravel(Compass.NORTH, new Position(4, 4));
		assertTrue(fleet.addShip(barge));
		assertTrue(fleet.addShip(caravel));

		String output = captureOutput(() -> fleet.printShipsByCategory("Barca"));

		assertEquals(barge.toString() + System.lineSeparator(), output);
	}

	@Test
	void testPrintFloatingShips() {
		IShip sunk = new Barge(Compass.NORTH, new Position(1, 1));
		IShip floating = new Barge(Compass.NORTH, new Position(4, 4));
		assertTrue(fleet.addShip(sunk));
		assertTrue(fleet.addShip(floating));
		sunk.getPositions().get(0).shoot();

		assertEquals(floating.toString() + System.lineSeparator(),
				captureOutput(fleet::printFloatingShips));
	}

	@Test
	void testPrintAllShips() {
		IShip first = new Barge(Compass.NORTH, new Position(1, 1));
		IShip second = new Barge(Compass.NORTH, new Position(4, 4));
		assertTrue(fleet.addShip(first));
		assertTrue(fleet.addShip(second));

		String newline = System.lineSeparator();
		String expected = first.toString() + newline + second.toString() + newline;

		assertEquals(expected, captureOutput(fleet::printAllShips));
	}

	@Test
	void testPrintStatus() {
		IShip sunk = new Barge(Compass.NORTH, new Position(1, 1));
		assertTrue(fleet.addShip(sunk));
		sunk.getPositions().get(0).shoot();

		String expected = MessageFormat.format(Messages.get("fleet.status"), 0, 1)
				+ System.lineSeparator();

		assertEquals(expected, captureOutput(fleet::printStatus));
	}

	private static String captureOutput(Runnable action) {
		PrintStream original = System.out;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		try (PrintStream captured = new PrintStream(buffer)) {
			System.setOut(captured);
			action.run();
			captured.flush();
			return buffer.toString();
		} finally {
			System.setOut(original);
		}
	}
	@Test
	void testNullArguments() {
		assertTrue(Fleet.class.desiredAssertionStatus(),
				"Ativa as assertions da JVM com -ea.");

		assertThrows(AssertionError.class, () -> fleet.addShip(null));
		assertThrows(AssertionError.class, () -> fleet.getShipsLike(null));
		assertThrows(AssertionError.class, () -> fleet.shipAt(null));
		assertThrows(AssertionError.class, () -> fleet.printShips(null));
		assertThrows(AssertionError.class, () -> fleet.printShipsByCategory(null));
	}

	@Test
	void testCollisionRiskRejectsNull() throws Exception {
		java.lang.reflect.Method method =
				Fleet.class.getDeclaredMethod("collisionRisk", IShip.class);
		method.setAccessible(true);

		java.lang.reflect.InvocationTargetException exception = assertThrows(
				java.lang.reflect.InvocationTargetException.class,
				() -> method.invoke(fleet, (Object) null)
		);

		assertTrue(exception.getCause() instanceof AssertionError);
	}
}