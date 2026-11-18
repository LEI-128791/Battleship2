package battleship;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Minimal in-memory file system used by PdfReportExporterTest to simulate a
 * save failure and a successful or failing cleanup, without touching the disk.
 * The provider records which directories and files were created and deleted.
 */
final class FakeFileSystem extends FileSystem {
    final FakeProvider provider = new FakeProvider(this);

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSeparator() {
        return "/";
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return List.of();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return List.of();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return Set.of("basic");
    }

    @Override
    public Path getPath(String first, String... more) {
        throw new UnsupportedOperationException("getPath");
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        throw new UnsupportedOperationException("getPathMatcher");
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException("getUserPrincipalLookupService");
    }

    @Override
    public WatchService newWatchService() {
        throw new UnsupportedOperationException("newWatchService");
    }

    static final class FakeProvider extends FileSystemProvider {
        private final FakeFileSystem fileSystem;
        final List<Path> createdDirectories = new ArrayList<>();
        final List<Path> createdFiles = new ArrayList<>();
        final List<Path> deletedFiles = new ArrayList<>();
        boolean failDelete;

        private FakeProvider(FakeFileSystem fileSystem) {
            this.fileSystem = fileSystem;
        }

        @Override
        public String getScheme() {
            return "fake";
        }

        @Override
        public FileSystem newFileSystem(URI uri, Map<String, ?> env) {
            return fileSystem;
        }

        @Override
        public FileSystem getFileSystem(URI uri) {
            return fileSystem;
        }

        @Override
        public Path getPath(URI uri) {
            throw new UnsupportedOperationException("getPath");
        }

        @Override
        public SeekableByteChannel newByteChannel(Path path,
                                                  Set<? extends OpenOption> options,
                                                  FileAttribute<?>... attrs) {
            createdFiles.add(path);
            return (SeekableByteChannel) Proxy.newProxyInstance(
                    SeekableByteChannel.class.getClassLoader(),
                    new Class<?>[]{SeekableByteChannel.class},
                    (proxy, method, args) -> method.getName().equals("isOpen") ? Boolean.FALSE : null);
        }

        @Override
        public DirectoryStream<Path> newDirectoryStream(Path dir,
                                                        DirectoryStream.Filter<? super Path> filter) {
            throw new UnsupportedOperationException("newDirectoryStream");
        }

        @Override
        public void createDirectory(Path dir, FileAttribute<?>... attrs) {
            createdDirectories.add(dir);
        }

        @Override
        public void delete(Path path) throws IOException {
            deleteIfExists(path);
        }

        @Override
        public boolean deleteIfExists(Path path) throws IOException {
            if (failDelete) {
                throw new IOException("cleanup failure");
            }
            deletedFiles.add(path);
            return true;
        }

        @Override
        public void copy(Path source, Path target, CopyOption... options) {
            throw new UnsupportedOperationException("copy");
        }

        @Override
        public void move(Path source, Path target, CopyOption... options) {
            throw new UnsupportedOperationException("move");
        }

        @Override
        public boolean isSameFile(Path path, Path path2) {
            return path == path2;
        }

        @Override
        public boolean isHidden(Path path) {
            return false;
        }

        @Override
        public FileStore getFileStore(Path path) {
            throw new UnsupportedOperationException("getFileStore");
        }

        @Override
        public void checkAccess(Path path, AccessMode... modes) {
        }

        @Override
        public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type,
                                                                    LinkOption... options) {
            throw new UnsupportedOperationException("getFileAttributeView");
        }

        @Override
        public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type,
                                                                LinkOption... options) {
            throw new UnsupportedOperationException("readAttributes");
        }

        @Override
        public Map<String, Object> readAttributes(Path path, String attributes,
                                                  LinkOption... options) {
            throw new UnsupportedOperationException("readAttributes");
        }

        @Override
        public void setAttribute(Path path, String attribute, Object value,
                                 LinkOption... options) {
            throw new UnsupportedOperationException("setAttribute");
        }
    }
}
