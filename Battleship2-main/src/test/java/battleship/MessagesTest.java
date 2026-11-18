package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Messages.
 * Author: LEI-128782
 * Date: 2026-11-18 20:30
 * Cyclomatic Complexity:
 * - Messages() (default constructor): 1
 * - setLanguage(String): 3
 * - getLanguage(): 1
 * - get(String): 2
 * - get(String, Object...): 1  (test named getWithArguments() to tell it apart
 *   from the get(String) overload, whose tests are get1() and get2())
 *
 * Note: in setLanguage both ternaries depend on the same value (language is "en"
 * exactly when lang equals "en" ignoring case), so only the (true, true) and
 * (false, false) combinations are feasible; the third path is the exception exit
 * for a null argument.
 */
class MessagesTest {

    private Messages messages;

    @BeforeEach
    void setUp() {
        messages = new Messages();
        Messages.setLanguage("pt");
    }

    @AfterEach
    void tearDown() {
        messages = null;
        Messages.setLanguage("pt"); // leave the shared static state as the game expects it
    }

    // ------------------------------------------------------------------
    // Messages() - CC = 1
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Default constructor creates a Messages instance")
    void messages() {
        assertAll(
                () -> assertNotNull(messages,
                        "Error: expected a Messages instance but got null"),
                () -> assertEquals(Messages.class, messages.getClass(),
                        "Error: expected class Messages but got " + messages.getClass())
        );
    }

    // ------------------------------------------------------------------
    // setLanguage(String) - CC = 3
    // ------------------------------------------------------------------

    @Test
    @DisplayName("setLanguage: 'en' (any letter case) selects English and the English bundle")
    void setLanguage1() {
        Messages.setLanguage("EN");
        String upperCaseLanguage = Messages.getLanguage();
        String upperCaseQuit = Messages.get("command.quit");

        Messages.setLanguage("en");

        assertAll(
                () -> assertEquals("en", upperCaseLanguage,
                        "Error: expected language 'en' for input 'EN' but got '" + upperCaseLanguage + "'"),
                () -> assertEquals("quit", upperCaseQuit,
                        "Error: expected English command 'quit' for input 'EN' but got '" + upperCaseQuit + "'"),
                () -> assertEquals("en", Messages.getLanguage(),
                        "Error: expected language 'en' but got '" + Messages.getLanguage() + "'"),
                () -> assertEquals("quit", Messages.get("command.quit"),
                        "Error: expected English command 'quit' but got '" + Messages.get("command.quit") + "'")
        );
    }

    @Test
    @DisplayName("setLanguage: 'pt' and unsupported languages select Portuguese")
    void setLanguage2() {
        Messages.setLanguage("en");
        Messages.setLanguage("pt");
        String ptQuit = Messages.get("command.quit");

        Messages.setLanguage("en");
        Messages.setLanguage("fr");

        assertAll(
                () -> assertEquals("desisto", ptQuit,
                        "Error: expected Portuguese command 'desisto' for 'pt' but got '" + ptQuit + "'"),
                () -> assertEquals("pt", Messages.getLanguage(),
                        "Error: expected fallback language 'pt' for 'fr' but got '" + Messages.getLanguage() + "'"),
                () -> assertEquals("desisto", Messages.get("command.quit"),
                        "Error: expected Portuguese command 'desisto' for 'fr' but got '"
                                + Messages.get("command.quit") + "'")
        );
    }

    @Test
    @DisplayName("setLanguage: null language throws NullPointerException and keeps the current language")
    void setLanguage3() {
        Messages.setLanguage("en");

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> Messages.setLanguage(null),
                        "Error: expected NullPointerException for a null language but none was thrown"),
                () -> assertEquals("en", Messages.getLanguage(),
                        "Error: expected language to stay 'en' but got '" + Messages.getLanguage() + "'")
        );
    }

    // ------------------------------------------------------------------
    // getLanguage() - CC = 1
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getLanguage returns the language selected last")
    void getLanguage() {
        Messages.setLanguage("en");

        assertEquals("en", Messages.getLanguage(),
                "Error: expected language 'en' but got '" + Messages.getLanguage() + "'");
    }

    // ------------------------------------------------------------------
    // get(String) - CC = 2
    // ------------------------------------------------------------------

    @Test
    @DisplayName("get: with no bundle loaded yet, Portuguese is loaded lazily")
    void get1() throws Exception {
        Messages.setLanguage("en");
        setBundle(null);

        String quit = Messages.get("command.quit");

        assertAll(
                () -> assertEquals("desisto", quit,
                        "Error: expected lazily loaded Portuguese 'desisto' but got '" + quit + "'"),
                () -> assertEquals("pt", Messages.getLanguage(),
                        "Error: expected language 'pt' after lazy loading but got '" + Messages.getLanguage() + "'"),
                () -> assertNotNull(getBundle(),
                        "Error: expected the bundle to be loaded but it is still null")
        );
    }

    @Test
    @DisplayName("get: with a bundle loaded, reads from it and rejects unknown keys")
    void get2() {
        Messages.setLanguage("en");

        assertAll(
                () -> assertEquals("quit", Messages.get("command.quit"),
                        "Error: expected English 'quit' from the loaded bundle but got '"
                                + Messages.get("command.quit") + "'"),
                () -> assertEquals("en", Messages.getLanguage(),
                        "Error: expected the language to stay 'en' but got '" + Messages.getLanguage() + "'"),
                () -> assertThrows(MissingResourceException.class,
                        () -> Messages.get("no.such.key"),
                        "Error: expected MissingResourceException for an unknown key but none was thrown")
        );
    }

    // ------------------------------------------------------------------
    // get(String, Object...) - CC = 1
    // ------------------------------------------------------------------

    @Test
    @DisplayName("get with arguments formats the message with MessageFormat")
    void getWithArguments() {
        String pt = Messages.get("fleet.status", 3, 2);
        Messages.setLanguage("en");
        String en = Messages.get("fleet.status", 3, 2);

        assertAll(
                () -> assertEquals("Estado da Frota: 3 a flutuar, 2 afundados!", pt,
                        "Error: expected formatted Portuguese status but got '" + pt + "'"),
                () -> assertEquals("Fleet status: 3 afloat, 2 sunk!", en,
                        "Error: expected formatted English status but got '" + en + "'")
        );
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static void setBundle(ResourceBundle value) throws Exception {
        Field field = Messages.class.getDeclaredField("bundle");
        field.setAccessible(true);
        field.set(null, value);
    }

    private static ResourceBundle getBundle() throws Exception {
        Field field = Messages.class.getDeclaredField("bundle");
        field.setAccessible(true);
        return (ResourceBundle) field.get(null);
    }
}
