package battleship;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MoveTest {

    private static final IGame.ShotResult INVALID = new IGame.ShotResult(false, false, null, false);
    private static final IGame.ShotResult REPEATED = new IGame.ShotResult(true, true, null, false);
    private static final IGame.ShotResult WATER = new IGame.ShotResult(true, false, null, false);

    private final List<IPosition> shots =
            List.of(new Position(1, 1), new Position(2, 2), new Position(3, 3));

    private Move move(IGame.ShotResult... results) {
        return new Move(1, shots, List.of(results));
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
    void testToString() {
        String s = move(WATER, WATER, WATER).toString();
        assertTrue(s.contains("number=1"));
        assertTrue(s.contains("shots=3"));
        assertTrue(s.contains("results=3"));
    }

    @Test
    void getNumber() {
        assertEquals(5, new Move(5, shots, List.of(WATER, WATER, WATER)).getNumber());
    }

    @Test
    void getShots() {
        assertSame(shots, move(WATER, WATER, WATER).getShots());
    }

    @Test
    void getShotResults() {
        List<IGame.ShotResult> results = List.of(WATER, WATER, WATER);
        assertSame(results, new Move(1, shots, results).getShotResults());
    }

    @Test
    void processEnemyFire() {
        String json = move(WATER, WATER, WATER).processEnemyFire(true);
        assertTrue(json.startsWith("{"));
    }

    @Test
    void onlyRepeated() {
        assertNotNull(move(REPEATED, REPEATED, REPEATED).processEnemyFire(true));
    }

    @Test
    void onlyInvalid() {
        String out = captureOutput(() -> move(INVALID, INVALID, INVALID).processEnemyFire(true));
        assertFalse(out.isEmpty());
    }

    @Test
    void validRepeatedAndOutside() {
        assertNotNull(move(WATER, REPEATED, INVALID).processEnemyFire(true));
    }

    @Test
    void repeatedWithoutValid() {
        assertNotNull(move(REPEATED, INVALID, INVALID).processEnemyFire(true));
    }

    @Test
    void hitOnFloatingShip() {
        Ship frigate = new Frigate(Compass.EAST, new Position(1, 1));
        IGame.ShotResult hit = new IGame.ShotResult(true, false, frigate, false);
        assertNotNull(move(hit, INVALID, INVALID).processEnemyFire(true));
    }

    @Test
    void hitAndMiss() {
        Ship frigate = new Frigate(Compass.EAST, new Position(1, 1));
        IGame.ShotResult hit = new IGame.ShotResult(true, false, frigate, false);
        assertNotNull(move(hit, WATER, INVALID).processEnemyFire(true));
    }

    @Test
    void sunkShip() {
        Ship barge = new Barge(Compass.NORTH, new Position(1, 1));
        IGame.ShotResult sunk = new IGame.ShotResult(true, false, barge, true);
        assertNotNull(move(sunk, INVALID, INVALID).processEnemyFire(true));
    }

    @Test
    void sunkAndHitSameCategory() {
        Ship b1 = new Barge(Compass.NORTH, new Position(1, 1));
        Ship b2 = new Barge(Compass.NORTH, new Position(5, 5));
        IGame.ShotResult sunk = new IGame.ShotResult(true, false, b1, true);
        IGame.ShotResult hit = new IGame.ShotResult(true, false, b2, false);
        assertNotNull(move(sunk, hit, WATER).processEnemyFire(true));
    }

    @Test
    void sunkAndHitDifferentCategory() {
        Ship barge = new Barge(Compass.NORTH, new Position(1, 1));
        Ship frigate = new Frigate(Compass.EAST, new Position(5, 5));
        IGame.ShotResult sunk = new IGame.ShotResult(true, false, barge, true);
        IGame.ShotResult hit = new IGame.ShotResult(true, false, frigate, false);
        assertNotNull(move(sunk, hit, WATER).processEnemyFire(true));
    }

    @Test
    void notVerbose() {
        String verbose = captureOutput(() -> move(WATER, WATER, WATER).processEnemyFire(true));
        String quiet = captureOutput(() -> move(WATER, WATER, WATER).processEnemyFire(false));
        assertTrue(verbose.length() > quiet.length(), "Verbose mode should print more.");
    }

    @Test
    void serializationError() {
        try (org.mockito.MockedConstruction<com.fasterxml.jackson.databind.ObjectMapper> mocked =
                     org.mockito.Mockito.mockConstruction(
                             com.fasterxml.jackson.databind.ObjectMapper.class,
                             (mock, ctx) -> org.mockito.Mockito.when(
                                             mock.writeValueAsString(org.mockito.Mockito.any()))
                                     .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("boom") {}))) {
            assertThrows(RuntimeException.class,
                    () -> move(WATER, WATER, WATER).processEnemyFire(false));
        }
    }
}