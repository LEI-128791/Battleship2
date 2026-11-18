package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Carrack.
 * Author: LEI-128782
 * Date: 2026-11-18 20:20
 * Cyclomatic Complexity:
 * - Carrack(Compass, IPosition) (constructor): 9
 *   (1 + 4 switch cases + 4 for loops, one loop per case)
 *
 * Notes on the 9 test paths:
 * - carrack1..carrack4: one per switch case (SOUTH, NORTH, EAST, WEST); each one takes the
 *   case edge, the loop-body edge (3 iterations) and the loop-exit edge of that case.
 * - carrack5..carrack7: exception exits from super(...) (Objects.requireNonNull in Ship),
 *   thrown before the switch is reached.
 * - carrack8..carrack9: boundary starts (board corner / last row and column). The two
 *   remaining basis paths in the CFG (loop body never executed, switch with no
 *   matching case) are infeasible: the size is the constant 3 and Compass has
 *   exactly four values, so these tests check the edges of the board instead.
 *
 * Methods inherited from Ship (getSize, stillFloating, getTopMostPos, ...) are
 * declared in Ship and covered by ShipTest.
 */
class CarrackTest {

    private static final String CATEGORY = "Nau";
    private static final int SIZE = 3;

    private Carrack carrack;

    @BeforeEach
    void setUp() {
        carrack = new Carrack(Compass.NORTH, new Position(5, 5));
    }

    @AfterEach
    void tearDown() {
        carrack = null;
    }

    @Test
    @DisplayName("Carrack: bearing SOUTH places 3 vertical positions (rows grow downwards from the start position)")
    void carrack1() {
        Carrack ship = new Carrack(Compass.SOUTH, new Position(5, 5));
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
    @DisplayName("Carrack: bearing NORTH places 3 vertical positions (rows grow downwards from the start position)")
    void carrack2() {
        // Instance created in setUp() with bearing NORTH at (5, 5)
        Carrack ship = carrack;
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
    @DisplayName("Carrack: bearing EAST places 3 horizontal positions (columns grow to the right of the start position)")
    void carrack3() {
        Carrack ship = new Carrack(Compass.EAST, new Position(5, 5));
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
    @DisplayName("Carrack: bearing WEST places 3 horizontal positions (columns grow to the right of the start position)")
    void carrack4() {
        Carrack ship = new Carrack(Compass.WEST, new Position(5, 5));
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
    @DisplayName("Carrack: null bearing throws NullPointerException before the switch")
    void carrack5() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new Carrack(null, new Position(5, 5)),
                "Error: expected NullPointerException for a null bearing but none was thrown");

        assertEquals("Ship's bearing must not be null", e.getMessage(),
                "Error: expected message 'Ship's bearing must not be null' but got '" + e.getMessage() + "'");
    }

    @Test
    @DisplayName("Carrack: null position throws NullPointerException before the switch")
    void carrack6() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new Carrack(Compass.EAST, null),
                "Error: expected NullPointerException for a null position but none was thrown");

        assertEquals("Ship's position must not be null", e.getMessage(),
                "Error: expected message 'Ship's position must not be null' but got '" + e.getMessage() + "'");
    }

    @Test
    @DisplayName("Carrack: null bearing and position fail on the bearing check first")
    void carrack7() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> new Carrack(null, null),
                "Error: expected NullPointerException for null arguments but none was thrown");

        assertEquals("Ship's bearing must not be null", e.getMessage(),
                "Error: expected the bearing check to fail first but got '" + e.getMessage() + "'");
    }

    @Test
    @DisplayName("Carrack: ship starting at the board corner (0, 0) stays inside the board")
    void carrack8() {
        Carrack south = new Carrack(Compass.SOUTH, new Position(0, 0));
        Carrack west = new Carrack(Compass.WEST, new Position(0, 0));

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
    @DisplayName("Carrack: constructor does not clip a ship that runs past the last row or column")
    void carrack9() {
        int last = Game.BOARD_SIZE - 1;
        Carrack north = new Carrack(Compass.NORTH, new Position(last, 0));
        Carrack east = new Carrack(Compass.EAST, new Position(0, last));

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
