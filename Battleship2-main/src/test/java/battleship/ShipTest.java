package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Test class for Ship.
 * Author: ${user.name}
 * Date: ${current_date}
 * Time: ${current_time}
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - getCategory: 1
 * - getSize: 1
 * - getBearing: 1
 * - getPositions: 1
 * - stillFloating: 2
 * - shoot: 2
 * - occupies: 2
 * - tooCloseTo (IShip): 2
 * - tooCloseTo (IPosition): 2
 * - getTopMostPos: 2
 * - getBottomMostPos: 2
 * - getLeftMostPos: 2
 * - getRightMostPos: 2
 */
public class ShipTest {

    private Ship ship, shipBorder;

    @BeforeEach
    void setUp() {
        // Since Ship is abstract, instantiate it with a concrete subclass (e.g., Barge)
        ship = new Barge(Compass.NORTH, new Position(5, 5));
        shipBorder = new Barge(Compass.NORTH, new Position(0, 0));
    }

    @AfterEach
    void tearDown() {
        ship = null;
    }

    /**
     * Test for the constructor.
     * Cyclomatic Complexity: 1
     */
    @Test
    void testConstructor() {
        assertNotNull(ship, "Error: Ship instance should not be null.");
        assertEquals("Barca", ship.getCategory(), "Error: Ship category is incorrect.");
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: Ship bearing is incorrect.");
        assertEquals(1, ship.getSize(), "Error: Ship size is incorrect.");
        assertFalse(ship.getPositions().isEmpty(), "Error: Ship positions should not be empty.");
    }

    /**
     * Test for the getCategory method.
     * Cyclomatic Complexity: 1
     */
    @Test
    void testGetCategory() {
        assertEquals("Barca", ship.getCategory(), "Error: Ship category should be 'Barca'.");
    }

    /**
     * Test for the getSize method.
     * Cyclomatic Complexity: 1
     */
    @Test
    void testGetSize() {
        assertEquals(1, ship.getSize(), "Error: Ship size should be 1.");
    }

    /**
     * Test for the getBearing method.
     * Cyclomatic Complexity: 1
     */
    @Test
    void testGetBearing() {
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: Ship bearing should be NORTH.");
    }

    /**
     * Test for the getPositions method.
     * Cyclomatic Complexity: 1
     */
    @Test
    void testGetPositions() {
        List<IPosition> positions = ship.getPositions();
        assertNotNull(positions, "Error: Ship positions should not be null.");
        assertEquals(1, positions.size(), "Error: Ship should have exactly one position.");
        assertEquals(5, positions.get(0).getRow(), "Error: Position's row should be 5.");
        assertEquals(5, positions.get(0).getColumn(), "Error: Position's column should be 5.");
    }

    /**
     * Test for the stillFloating method (all positions intact).
     * Cyclomatic Complexity: 2
     */
    @Test
    void testStillFloating1() {
        assertTrue(ship.stillFloating(), "Error: Ship should still be floating.");
    }

    /**
     * Test for the stillFloating method (all positions hit).
     */
    @Test
    void testStillFloating2() {
        ship.getPositions().get(0).shoot();
        assertFalse(ship.stillFloating(), "Error: Ship should no longer be floating after being hit.");
    }

    /**
     * Test for the shoot method (valid position).
     * Cyclomatic Complexity: 2
     */
    @Test
    void testShoot1() {
        Position target = new Position(5, 5);
        ship.shoot(target);
        assertTrue(ship.getPositions().get(0).isHit(), "Error: Position should be marked as hit.");
    }

    /**
     * Test for the shoot method (invalid position).
     */
    @Test
    void testShoot2() {
        Position target = new Position(0, 0);
        ship.shoot(target); // No exception expected
        assertFalse(ship.getPositions().get(0).isHit(), "Error: Position should not be marked as hit for an invalid target.");
    }

    /**
     * Test for the occupies method (position occupied).
     * Cyclomatic Complexity: 2
     */
    @Test
    void testOccupies1() {
        Position pos = new Position(5, 5);
        assertTrue(ship.occupies(pos), "Error: Ship should occupy position (5, 5).");
    }

    /**
     * Test for the occupies method (position not occupied).
     */
    @Test
    void testOccupies2() {
        Position pos = new Position(1, 1);
        assertFalse(ship.occupies(pos), "Error: Ship should not occupy position (1, 1).");
    }

    /**
     * Test for the tooCloseTo method with another IShip (ships too close).
     * Cyclomatic Complexity: 2
     */
    @Test
    void testTooCloseToShip1() {
        Ship nearbyShip = new Barge(Compass.NORTH, new Position(5, 6));
        assertTrue(ship.tooCloseTo(nearbyShip), "Error: Ships should be too close.");
    }

