package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Frigate.
 * Author: LEI-128782
 * Date: 2026-11-18 20:20
 * Cyclomatic Complexity:
 * - Frigate(Compass, IPosition) (constructor): 9
 *   (1 + 4 switch cases + 4 for loops, one loop per case)
 *
 * Notes on the 9 test paths:
 * - frigate1..frigate4: one per switch case (NORTH, SOUTH, EAST, WEST); each one takes the
 *   case edge, the loop-body edge (4 iterations) and the loop-exit edge of that case.
 * - frigate5..frigate7: exception exits from super(...) (Objects.requireNonNull in Ship),
 *   thrown before the switch is reached.
 * - frigate8..frigate9: boundary starts (board corner / last row and column). The two
 *   remaining basis paths in the CFG (loop body never executed, switch with no
 *   matching case) are infeasible: the size is the constant 4 and Compass has
 *   exactly four values, so these tests check the edges of the board instead.
 *
 * Methods inherited from Ship (getSize, stillFloating, getTopMostPos, ...) are
 * declared in Ship and covered by ShipTest.
 */
class FrigateTest {

    private static final String CATEGORY = "Fragata";
    private static final int SIZE = 4;

    private Frigate frigate;

    @BeforeEach
    void setUp() {
        frigate = new Frigate(Compass.NORTH, new Position(5, 5));
    }

    @AfterEach
    void tearDown() {
        frigate = null;
    }

    @Test
    @DisplayName("Frigate: bearing NORTH places 4 vertical positions (rows grow downwards from the start position)")
    void frigate1() {
        // Instance created in setUp() with bearing NORTH at (5, 5)
        Frigate ship = frigate;
        List<IPosition> expected = verticalPositions(5, 5);

        assertAll(
                () -> assertEquals(CATEGORY, ship.getCategory(),
                        "Error: expected category " + CATEGORY + " but got " + ship.getCategory()),
                () -> assertEquals(Compass.NORTH, ship.getBearing(),
                        "Error: expected bearing NORTH but got " + ship.getBearing()),
                () -> assertEquals(SIZE, ship.getSize(),
                        "Error: expected size " + SIZE + " but got " + ship.getSize()),
                () -> assertEquals(new Position(5, 5), ship.getPosition(),
                        "Error: expected start position (5, 5) but got " + ship.getPosition()),
                () -> assertEquals(expected, ship.getPositions(),
                        "Error: expected positions " + expected + " but got " + ship.getPositions())
        );
    }

    @Test
    @DisplayName("Frigate: bearing SOUTH places 4 vertical positions (rows grow downwards from the start position)")
    void frigate2() {
        Frigate ship = new Frigate(Compass.SOUTH, new Position(5, 5));
        List<IPosition> expected = verticalPositions(5, 5);

        assertAll(
                () -> assertEquals(CATEGORY, ship.getCategory(),
                        "Error: expected category " + CATEGORY + " but got " + ship.getCategory()),
                () -> assertEquals(Compass.SOUTH, ship.getBearing(),
                        "Error: expected bearing SOUTH but got " + ship.getBearing()),
                () -> assertEquals(SIZE, ship.getSize(),
                        "Error: expected size " + SIZE + " but got " + ship.getSize()),
                () -> assertEquals(new Position(5, 5), ship.getPosition(),
                        "Error: expected start position (5, 5) but got " + ship.getPosition()),
                () -> assertEquals(expected, ship.getPositions(),
                        "Error: expected positions " + expected + " but got " + ship.getPositions())
        );
    }

    @Test
    @DisplayName("Frigate: bearing EAST places 4 horizontal positions (columns grow to the right of the start position)")
    void frigate3() {
        Frigate ship = new Frigate(Compass.EAST, new Position(5, 5));
        List<IPosition> expected = horizontalPositions(5, 5);

        assertAll(
                () -> assertEquals(CATEGORY, ship.getCategory(),
                        "Error: expected category " + CATEGORY + " but got " + ship.getCategory()),
                () -> assertEquals(Compass.EAST, ship.getBearing(),
                        "Error: expected bearing EAST but got " + ship.getBearing()),
                () -> assertEquals(SIZE, ship.getSize(),
                        "Error: expected size " + SIZE + " but got " + ship.getSize()),
                () -> assertEquals(new Position(5, 5), ship.getPosition(),
                        "Error: expected start position (5, 5) but got " + ship.getPosition()),
                () -> assertEquals(expected, ship.getPositions(),
                        "Error: expected positions " + expected + " but got " + ship.getPositions())
        );
    }

