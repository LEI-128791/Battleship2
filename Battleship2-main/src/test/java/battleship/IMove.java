package battleship;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IMoveTest {

    @Test
    void readMoveReadsShotsInInputOrder() {
        try (Scanner scanner = new Scanner("2 1 3 4 2 99")) {
            IMove move = IMove.readMove(7, scanner);

            assertEquals(7, move.getNumber());
            assertEquals(
                    Arrays.asList(new Position(1, 3), new Position(4, 2)),
                    move.getShots()
            );
            assertTrue(move.getShotResults().isEmpty());
            assertEquals(99, scanner.nextInt());
        }
    }

    @Test
    void readMoveWithZeroShotsLeavesNextValueUnread() {
        try (Scanner scanner = new Scanner("0 99")) {
            IMove move = IMove.readMove(8, scanner);

            assertEquals(8, move.getNumber());
            assertTrue(move.getShots().isEmpty());
            assertTrue(move.getShotResults().isEmpty());
            assertEquals(99, scanner.nextInt());
        }
    }

    @Test
    void readMoveThrowsWhenShotCoordinateIsMissing() {
        try (Scanner scanner = new Scanner("1 3")) {
            assertThrows(NoSuchElementException.class,
                    () -> IMove.readMove(9, scanner));
        }
    }

    @Test
    void readMoveThrowsWhenShotCoordinateIsNotAnInteger() {
        try (Scanner scanner = new Scanner("1 3 A")) {
            assertThrows(InputMismatchException.class,
                    () -> IMove.readMove(9, scanner));
        }
    }
}