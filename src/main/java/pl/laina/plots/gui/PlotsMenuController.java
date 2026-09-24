/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.plugin.Plugin
 */
package pl.laina.plots.gui;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import pl.laina.plots.LainaPlotsPlugin;
import pl.laina.plots.core.PlotMapper;
import pl.laina.plots.favorite.FavoriteStore;
import pl.laina.plots.gui.PlotsMenuRenderer;
import pl.laina.plots.message.Messages;
import pl.laina.plots.model.PlotData;
import pl.laina.plots.model.PlotFilter;
import pl.laina.plots.model.PlotKey;
import pl.laina.plots.model.PlotSource;
import pl.laina.plots.service.PlotGateway;

public final class PlotsMenuController {
    private final LainaPlotsPlugin plugin;
    private final PlotGateway gateway;
    private final PlotMapper mapper;
    private final PlotsMenuRenderer renderer;
    private final Messages messages;
    private final FavoriteStore favorites;
    private final Map<UUID, PlotKey> lastUsed = new ConcurrentHashMap<UUID, PlotKey>();

    public PlotsMenuController(LainaPlotsPlugin plugin, PlotGateway gateway, PlotMapper mapper, PlotsMenuRenderer renderer, Messages messages, FavoriteStore favorites) {
        this.plugin = plugin;
        this.gateway = gateway;
        this.mapper = mapper;
        this.renderer = renderer;
        this.messages = messages;
        this.favorites = favorites;
    }

    public void open(Player player) {
        player.openInventory(this.renderer.loading(this.plugin.settings()));
        this.messages.send(player, "loading");
        this.gateway.findAccessible(player).whenComplete((sources, error) -> this.onMain(() -> {
            if (!player.isOnline()) {
                return;
            }
            if (error != null) {
                this.plugin.getLogger().severe("Nie uda\u0142o si\u0119 pobra\u0107 dzia\u0142ek gracza " + player.getName() + ": " + error.getMessage());
                player.closeInventory();
                this.messages.send(player, "load-failed");
                return;
            }
            List<PlotData> plots = sources.stream().map(source -> this.mapper.map((PlotSource)source, player.getUniqueId())).toList();
            this.show(player, plots, PlotFilter.ALL, 0);
        }));
    }

    public void show(Player player, List<PlotData> plots, PlotFilter filter, int page) {
        Inventory inventory = this.renderer.render(
                plots,
                filter,
                page,
                this.plugin.settings(),
                Optional.ofNullable(this.lastUsed.get(player.getUniqueId())),
                this.favorites.favorites(player.getUniqueId())
        );
        player.openInventory(inventory);
    }

    public void toggleFavorite(Player player, List<PlotData> plots, PlotFilter filter, int page, PlotKey plot) {
        try {
            this.favorites.toggle(player.getUniqueId(), plot);
            this.show(player, plots, filter, page);
        } catch (IOException exception) {
            this.plugin.getLogger().severe("Nie udało się zapisać ulubionych gracza " + player.getName() + ": " + exception.getMessage());
            player.sendMessage(this.messages.parse("<red>Nie udało się zapisać ulubionej działki."));
        }
    }

    public void refresh(Player player) {
        this.open(player);
    }

    public void markUsed(Player player, PlotKey key) {
        this.lastUsed.put(player.getUniqueId(), key);
    }

    public PlotsMenuRenderer renderer() {
        return this.renderer;
    }

    private void onMain(Runnable runnable) {
        Bukkit.getScheduler().runTask((Plugin)this.plugin, runnable);
    }
}
