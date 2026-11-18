package battleship;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Helpers for PdfReportExporterTest: reading generated PDFs, reflective access to
 * private methods, and Path proxies backed by FakeFileSystem to simulate failures.
 * Kept in a separate class so the test class itself stays below the class
 * complexity limit of the Large Class quality gate (Qodana ClassComplexity, 46).
 */
final class PdfReportExporterTestHelper {

    private PdfReportExporterTestHelper() {
    }

    static String pdfText(Path pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
            return new PDFTextStripper().getText(document);
        }
    }

    static int pageCount(Path pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
            return document.getNumberOfPages();
        }
    }

    /**
     * Builds input -> absolute -> parent -> candidate path proxies on a fake
     * file system, so the reserved candidate's toFile() can make the save fail.
     */
    static Path failingSaveChain(FakeFileSystem fs, Function<Object[], Object> toFile) {
        Path candidate = pathProxy(fs, "candidate.pdf", Map.of("toFile", toFile));
        Path parent = pathProxy(fs, "parent", Map.of("resolve", args -> candidate));
        Path absolute = pathProxy(fs, "report.pdf", Map.of(
                "getParent", args -> parent,
                "getFileName", args -> Path.of("report.pdf")));
        return pathProxy(fs, "report.pdf", Map.of("toAbsolutePath", args -> absolute));
    }

    static Path pathProxy(FakeFileSystem fs, String name,
                          Map<String, Function<Object[], Object>> overrides) {
        Map<String, Function<Object[], Object>> handlers = new HashMap<>(overrides);
        return (Path) Proxy.newProxyInstance(
                Path.class.getClassLoader(),
                new Class<?>[]{Path.class},
                (proxy, method, args) -> {
                    Function<Object[], Object> handler = handlers.get(method.getName());
                    if (handler != null) {
                        return handler.apply(args);
                    }
                    return switch (method.getName()) {
                        case "getFileSystem" -> fs;
                        case "toString" -> name;
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "equals" -> proxy == args[0];
                        default -> throw new UnsupportedOperationException(method.getName());
                    };
                });
    }

    /**
     * Invokes a private static method of PdfReportExporter, rethrowing the
     * exception thrown by the method itself instead of the reflection wrapper.
     */
    static Object invokePrivate(String name, Class<?>[] types, Object... args) throws Exception {
        Method method = PdfReportExporter.class.getDeclaredMethod(name, types);
        method.setAccessible(true);
        try {
            return method.invoke(null, args);
        } catch (InvocationTargetException e) {
            throw e.getCause() instanceof Exception cause ? cause : e;
        }
    }
}
