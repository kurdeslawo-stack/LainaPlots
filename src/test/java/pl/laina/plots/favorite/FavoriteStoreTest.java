package pl.laina.plots.favorite;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pl.laina.plots.model.PlotKey;

class FavoriteStoreTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void persistsFavoriteAcrossReloadAndRemovesItOnSecondToggle() throws Exception {
        Path file = this.temporaryDirectory.resolve("favorites.yml");
        UUID playerId = UUID.randomUUID();
        PlotKey plot = new PlotKey(UUID.randomUUID(), "region:with/unicode-ą");

        FavoriteStore first = new FavoriteStore(file, message -> {});
        assertTrue(first.toggle(playerId, plot));
        assertTrue(first.favorites(playerId).contains(plot));

        FavoriteStore reloaded = new FavoriteStore(file, message -> {});
        assertTrue(reloaded.favorites(playerId).contains(plot));
        assertFalse(reloaded.toggle(playerId, plot));

        FavoriteStore afterRemoval = new FavoriteStore(file, message -> {});
        assertFalse(afterRemoval.favorites(playerId).contains(plot));
    }

    @Test
    void ignoresMalformedEntriesWithoutLosingValidFavorites() throws Exception {
        Path file = this.temporaryDirectory.resolve("favorites.yml");
        UUID playerId = UUID.randomUUID();
        PlotKey valid = new PlotKey(UUID.randomUUID(), "valid");
        FavoriteStore seed = new FavoriteStore(file, message -> {});
        seed.toggle(playerId, valid);

        java.nio.file.Files.writeString(file, java.nio.file.Files.readString(file)
                .replace("  - ", "  - definitely-invalid\n  - "));
        ArrayList<String> warnings = new ArrayList<>();
        FavoriteStore reloaded = new FavoriteStore(file, warnings::add);

        assertTrue(reloaded.favorites(playerId).contains(valid));
    }
}
