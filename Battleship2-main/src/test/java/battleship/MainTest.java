package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Main.
 * Author: LEI-128782
 * Date: 2026-11-18 20:30
 * Cyclomatic Complexity:
 * - Main() (default constructor): 1  -> test mainConstructor() (main() is taken by the method)
 * - main(String[]): 1                -> test main()
 */
class MainTest {

    private static final InputStream ORIGINAL_IN = System.in;
    private static final PrintStream ORIGINAL_OUT = System.out;

    private Main main;
    private ByteArrayOutputStream out;

    @BeforeEach
    void setUp() {
        main = new Main();
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        main = null;
        System.setIn(ORIGINAL_IN);
        System.setOut(ORIGINAL_OUT);
        Messages.setLanguage("pt");
    }

    @Test
    @DisplayName("Default constructor creates a Main instance")
    void mainConstructor() {
        assertAll(
                () -> assertNotNull(main,
                        "Error: expected a Main instance but got null"),
                () -> assertEquals(Main.class, main.getClass(),
                        "Error: expected class Main but got " + main.getClass())
        );
    }

    /**
     * main(String[]) has a single path: print the banner and delegate to Tasks.menu().
     * The same test also checks that an exception raised by the menu (no input
     * available) propagates out of main unchanged.
     */
    @Test
    @DisplayName("main prints the banner, runs the menu until the quit command and propagates menu errors")
    void main() {
        System.setIn(input("pt\ndesisto\n"));
        Main.main(new String[0]);
        String output = out.toString(StandardCharsets.UTF_8);

        System.setIn(input(""));

        assertAll(
                () -> assertTrue(output.startsWith("***  Battleship  ***"),
                        "Error: expected output to start with the banner but got: " + output),
                () -> assertTrue(output.contains("Choose language (pt/en): "),
                        "Error: expected the language prompt but got: " + output),
                () -> assertTrue(output.contains(Messages.get("menu.title")),
                        "Error: expected the help menu title but got: " + output),
                () -> assertTrue(output.contains("Bons ventos!"),
                        "Error: expected the Portuguese goodbye message but got: " + output),
                () -> assertEquals("pt", Messages.getLanguage(),
                        "Error: expected language 'pt' but got '" + Messages.getLanguage() + "'"),
                () -> assertThrows(NoSuchElementException.class,
                        () -> Main.main(new String[0]),
                        "Error: expected NoSuchElementException when no input is available but none was thrown")
        );
    }

    private static InputStream input(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }
}