    /**
     * Test for the tooCloseTo method with another IShip (ships not close).
     */
    @Test
    void testTooCloseToShip2() {
        Ship farShip = new Barge(Compass.NORTH, new Position(10, 10));
        assertFalse(ship.tooCloseTo(farShip), "Error: Ships should not be too close.");
    }

    /**
     * Test for the tooCloseTo method with an IPosition (positions adjacent).
     * Cyclomatic Complexity: 2
     */
    @Test
    void testTooCloseToPosition1() {
        Position pos = new Position(5, 6); // Adjacent position
        assertTrue(ship.tooCloseTo(pos), "Error: Ship should be too close to the given position.");
    }

    /**
     * Test for the tooCloseTo method with an IPosition (positions not adjacent).
     */
    @Test
    void testTooCloseToPosition2() {
        Position pos = new Position(7, 7); // Non-adjacent position
        assertFalse(ship.tooCloseTo(pos), "Error: Ship should not be too close to the given position.");
    }

    /**
     * Test for the getTopMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    void testGetTopMostPos() {
        assertEquals(5, ship.getTopMostPos(), "Error: The topmost position should be 5.");
    }

    /**
     * Test for the getBottomMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    void testGetBottomMostPos() {
        assertEquals(5, ship.getBottomMostPos(), "Error: The bottommost position should be 5.");
    }

    /**
     * Test for the getLeftMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    void testGetLeftMostPos() {
        assertEquals(5, ship.getLeftMostPos(), "Error: The leftmost position should be 5.");
    }

    /**
     * Test for the getRightMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    void testGetRightMostPos() {
        assertEquals(5, ship.getRightMostPos(), "Error: The rightmost position should be 5.");
    }

    /**
     * Test for the getRightMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    void testGetAdjacentPositions() {
        assertEquals(8, ship.getAdjacentPositions().size(), "Error: The number of adjacent positions of a barge in the middle of the tray should be 8.");
        assertEquals(3, shipBorder.getAdjacentPositions().size(), "Error: The number of adjacent positions of a barge in the middle of the tray should be 8.");
    }

    // ------------------------------------------------------------------
    // Testes adicionais (cobertura de linhas e ramos)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("buildShip cria o tipo de navio certo para cada categoria")
    void buildShipKnownKinds() {
        Position p = new Position(1, 1);
        assertInstanceOf(Barge.class, Ship.buildShip("barca", Compass.NORTH, p));
        assertInstanceOf(Caravel.class, Ship.buildShip("caravela", Compass.NORTH, p));
        assertInstanceOf(Carrack.class, Ship.buildShip("nau", Compass.NORTH, p));
        assertInstanceOf(Frigate.class, Ship.buildShip("fragata", Compass.NORTH, p));
        assertInstanceOf(Galleon.class, Ship.buildShip("galeao", Compass.NORTH, p));
    }

    @Test
    @DisplayName("buildShip devolve null para uma categoria desconhecida")
    void buildShipUnknownKind() {
        assertNull(Ship.buildShip("submarino", Compass.NORTH, new Position(1, 1)),
                "Categoria desconhecida deve devolver null");
    }

    @Test
    @DisplayName("getPosition devolve a posição inicial do navio")
    void getPositionReturnsStartPosition() {
        Position start = new Position(3, 4);
        Ship barge = new Barge(Compass.NORTH, start);
        assertEquals(start, barge.getPosition(), "Deve devolver a posicao de criacao");
    }

    @Test
    @DisplayName("getAdjacentPositions de uma fragata tem 14 posições sem repetições")
    void adjacentPositionsOfFrigate() {
        Ship frigate = new Frigate(Compass.NORTH, new Position(4, 4));
        List<IPosition> adjacent = frigate.getAdjacentPositions();
        assertEquals(14, adjacent.size(), "Fragata vertical tem 14 posicoes adjacentes");
        assertEquals(adjacent.size(), adjacent.stream().distinct().count(),
                "Nao deve haver posicoes repetidas");
        for (IPosition position : frigate.getPositions())
            assertFalse(adjacent.contains(position),
                    "As posicoes do proprio navio nao sao adjacentes");
    }

    @Test
    @DisplayName("getAdjacentPositions de uma barca tem 8 posições")
    void adjacentPositionsOfBarge() {
        assertEquals(8, ship.getAdjacentPositions().size(),
                "Barca no meio do tabuleiro tem 8 vizinhas");
    }

    @Test
    @DisplayName("getTopMostPos devolve a linha mais pequena mesmo com posições fora de ordem")
    void topMostPosWithUnorderedPositions() {
        Ship custom = new Ship("Teste", Compass.NORTH, new Position(3, 0), 3) {
            {
                positions.add(new Position(3, 0));
                positions.add(new Position(1, 0));
                positions.add(new Position(2, 0));
            }
        };
        assertEquals(1, custom.getTopMostPos(), "A linha mais acima e a 1");
    }

    @Test
    @DisplayName("occupies devolve true para posição do navio e false para outra")
    void occupiesTrueAndFalse() {
        assertTrue(ship.occupies(new Position(5, 5)), "Posicao do navio deve estar ocupada");
        assertFalse(ship.occupies(new Position(0, 9)), "Posicao longe nao esta ocupada");
    }

    @Test
    @DisplayName("tooCloseTo(IShip) devolve true para navio encostado e false para navio longe")
    void tooCloseToShip() {
        Ship near = new Barge(Compass.NORTH, new Position(5, 6));
        Ship far = new Barge(Compass.NORTH, new Position(9, 9));
        assertTrue(ship.tooCloseTo(near), "Navios encostados estao demasiado perto");
        assertFalse(ship.tooCloseTo(far), "Navios afastados nao estao demasiado perto");
    }

    @Test
    @DisplayName("tooCloseTo(IPosition) devolve true para posição vizinha e false para distante")
    void tooCloseToPosition() {
        assertTrue(ship.tooCloseTo(new Position(6, 6)), "Diagonal conta como perto");
        assertFalse(ship.tooCloseTo(new Position(9, 0)), "Posicao distante nao conta");
    }

    @Test
    @DisplayName("isInsideBoard devolve true para um navio dentro do tabuleiro")
    void isInsideBoardTrue() {
        assertTrue(ship.isInsideBoard(), "Barca em (5,5) esta dentro");
        assertTrue(new Frigate(Compass.NORTH, new Position(6, 9)).isInsideBoard(),
                "Fragata encostada ao limite inferior ainda esta dentro");
    }

    @Test
    @DisplayName("isInsideBoard devolve false quando sai por cada um dos lados")
    void isInsideBoardFalse() {
        assertFalse(new Barge(Compass.NORTH, new Position(5, -1)).isInsideBoard(), "Sai pela esquerda");
        assertFalse(new Frigate(Compass.EAST, new Position(5, 8)).isInsideBoard(), "Sai pela direita");
        assertFalse(new Barge(Compass.NORTH, new Position(-1, 5)).isInsideBoard(), "Sai por cima");
        assertFalse(new Frigate(Compass.NORTH, new Position(8, 5)).isInsideBoard(), "Sai por baixo");
    }

    @Test
    @DisplayName("shoot só atinge a posição indicada")
    void shootOnlyHitsGivenPosition() {
        Ship frigate = new Frigate(Compass.NORTH, new Position(2, 2));
        frigate.shoot(new Position(3, 2));
        assertTrue(frigate.getPositions().get(1).isHit(), "A posicao atingida deve estar marcada");
        assertFalse(frigate.getPositions().get(0).isHit(), "As outras nao devem ficar atingidas");
        assertTrue(frigate.stillFloating(), "O navio continua a flutuar");
    }

    @Test
    @DisplayName("shoot numa posição que não é do navio não altera nada")
    void shootMissLeavesShipIntact() {
        ship.shoot(new Position(0, 0));
        assertFalse(ship.getPositions().get(0).isHit(), "Tiro ao lado nao atinge o navio");
    }

    @Test
    @DisplayName("toString inclui a categoria e a posição")
    void toStringContainsCategoryAndPosition() {
        String text = ship.toString();
        assertTrue(text.contains("Barca"), "Deve conter a categoria");
        assertTrue(text.contains("F6"), "Deve conter a posicao classica F6");
    }

    @Test
    @DisplayName("buildShip rejeita argumentos nulos (assert)")
    void buildShipNullArguments() {
        Position p = new Position(1, 1);
        assertThrows(AssertionError.class, () -> Ship.buildShip(null, Compass.NORTH, p));
        assertThrows(AssertionError.class, () -> Ship.buildShip("barca", null, p));
        assertThrows(AssertionError.class, () -> Ship.buildShip("barca", Compass.NORTH, null));
    }

    @Test
    @DisplayName("occupies, tooCloseTo e shoot rejeitam argumentos inválidos (assert)")
    void methodsRejectInvalidArguments() {
        assertThrows(AssertionError.class, () -> ship.occupies(null));
        assertThrows(AssertionError.class, () -> ship.tooCloseTo((IShip) null));
        assertThrows(AssertionError.class, () -> ship.tooCloseTo((IPosition) null));
        assertThrows(AssertionError.class, () -> ship.shoot(null));
        assertThrows(AssertionError.class, () -> ship.shoot(new Position(-1, 0)),
                "Posicao fora do tabuleiro nao pode ser disparada");
    }
}
