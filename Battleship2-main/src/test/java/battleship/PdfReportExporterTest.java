package battleship;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static battleship.PdfReportExporterTestHelper.failingSaveChain;
import static battleship.PdfReportExporterTestHelper.invokePrivate;
import static battleship.PdfReportExporterTestHelper.pageCount;
import static battleship.PdfReportExporterTestHelper.pathProxy;
import static battleship.PdfReportExporterTestHelper.pdfText;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class PdfReportExporter.
 * Author: LEI-128782
 * Date: 2026-11-18 20:05
 * Cyclomatic Complexity:
 * - PdfReportExporter() (constructor): 1
 * - ShotReport(String, String) (record constructor): 1
 * - MoveReport(int, List) (record constructor): 1
 * - export(List, Path): 4
 * - reserveAvailablePath(Path): 6
 * - exportHistory(List, Path): 12
 * - describeResult(IGame.ShotResult): 5
 * - writeLine(PDPageContentStream, String, PDFont, float, float): 1
 */
class PdfReportExporterTest {

    private static final String TITLE = "Historico de jogadas - Batalha Naval";
    private static final String EMPTY_MESSAGE = "Nao existem jogadas registadas.";
    private static final String CONTINUATION = "Continuacao";

    private PdfReportExporter exporter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        var constructor = PdfReportExporter.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        exporter = constructor.newInstance();
    }

    @AfterEach
    void tearDown() {
        exporter = null;
    }

    // ------------------------------------------------------------------
    // Constructors (CC = 1 each)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Private constructor creates a PdfReportExporter instance")
    void pdfReportExporter() {
        assertAll(
                () -> assertNotNull(exporter,
                        "Error: expected an exporter instance but got null"),
                () -> assertEquals(PdfReportExporter.class, exporter.getClass(),
                        "Error: expected class PdfReportExporter but got " + exporter.getClass())
        );
    }

    @Test
    @DisplayName("ShotReport stores its values and rejects null coordinate or outcome")
    void shotReport() {
        var shot = new PdfReportExporter.ShotReport("A1", "Agua");

        assertAll(
                () -> assertEquals("A1", shot.coordinate(),
                        "Error: expected coordinate A1 but got " + shot.coordinate()),
                () -> assertEquals("Agua", shot.outcome(),
                        "Error: expected outcome Agua but got " + shot.outcome()),
                () -> assertThrows(NullPointerException.class,
                        () -> new PdfReportExporter.ShotReport(null, "Agua"),
                        "Error: expected NullPointerException for null coordinate but none was thrown"),
                () -> assertThrows(NullPointerException.class,
                        () -> new PdfReportExporter.ShotReport("A1", null),
                        "Error: expected NullPointerException for null outcome but none was thrown")
        );
    }

    @Test
    @DisplayName("MoveReport keeps an immutable defensive copy of the shot list")
    void moveReport() {
        var shot = new PdfReportExporter.ShotReport("A1", "Agua");
        var source = new ArrayList<>(List.of(shot));
        var report = new PdfReportExporter.MoveReport(1, source);

        source.clear();

        assertAll(
                () -> assertEquals(1, report.number(),
                        "Error: expected move number 1 but got " + report.number()),
                () -> assertEquals(List.of(shot), report.shots(),
                        "Error: expected the copied shot list [A1] but got " + report.shots()),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> report.shots().add(shot),
                        "Error: expected the shot list to be immutable but it accepted a new element"),
                () -> assertThrows(NullPointerException.class,
                        () -> new PdfReportExporter.MoveReport(1, null),
                        "Error: expected NullPointerException for a null shot list but none was thrown")
        );
    }

    // ------------------------------------------------------------------
    // export(List, Path) - CC = 4
    // ------------------------------------------------------------------

    @Test
    @DisplayName("export: null move list throws NullPointerException")
    void export1() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> PdfReportExporter.export(null, tempDir.resolve("report.pdf")),
                "Error: expected NullPointerException for null moves but none was thrown");

        assertEquals("moves cannot be null", e.getMessage(),
                "Error: expected message 'moves cannot be null' but got '" + e.getMessage() + "'");
    }

    @Test
    @DisplayName("export: empty move list skips the outer loop and writes the empty-history message")
    void export2() throws Exception {
        Path output = PdfReportExporter.export(List.of(), tempDir.resolve("empty.pdf"));
        String text = pdfText(output);

        assertAll(
                () -> assertTrue(Files.exists(output),
                        "Error: expected exported PDF to exist but it was not found at " + output),
                () -> assertTrue(text.contains(EMPTY_MESSAGE),
                        "Error: expected the empty-history message but got: " + text)
        );
    }

    @Test
    @DisplayName("export: move without shots skips the inner loop and writes only the heading")
    void export3() throws Exception {
        IMove move = new Move(7, List.of(), List.of());

        Path output = PdfReportExporter.export(List.of(move), tempDir.resolve("no-shots.pdf"));
        String text = pdfText(output);

        assertAll(
                () -> assertTrue(text.contains("Jogada 7"),
                        "Error: expected heading 'Jogada 7' but got: " + text),
                () -> assertFalse(text.contains("Tiro"),
                        "Error: expected no shot lines but got: " + text)
        );
    }

    @Test
    @DisplayName("export: shots with and without a matching result use both ternary branches")
    void export4() throws Exception {
        IPosition first = new Position('B', 2);
        IPosition second = new Position('C', 3);
        IGame.ShotResult result = new IGame.ShotResult(true, false, null, false);
        IMove move = new Move(2, List.of(first, second), List.of(result));

        Path output = PdfReportExporter.export(List.of(move), tempDir.resolve("results.pdf"));
        String text = pdfText(output);

        assertAll(
                () -> assertTrue(text.contains("Jogada 2"),
                        "Error: expected heading 'Jogada 2' but got: " + text),
                () -> assertTrue(text.contains("Tiro B2: Agua"),
                        "Error: expected 'Tiro B2: Agua' (result available) but got: " + text),
                () -> assertTrue(text.contains("Tiro C3: Resultado nao disponivel"),
                        "Error: expected 'Tiro C3: Resultado nao disponivel' (no result) but got: " + text)
        );
    }

    // ------------------------------------------------------------------
    // reserveAvailablePath(Path) - CC = 6
    // ------------------------------------------------------------------

    @Test
    @DisplayName("reserveAvailablePath: new file without extension is created as requested")
    void reserveAvailablePath1() throws Exception {
        Path requested = tempDir.resolve("plain");
        Path reserved = reserveAvailablePath(requested);

        assertAll(
                () -> assertEquals(requested, reserved,
                        "Error: expected reserved path " + requested + " but got " + reserved),
                () -> assertTrue(Files.exists(reserved),
                        "Error: expected reserved file to exist but it was not found at " + reserved)
        );
    }

    @Test
    @DisplayName("reserveAvailablePath: new file with extension is created as requested")
    void reserveAvailablePath2() throws Exception {
        Path requested = tempDir.resolve("report.pdf");
        Path reserved = reserveAvailablePath(requested);

        assertAll(
                () -> assertEquals(requested, reserved,
                        "Error: expected reserved path " + requested + " but got " + reserved),
                () -> assertTrue(Files.exists(reserved),
                        "Error: expected reserved file to exist but it was not found at " + reserved)
        );
    }

    @Test
    @DisplayName("reserveAvailablePath: first collision with extension yields name-1.ext")
    void reserveAvailablePath3() throws Exception {
        Files.createFile(tempDir.resolve("collision.pdf"));
        Path reserved = reserveAvailablePath(tempDir.resolve("collision.pdf"));

        assertAll(
                () -> assertEquals(tempDir.resolve("collision-1.pdf"), reserved,
                        "Error: expected collision-1.pdf but got " + reserved),
                () -> assertTrue(Files.exists(reserved),
                        "Error: expected reserved file to exist but it was not found at " + reserved)
        );
    }

    @Test
    @DisplayName("reserveAvailablePath: collision without extension yields name-1")
    void reserveAvailablePath4() throws Exception {
        Files.createFile(tempDir.resolve("plain"));
        Path reserved = reserveAvailablePath(tempDir.resolve("plain"));

        assertEquals(tempDir.resolve("plain-1"), reserved,
                "Error: expected plain-1 but got " + reserved);
    }

    @Test
    @DisplayName("reserveAvailablePath: dot-file (extension index 0) is treated as having no extension")
    void reserveAvailablePath5() throws Exception {
        Files.createFile(tempDir.resolve(".history"));
        Path reserved = reserveAvailablePath(tempDir.resolve(".history"));

        assertEquals(tempDir.resolve(".history-1"), reserved,
                "Error: expected .history-1 but got " + reserved);
    }

    @Test
    @DisplayName("reserveAvailablePath: loop keeps incrementing while suffixed names are taken")
    void reserveAvailablePath6() throws Exception {
        Files.createFile(tempDir.resolve("loop.pdf"));
        Files.createFile(tempDir.resolve("loop-1.pdf"));
        Files.createFile(tempDir.resolve("loop-2.pdf"));
        Path reserved = reserveAvailablePath(tempDir.resolve("loop.pdf"));

        assertEquals(tempDir.resolve("loop-3.pdf"), reserved,
                "Error: expected loop-3.pdf after occupied suffixes but got " + reserved);
    }

    // ------------------------------------------------------------------
    // exportHistory(List, Path) - CC = 12
    // ------------------------------------------------------------------

    @Test
    @DisplayName("exportHistory: null move list throws NullPointerException")
    void exportHistory1() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> PdfReportExporter.exportHistory(null, tempDir.resolve("report.pdf")),
                "Error: expected NullPointerException for null moves but none was thrown");

        assertEquals("moves cannot be null", e.getMessage(),
                "Error: expected message 'moves cannot be null' but got '" + e.getMessage() + "'");
    }

    @Test
    @DisplayName("exportHistory: null output path throws NullPointerException")
    void exportHistory2() {
        NullPointerException e = assertThrows(NullPointerException.class,
                () -> PdfReportExporter.exportHistory(List.of(), null),
                "Error: expected NullPointerException for null output path but none was thrown");

        assertEquals("outputPath cannot be null", e.getMessage(),
                "Error: expected message 'outputPath cannot be null' but got '" + e.getMessage() + "'");
    }

    @Test
    @DisplayName("exportHistory: empty history produces one page with the empty message and no continuation")
    void exportHistory3() throws Exception {
        Path requested = tempDir.resolve("empty-history.pdf");
        Path actual = PdfReportExporter.exportHistory(List.of(), requested);
        String text = pdfText(actual);

        assertAll(
                () -> assertEquals(requested, actual,
                        "Error: expected output path " + requested + " but got " + actual),
                () -> assertEquals(1, pageCount(actual),
                        "Error: expected 1 page but got " + pageCount(actual)),
                () -> assertTrue(text.contains(TITLE),
                        "Error: expected the report title but got: " + text),
                () -> assertTrue(text.contains(EMPTY_MESSAGE),
                        "Error: expected the empty-history message but got: " + text),
                () -> assertFalse(text.contains(CONTINUATION),
                        "Error: expected no continuation text on page 1 but got: " + text)
        );
    }

    @Test
    @DisplayName("exportHistory: absolute path without parent skips directory creation")
    void exportHistory4() {
        FakeFileSystem fs = new FakeFileSystem();
        Path orphan = pathProxy(fs, "orphan.pdf", Map.of(
                "getParent", args -> null,
                "getFileName", args -> Path.of("orphan.pdf")));
        Path input = pathProxy(fs, "orphan.pdf", Map.of(
                "toAbsolutePath", args -> orphan));

        // With no parent, createDirectories is skipped and reserveAvailablePath
        // cannot resolve a sibling, so a NullPointerException is expected.
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> PdfReportExporter.exportHistory(List.of(), input),
                        "Error: expected NullPointerException for a path without parent but none was thrown"),
                () -> assertTrue(fs.provider.createdDirectories.isEmpty(),
                        "Error: expected no directory creation but got " + fs.provider.createdDirectories)
        );
    }

    @Test
    @DisplayName("exportHistory: move without shots writes a bold heading and skips the blank line")
    void exportHistory5() throws Exception {
        var report = new PdfReportExporter.MoveReport(3, List.of());
        Path actual = PdfReportExporter.exportHistory(List.of(report),
                tempDir.resolve("heading.pdf"));
        String text = pdfText(actual);

        assertAll(
                () -> assertTrue(text.contains("Jogada 3"),
                        "Error: expected heading 'Jogada 3' but got: " + text),
                () -> assertFalse(text.contains("Tiro"),
                        "Error: expected no shot lines but got: " + text),
                () -> assertFalse(text.contains(EMPTY_MESSAGE),
                        "Error: expected no empty-history message but got: " + text)
        );
    }

    @Test
    @DisplayName("exportHistory: move with shots writes regular shot lines")
    void exportHistory6() throws Exception {
        var shots = List.of(
                new PdfReportExporter.ShotReport("C4", "Agua"),
                new PdfReportExporter.ShotReport("D5", "Navio atingido (Barca)"));
        var report = new PdfReportExporter.MoveReport(4, shots);
        Path actual = PdfReportExporter.exportHistory(List.of(report),
                tempDir.resolve("shot-lines.pdf"));
        String text = pdfText(actual);

        assertAll(
                () -> assertTrue(text.contains("Jogada 4"),
                        "Error: expected heading 'Jogada 4' but got: " + text),
                () -> assertTrue(text.contains("Tiro C4: Agua"),
                        "Error: expected 'Tiro C4: Agua' but got: " + text),
                () -> assertTrue(text.contains("Tiro D5: Navio atingido (Barca)"),
                        "Error: expected 'Tiro D5: Navio atingido (Barca)' but got: " + text)
        );
    }

    @Test
    @DisplayName("exportHistory: long history spans several pages with continuation headers")
    void exportHistory7() throws Exception {
        var reports = new ArrayList<PdfReportExporter.MoveReport>();
        for (int i = 1; i <= 50; i++) {
            reports.add(new PdfReportExporter.MoveReport(i,
                    List.of(new PdfReportExporter.ShotReport("A" + i, "Agua"))));
        }
        // 50 moves x 3 lines = 150 lines -> ceil(150 / 45) = 4 pages
        Path actual = PdfReportExporter.exportHistory(reports,
                tempDir.resolve("multi-page.pdf"));
        String text = pdfText(actual);

        assertAll(
                () -> assertEquals(4, pageCount(actual),
                        "Error: expected 4 pages but got " + pageCount(actual)),
                () -> assertTrue(text.contains(CONTINUATION),
                        "Error: expected continuation text on later pages but got none"),
                () -> assertTrue(text.contains("Jogada 50"),
                        "Error: expected the last move 'Jogada 50' to be written but it was missing")
        );
    }

    @Test
    @DisplayName("exportHistory: missing parent directories are created")
    void exportHistory8() throws Exception {
        Path nested = tempDir.resolve("a/b/report.pdf");
        Path actual = PdfReportExporter.exportHistory(List.of(), nested);

        assertAll(
                () -> assertTrue(Files.isDirectory(nested.getParent()),
                        "Error: expected parent directory " + nested.getParent() + " to be created"),
                () -> assertEquals(nested, actual,
                        "Error: expected output path " + nested + " but got " + actual),
                () -> assertTrue(Files.size(actual) > 0,
                        "Error: expected a non-empty PDF but got size " + Files.size(actual))
        );
    }

    @Test
    @DisplayName("exportHistory: existing output file is kept and a suffixed file is written")
    void exportHistory9() throws Exception {
        Path requested = tempDir.resolve("existing.pdf");
        Files.createFile(requested);
        Path actual = PdfReportExporter.exportHistory(List.of(), requested);

        assertAll(
                () -> assertEquals(tempDir.resolve("existing-1.pdf"), actual,
                        "Error: expected existing-1.pdf but got " + actual),
                () -> assertEquals(0, Files.size(requested),
                        "Error: expected the original file to stay empty but got size " + Files.size(requested)),
                () -> assertTrue(Files.size(actual) > 0,
                        "Error: expected a non-empty PDF but got size " + Files.size(actual))
        );
    }

    @Test
    @DisplayName("exportHistory: IOException while saving deletes the reserved file and is rethrown")
    void exportHistory10() {
        FakeFileSystem fs = new FakeFileSystem();
        File directory = tempDir.toFile(); // saving into a directory fails with IOException
        Path input = failingSaveChain(fs, args -> directory);

        IOException e = assertThrows(IOException.class,
                () -> PdfReportExporter.exportHistory(List.of(), input),
                "Error: expected IOException from the failed save but none was thrown");

        assertAll(
                () -> assertEquals(1, fs.provider.createdFiles.size(),
                        "Error: expected 1 reserved file but got " + fs.provider.createdFiles.size()),
                () -> assertEquals(fs.provider.createdFiles, fs.provider.deletedFiles,
                        "Error: expected the reserved file to be deleted but got " + fs.provider.deletedFiles),
                () -> assertEquals(0, e.getSuppressed().length,
                        "Error: expected no suppressed exceptions but got " + e.getSuppressed().length)
        );
    }

    @Test
    @DisplayName("exportHistory: RuntimeException while saving deletes the reserved file and is rethrown")
    void exportHistory11() {
        FakeFileSystem fs = new FakeFileSystem();
        Path input = failingSaveChain(fs, args -> {
            throw new IllegalStateException("save failure");
        });

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> PdfReportExporter.exportHistory(List.of(), input),
                "Error: expected IllegalStateException from the failed save but none was thrown");

        assertAll(
                () -> assertEquals("save failure", e.getMessage(),
                        "Error: expected message 'save failure' but got '" + e.getMessage() + "'"),
                () -> assertEquals(fs.provider.createdFiles, fs.provider.deletedFiles,
                        "Error: expected the reserved file to be deleted but got " + fs.provider.deletedFiles),
                () -> assertEquals(0, e.getSuppressed().length,
                        "Error: expected no suppressed exceptions but got " + e.getSuppressed().length)
        );
    }

    @Test
    @DisplayName("exportHistory: cleanup failure after a failed save is added as suppressed exception")
    void exportHistory12() {
        FakeFileSystem fs = new FakeFileSystem();
        fs.provider.failDelete = true;
        File directory = tempDir.toFile();
        Path input = failingSaveChain(fs, args -> directory);

        IOException e = assertThrows(IOException.class,
                () -> PdfReportExporter.exportHistory(List.of(), input),
                "Error: expected IOException from the failed save but none was thrown");

        assertAll(
                () -> assertEquals(1, e.getSuppressed().length,
                        "Error: expected 1 suppressed exception but got " + e.getSuppressed().length),
                () -> assertInstanceOf(IOException.class, e.getSuppressed()[0],
                        "Error: expected suppressed IOException but got " + e.getSuppressed()[0].getClass()),
                () -> assertEquals("cleanup failure", e.getSuppressed()[0].getMessage(),
                        "Error: expected suppressed message 'cleanup failure' but got '"
                                + e.getSuppressed()[0].getMessage() + "'"),
                () -> assertTrue(fs.provider.deletedFiles.isEmpty(),
                        "Error: expected no successful deletion but got " + fs.provider.deletedFiles)
        );
    }

    // ------------------------------------------------------------------
    // describeResult(IGame.ShotResult) - CC = 5
    // ------------------------------------------------------------------

    @Test
    @DisplayName("describeResult: invalid shot")
    void describeResult1() throws Exception {
        String actual = describeResult(new IGame.ShotResult(false, false, null, false));

        assertEquals("Tiro invalido", actual,
                "Error: expected 'Tiro invalido' but got '" + actual + "'");
    }

    @Test
    @DisplayName("describeResult: valid but repeated shot")
    void describeResult2() throws Exception {
        String actual = describeResult(new IGame.ShotResult(true, true, null, false));

        assertEquals("Tiro repetido", actual,
                "Error: expected 'Tiro repetido' but got '" + actual + "'");
    }

    @Test
    @DisplayName("describeResult: valid, new shot that hits water")
    void describeResult3() throws Exception {
        String actual = describeResult(new IGame.ShotResult(true, false, null, false));

        assertEquals("Agua", actual,
                "Error: expected 'Agua' but got '" + actual + "'");
    }

    @Test
    @DisplayName("describeResult: valid shot that sinks a ship")
    void describeResult4() throws Exception {
        IShip ship = new Barge(Compass.NORTH, new Position('A', 1));
        String actual = describeResult(new IGame.ShotResult(true, false, ship, true));

        assertEquals("Navio afundado (" + ship.getCategory() + ")", actual,
                "Error: expected 'Navio afundado (" + ship.getCategory() + ")' but got '" + actual + "'");
    }

    @Test
    @DisplayName("describeResult: valid shot that hits but does not sink a ship")
    void describeResult5() throws Exception {
        IShip ship = new Barge(Compass.NORTH, new Position('A', 1));
        String actual = describeResult(new IGame.ShotResult(true, false, ship, false));

        assertEquals("Navio atingido (" + ship.getCategory() + ")", actual,
                "Error: expected 'Navio atingido (" + ship.getCategory() + ")' but got '" + actual + "'");
    }

    // ------------------------------------------------------------------
    // writeLine(...) - CC = 1
    // ------------------------------------------------------------------

    @Test
    @DisplayName("writeLine: writes the given text into the PDF page")
    void writeLine() throws Exception {
        Path output = tempDir.resolve("write-line.pdf");
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                writeLine(content, "Test line", font, 11, 700);
            }
            document.save(output.toFile());
        }
        String text = pdfText(output);

        assertTrue(text.contains("Test line"),
                "Error: expected 'Test line' in the PDF but got: " + text);
    }

    // ------------------------------------------------------------------
    // Helpers (see PdfReportExporterTestHelper)
    // ------------------------------------------------------------------

    private static Path reserveAvailablePath(Path requested) throws Exception {
        return (Path) invokePrivate("reserveAvailablePath", new Class<?>[]{Path.class}, requested);
    }

    private static String describeResult(IGame.ShotResult result) throws Exception {
        return (String) invokePrivate("describeResult", new Class<?>[]{IGame.ShotResult.class}, result);
    }

    private static void writeLine(PDPageContentStream content, String text, PDFont font,
                                  float size, float y) throws Exception {
        invokePrivate("writeLine", new Class<?>[]{PDPageContentStream.class, String.class,
                PDFont.class, float.class, float.class}, content, text, font, size, y);
    }
}
