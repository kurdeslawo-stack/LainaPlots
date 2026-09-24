package pl.laina.plots.favorite;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.laina.plots.model.PlotKey;

public final class FavoriteStore {
    private static final String PLAYERS_PATH = "players";

    private final Path file;
    private final Consumer<String> warningLogger;
    private final Map<UUID, Set<PlotKey>> favorites = new HashMap<>();

    public FavoriteStore(Path file, Consumer<String> warningLogger) {
        this.file = file;
        this.warningLogger = warningLogger;
        this.load();
    }

    public synchronized Set<PlotKey> favorites(UUID playerId) {
        return Set.copyOf(this.favorites.getOrDefault(playerId, Set.of()));
    }

    public synchronized boolean toggle(UUID playerId, PlotKey plot) throws IOException {
        Set<PlotKey> playerFavorites = this.favorites.computeIfAbsent(playerId, ignored -> new HashSet<>());
        boolean added = playerFavorites.add(plot);
        if (!added) {
            playerFavorites.remove(plot);
        }
        if (playerFavorites.isEmpty()) {
            this.favorites.remove(playerId);
        }

        try {
            this.save();
            return added;
        } catch (IOException exception) {
            Set<PlotKey> rollback = this.favorites.computeIfAbsent(playerId, ignored -> new HashSet<>());
            if (added) {
                rollback.remove(plot);
            } else {
                rollback.add(plot);
            }
            if (rollback.isEmpty()) {
                this.favorites.remove(playerId);
            }
            throw exception;
        }
    }

    private void load() {
        if (!Files.isRegularFile(this.file)) {
            return;
        }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(this.file.toFile());
        } catch (IOException | InvalidConfigurationException exception) {
            this.warningLogger.accept("Nie udało się wczytać favorites.yml: " + exception.getMessage());
            return;
        }

        var players = config.getConfigurationSection(PLAYERS_PATH);
        if (players == null) {
            return;
        }
        for (String rawPlayerId : players.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(rawPlayerId);
                Set<PlotKey> playerFavorites = new HashSet<>();
                for (String encoded : players.getStringList(rawPlayerId)) {
                    decode(encoded).ifPresent(playerFavorites::add);
                }
                if (!playerFavorites.isEmpty()) {
                    this.favorites.put(playerId, playerFavorites);
                }
            } catch (IllegalArgumentException exception) {
                this.warningLogger.accept("Pomijam nieprawidłowy identyfikator gracza w favorites.yml: " + rawPlayerId);
            }
        }
    }

    private void save() throws IOException {
        Files.createDirectories(this.file.getParent());
        YamlConfiguration config = new YamlConfiguration();
        this.favorites.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> config.set(
                        PLAYERS_PATH + "." + entry.getKey(),
                        entry.getValue().stream().map(FavoriteStore::encode).sorted().toList()
                ));
        config.save(this.file.toFile());
    }

    private static String encode(PlotKey key) {
        String region = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(key.regionId().getBytes(StandardCharsets.UTF_8));
        return key.worldId() + ":" + region;
    }

    private static java.util.Optional<PlotKey> decode(String encoded) {
        int separator = encoded.indexOf(':');
        if (separator < 1 || separator == encoded.length() - 1) {
            return java.util.Optional.empty();
        }
        try {
            UUID worldId = UUID.fromString(encoded.substring(0, separator));
            String regionId = new String(
                    Base64.getUrlDecoder().decode(encoded.substring(separator + 1)),
                    StandardCharsets.UTF_8
            );
            return regionId.isEmpty()
                    ? java.util.Optional.empty()
                    : java.util.Optional.of(new PlotKey(worldId, regionId));
        } catch (IllegalArgumentException exception) {
            return java.util.Optional.empty();
        }
    }
}
