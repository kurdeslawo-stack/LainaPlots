package pl.laina.plots.resourcepack;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.laina.plots.LainaPlotsPlugin;
import pl.laina.plots.config.ResourcePackSettings;
import pl.laina.plots.message.Messages;

public final class ResourcePackManager implements Listener, AutoCloseable {
    private static final String BUNDLED_PACK = "LainaPlots-Icons.zip";
    private static final String HTTP_PATH = "/lainaplots-icons.zip";
    private static final UUID PACK_ID = UUID.nameUUIDFromBytes("lainaplots-icons".getBytes(StandardCharsets.UTF_8));

    private final LainaPlotsPlugin plugin;
    private final Messages messages;
    private HttpServer server;
    private ExecutorService executor;
    private ResourcePackSettings settings;

    public ResourcePackManager(LainaPlotsPlugin plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    public void start(ResourcePackSettings settings) {
        this.settings = settings;
        this.ensureBundledPack(settings);
        this.startServerIfNeeded(settings);
    }

    public void reload(ResourcePackSettings settings) {
        this.close();
        this.start(settings);
        if (settings.enabled()) {
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> Bukkit.getOnlinePlayers().forEach(this::sendTo), 20L);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (event.getPlayer().isOnline()) {
                this.sendTo(event.getPlayer());
            }
        }, 20L);
    }

    public void sendTo(Player player) {
        ResourcePackSettings current = this.settings;
        if (current == null || !current.enabled() || current.url().isBlank()) {
            return;
        }
        Path pack = this.packPath(current);
        if (!Files.isRegularFile(pack)) {
            this.plugin.getLogger().warning("Nie znaleziono paczki zasobów LainaPlots: " + pack);
            return;
        }
        try {
            byte[] sha1 = MessageDigest.getInstance("SHA-1").digest(Files.readAllBytes(pack));
            player.setResourcePack(PACK_ID, current.url(), sha1, this.messages.parse(current.prompt()), current.required());
            this.plugin.getLogger().fine("Wysłano paczkę ikon LainaPlots graczowi " + player.getName() + " (SHA-1 " + HexFormat.of().formatHex(sha1) + ").");
        } catch (IOException | NoSuchAlgorithmException exception) {
            this.plugin.getLogger().warning("Nie udało się wysłać paczki ikon LainaPlots: " + exception.getMessage());
        }
    }

    private void ensureBundledPack(ResourcePackSettings settings) {
        Path pack = this.packPath(settings);
        if (Files.exists(pack)) {
            return;
        }
        if (!BUNDLED_PACK.equals(settings.fileName())) {
            this.plugin.getLogger().warning("Brakuje zewnętrznej paczki zasobów: " + pack);
            return;
        }
        this.plugin.saveResource(BUNDLED_PACK, false);
    }

    private void startServerIfNeeded(ResourcePackSettings settings) {
        if (!settings.enabled() || !settings.serveEnabled()) {
            return;
        }
        try {
            this.server = HttpServer.create(new InetSocketAddress(settings.bindAddress(), settings.port()), 0);
            this.server.createContext(HTTP_PATH, exchange -> this.serve(exchange, settings));
            this.executor = Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "LainaPlots-resource-pack");
                thread.setDaemon(true);
                return thread;
            });
            this.server.setExecutor(this.executor);
            this.server.start();
            this.plugin.getLogger().info("Paczka ikon dostępna pod " + settings.url());
        } catch (IOException exception) {
            this.plugin.getLogger().warning("Nie udało się uruchomić serwera paczki ikon na porcie " + settings.port() + ": " + exception.getMessage());
        }
    }

    private void serve(HttpExchange exchange, ResourcePackSettings settings) throws IOException {
        try (exchange) {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            Path pack = this.packPath(settings);
            if (!Files.isRegularFile(pack)) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            byte[] content = Files.readAllBytes(pack);
            exchange.getResponseHeaders().set("Content-Type", "application/zip");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            exchange.sendResponseHeaders(200, content.length);
            exchange.getResponseBody().write(content);
        }
    }

    private Path packPath(ResourcePackSettings settings) {
        return this.plugin.getDataFolder().toPath().resolve(settings.fileName());
    }

    @Override
    public void close() {
        if (this.server != null) {
            this.server.stop(0);
            this.server = null;
        }
        if (this.executor != null) {
            this.executor.shutdownNow();
            this.executor = null;
        }
    }
}
