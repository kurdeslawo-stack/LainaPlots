package pl.laina.plots.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultConfigInstallerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void copiesTheCompleteDefaultConfigWhenTheFileIsMissingEvenAfterAZeroLengthRead() throws IOException {
        byte[] expected;
        try (InputStream resource = DefaultConfigInstallerTest.class.getResourceAsStream("/config.yml")) {
            assertNotNull(resource);
            expected = resource.readAllBytes();
        }
        assertTrue(expected.length > 0);

        boolean installed = DefaultConfigInstaller.installIfMissing(
                temporaryDirectory,
                () -> new ZeroThenDataInputStream(expected)
        );

        assertTrue(installed);
        assertArrayEquals(expected, Files.readAllBytes(temporaryDirectory.resolve("config.yml")));
    }

    @Test
    void leavesAnExistingConfigUntouched() throws IOException {
        Path configFile = temporaryDirectory.resolve("config.yml");
        byte[] existing = "legacy-setting: true\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Files.write(configFile, existing);

        boolean installed = DefaultConfigInstaller.installIfMissing(
                temporaryDirectory,
                () -> new ByteArrayInputStream("replacement: true\n".getBytes(java.nio.charset.StandardCharsets.UTF_8))
        );

        assertFalse(installed);
        assertArrayEquals(existing, Files.readAllBytes(configFile));
    }

    @Test
    void defaultConfigDoesNotConfigureASeparateResourcePack() throws IOException {
        String config;
        try (InputStream resource = DefaultConfigInstallerTest.class.getResourceAsStream("/config.yml")) {
            assertNotNull(resource);
            config = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertFalse(config.contains("resource-pack:"));
        assertFalse(config.matches("(?s).*custom-model-data:\\s*[1-9][0-9]*.*"));
    }

    private static final class ZeroThenDataInputStream extends InputStream {
        private final ByteArrayInputStream delegate;
        private boolean returnedZero;

        private ZeroThenDataInputStream(byte[] data) {
            this.delegate = new ByteArrayInputStream(data);
        }

        @Override
        public int read(byte[] buffer, int offset, int length) {
            if (!this.returnedZero) {
                this.returnedZero = true;
                return 0;
            }
            return this.delegate.read(buffer, offset, length);
        }

        @Override
        public int read() {
            return this.delegate.read();
        }
    }
}
