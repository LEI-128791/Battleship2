
package battleship;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PdfReportExporter {

    private static final float LEFT_MARGIN = 50;
    private static final float FIRST_LINE_Y = 790;
    private static final float LINE_HEIGHT = 15;
    private static final int LINES_PER_PAGE = 45;

    private PdfReportExporter() {
    }

    public static void export(List<IMove> moves, Path outputPath)
            throws IOException {

        Objects.requireNonNull(moves, "moves cannot be null");
        Objects.requireNonNull(outputPath, "outputPath cannot be null");

        Path absolutePath = outputPath.toAbsolutePath();
        Path parent = absolutePath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        List<ReportLine> lines = new ArrayList<>();

        if (moves.isEmpty()) {
            lines.add(new ReportLine(
                    "Nao existem jogadas registadas.", false));
        } else {
            for (IMove move : moves) {
                lines.add(new ReportLine(
                        "Jogada " + move.getNumber(), true));

                List<IPosition> shots = move.getShots();
                List<IGame.ShotResult> results =
                        move.getShotResults();

                for (int i = 0; i < shots.size(); i++) {
                    IPosition shot = shots.get(i);

                    String outcome = i < results.size()
                            ? describeResult(results.get(i))
                            : "Resultado nao disponivel";

                    String text = "Tiro "
                            + shot.getClassicRow()
                            + shot.getClassicColumn()
                            + ": " + outcome;

                    lines.add(new ReportLine(text, false));
                }

                lines.add(new ReportLine("", false));
            }
        }

        PDFont regularFont = new PDType1Font(
                Standard14Fonts.FontName.HELVETICA);
        PDFont boldFont = new PDType1Font(
                Standard14Fonts.FontName.HELVETICA_BOLD);

        try (PDDocument document = new PDDocument()) {
            int pageCount = Math.max(1,
                    (int) Math.ceil(
                            (double) lines.size() / LINES_PER_PAGE));

            for (int pageIndex = 0;
                 pageIndex < pageCount;
                 pageIndex++) {

                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream content =
                             new PDPageContentStream(document, page)) {

                    float y = FIRST_LINE_Y;

                    writeLine(content,
                            "Historico de jogadas - Batalha Naval",
                            boldFont, 16, y);

                    y -= 30;

                    if (pageIndex > 0) {
                        writeLine(content, "Continuacao",
                                boldFont, 12, y);
                        y -= LINE_HEIGHT * 2;
                    }

                    int start = pageIndex * LINES_PER_PAGE;
                    int end = Math.min(
                            start + LINES_PER_PAGE, lines.size());

                    for (int i = start; i < end; i++) {
                        ReportLine line = lines.get(i);

                        if (!line.text().isEmpty()) {
                            PDFont font = line.heading()
                                    ? boldFont : regularFont;

                            writeLine(content, line.text(),
                                    font, 11, y);
                        }

                        y -= LINE_HEIGHT;
                    }
                }
            }

            document.save(absolutePath.toFile());
        }
    }

    private static String describeResult(IGame.ShotResult result) {
        if (!result.valid()) {
            return "Tiro invalido";
        }

        if (result.repeated()) {
            return "Tiro repetido";
        }

        if (result.ship() == null) {
            return "Agua";
        }

        if (result.sunk()) {
            return "Navio afundado ("
                    + result.ship().getCategory() + ")";
        }

        return "Navio atingido ("
                + result.ship().getCategory() + ")";
    }

    private static void writeLine(
            PDPageContentStream content,
            String text,
            PDFont font,
            float fontSize,
            float y) throws IOException {

        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(LEFT_MARGIN, y);
        content.showText(text);
        content.endText();
    }

    private record ReportLine(String text, boolean heading) {
    }
}