    @Test
    @DisplayName("Frigate: bearing WEST places 4 horizontal positions (columns grow to the right of the start position)")
    void frigate4() {
        Frigate ship = new Frigate(Compass.WEST, new Position(5, 5));
        List<IPosition> expected = horizontalPositions(5, 5);

        assertAll(
                () -> assertEquals(CATEGORY, ship.getCategory(),
                        "Error: expected category " + CATEGORY + " but got " + ship.getCategory()),
                () -> assertEquals(Compass.WEST, ship.getBearing(),
                        "Error: expected bearing WEST but got " + ship.getBearing()),
                () -> assertEquals(SIZE, ship.getSize(),
                        "Error: expected size " + SIZE + " but got " + ship.getSize()),
                () -> assertEquals(new Position(5, 5), ship.getPosition(),
                        "Error: expected start position (5, 5) but got " + ship.getPosition()),
                () -> assertEquals(expected, ship.getPositions(),
                        "Error: expected positions " + expected + " but got " + ship.getPositions())
        );
    }

    @Test
    @DisplayName("Frigate: null bearing throws NullPointerException before the switch")
    void frigate5() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new Frigate(null, new Position(5, 5)),
                "Error: expected NullPointerException for a null bearing but none was thrown");

        assertEquals("Ship's bearing must not be null", e.getMessage(),
                "Error: expected message 'Ship's bearing must not be null' but got '" + e.getMessage() + "'");
    }

    @Test
    @DisplayName("Frigate: null position throws NullPointerException before the switch")
    void frigate6() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new Frigate(Compass.EAST, null),
                "Error: expected NullPointerException for a null position but none was thrown");

        assertEquals("Ship's position must not be null", e.getMessage(),
                "Error: expected message 'Ship's position must not be null' but got '" + e.getMessage() + "'");
    }

    @Test
    @DisplayName("Frigate: null bearing and position fail on the bearing check first")
    void frigate7() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new Frigate(null, null),
                "Error: expected NullPointerException for null arguments but none was thrown");

        assertEquals("Ship's bearing must not be null", e.getMessage(),
                "Error: expected the bearing check to fail first but got '" + e.getMessage() + "'");
    }

    @Test
    @DisplayName("Frigate: ship starting at the board corner (0, 0) stays inside the board")
    void frigate8() {
        Frigate south = new Frigate(Compass.SOUTH, new Position(0, 0));
        Frigate west = new Frigate(Compass.WEST, new Position(0, 0));

        assertAll(
                () -> assertEquals(verticalPositions(0, 0), south.getPositions(),
                        "Error: expected SOUTH positions " + verticalPositions(0, 0)
                                + " but got " + south.getPositions()),
                () -> assertEquals(horizontalPositions(0, 0), west.getPositions(),
                        "Error: expected WEST positions " + horizontalPositions(0, 0)
                                + " but got " + west.getPositions()),
                () -> assertTrue(south.isInsideBoard(),
                        "Error: expected the SOUTH ship at (0, 0) to be inside the board but it was not"),
                () -> assertTrue(west.isInsideBoard(),
                        "Error: expected the WEST ship at (0, 0) to be inside the board but it was not")
        );
    }

    @Test
    @DisplayName("Frigate: constructor does not clip a ship that runs past the last row or column")
    void frigate9() {
        int last = Game.BOARD_SIZE - 1;
        Frigate north = new Frigate(Compass.NORTH, new Position(last, 0));
        Frigate east = new Frigate(Compass.EAST, new Position(0, last));

        assertAll(
                () -> assertEquals(SIZE, north.getPositions().size(),
                        "Error: expected " + SIZE + " NORTH positions but got " + north.getPositions().size()),
                () -> assertEquals(SIZE, east.getPositions().size(),
                        "Error: expected " + SIZE + " EAST positions but got " + east.getPositions().size()),
                () -> assertEquals(last + SIZE - 1, north.getBottomMostPos(),
                        "Error: expected bottom row " + (last + SIZE - 1) + " but got " + north.getBottomMostPos()),
                () -> assertEquals(last + SIZE - 1, east.getRightMostPos(),
                        "Error: expected right column " + (last + SIZE - 1) + " but got " + east.getRightMostPos()),
                () -> assertFalse(north.isInsideBoard(),
                        "Error: expected the NORTH ship at the last row to be outside the board but it was inside"),
                () -> assertFalse(east.isInsideBoard(),
                        "Error: expected the EAST ship at the last column to be outside the board but it was inside")
        );
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static List<IPosition> verticalPositions(int row, int column) {
        List<IPosition> positions = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            positions.add(new Position(row + r, column));
        }
        return positions;
    }

    private static List<IPosition> horizontalPositions(int row, int column) {
        List<IPosition> positions = new ArrayList<>();
        for (int c = 0; c < SIZE; c++) {
            positions.add(new Position(row, column + c));
        }
        return positions;
    }
}
