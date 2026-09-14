package pl.laina.plots.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

public final class DefaultConfigInstaller {
    private static final String CONFIG_FILE_NAME = "config.yml";

    private DefaultConfigInstaller() {
    }

    public static boolean installIfMissing(Path dataFolder, Supplier<InputStream> resourceSupplier) throws IOException {
        Objects.requireNonNull(dataFolder, "dataFolder");
        Objects.requireNonNull(resourceSupplier, "resourceSupplier");

        Path configFile = dataFolder.resolve(CONFIG_FILE_NAME);
        if (Files.exists(configFile)) {
            return false;
        }

        Files.createDirectories(dataFolder);
        Path temporaryFile = Files.createTempFile(dataFolder, CONFIG_FILE_NAME + ".", ".tmp");
        try {
            long copiedBytes;
            try (InputStream source = resourceSupplier.get();
                 OutputStream destination = Files.newOutputStream(temporaryFile)) {
                if (source == null) {
                    throw new IOException("Brak zasobu config.yml w JAR-ze pluginu.");
                }
                copiedBytes = copyFully(source, destination);
            }

            if (copiedBytes == 0L) {
                throw new IOException("Zasób config.yml w JAR-ze pluginu jest pusty.");
            }

            Files.move(temporaryFile, configFile);
            return true;
        }
        catch (FileAlreadyExistsException ignored) {
            return false;
        }
        finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private static long copyFully(InputStream source, OutputStream destination) throws IOException {
        byte[] buffer = new byte[8192];
        long copiedBytes = 0L;
        while (true) {
            int read = source.read(buffer);
            if (read < 0) {
                return copiedBytes;
            }
            if (read == 0) {
                int singleByte = source.read();
                if (singleByte < 0) {
                    return copiedBytes;
                }
                destination.write(singleByte);
                ++copiedBytes;
                continue;
            }
            destination.write(buffer, 0, read);
            copiedBytes += read;
        }
    }

}
