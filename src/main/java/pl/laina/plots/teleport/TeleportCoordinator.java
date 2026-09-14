/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerMoveEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
package pl.laina.plots.teleport;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import pl.laina.plots.LainaPlotsPlugin;
import pl.laina.plots.core.PlotAccessPolicy;
import pl.laina.plots.gui.PlotsMenuController;
import pl.laina.plots.message.Messages;
import pl.laina.plots.model.AccessResult;
import pl.laina.plots.model.PlotKey;
import pl.laina.plots.service.PlotGateway;

public final class TeleportCoordinator
implements Listener {
    private final LainaPlotsPlugin plugin;
    private final PlotGateway gateway;
    private final PlotAccessPolicy accessPolicy;
    private final PlotsMenuController menus;
    private final Messages messages;
    private final Map<UUID, PendingTeleport> pending = new ConcurrentHashMap<UUID, PendingTeleport>();

    public TeleportCoordinator(LainaPlotsPlugin plugin, PlotGateway gateway, PlotAccessPolicy accessPolicy, PlotsMenuController menus, Messages messages) {
        this.plugin = plugin;
        this.gateway = gateway;
        this.accessPolicy = accessPolicy;
        this.menus = menus;
        this.messages = messages;
    }

    public void request(Player player, PlotKey key) {
        if (this.pending.containsKey(player.getUniqueId())) {
            return;
        }
        this.pending.put(player.getUniqueId(), new PendingTeleport(player.getLocation().clone(), null));
        player.closeInventory();
        this.gateway.resolve(player, key).whenComplete((resolved, error) -> this.onMain(() -> {
            int delay;
            if (!player.isOnline()) {
                this.cancel(player.getUniqueId(), false);
                return;
            }
            if (error != null) {
                this.plugin.getLogger().warning("B\u0142\u0105d walidacji teleportu " + player.getName() + ": " + error.getMessage());
                this.cancel(player.getUniqueId(), false);
                this.messages.send(player, "teleport-failed");
                return;
            }
            AccessResult result = this.accessPolicy.validate(resolved.map(PlotGateway.ResolvedPlot::source), player.getUniqueId());
            if (result != AccessResult.ALLOWED) {
                this.cancel(player.getUniqueId(), false);
                this.deny(player, result);
                if (result == AccessResult.MISSING) {
                    this.menus.refresh(player);
                }
                return;
            }
            int n = delay = player.hasPermission("lainaplots.bypassdelay") ? 0 : this.plugin.settings().teleportDelaySeconds();
            if (delay == 0) {
                this.finish(player, key, (PlotGateway.ResolvedPlot)resolved.orElseThrow());
                return;
            }
            this.messages.send(player, "teleport-wait", Map.of("seconds", Integer.toString(delay)));
            BukkitTask task = Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.revalidateAndFinish(player, key), (long)delay * 20L);
            this.pending.put(player.getUniqueId(), new PendingTeleport(player.getLocation().clone(), task));
        }));
    }

    private void revalidateAndFinish(Player player, PlotKey key) {
        if (!player.isOnline() || !this.pending.containsKey(player.getUniqueId())) {
            return;
        }
        this.gateway.resolve(player, key).whenComplete((resolved, error) -> this.onMain(() -> {
            if (!player.isOnline() || !this.pending.containsKey(player.getUniqueId())) {
                return;
            }
            if (error != null) {
                this.cancel(player.getUniqueId(), false);
                this.messages.send(player, "teleport-failed");
                return;
            }
            AccessResult result = this.accessPolicy.validate(resolved.map(PlotGateway.ResolvedPlot::source), player.getUniqueId());
            if (result != AccessResult.ALLOWED) {
                this.cancel(player.getUniqueId(), false);
                this.deny(player, result);
                return;
            }
            this.finish(player, key, (PlotGateway.ResolvedPlot)resolved.orElseThrow());
        }));
    }

    private void finish(Player player, PlotKey key, PlotGateway.ResolvedPlot resolved) {
        Location home = resolved.home().clone();
        String plotName = resolved.source().nickname() == null || resolved.source().nickname().isBlank() ? resolved.source().key().regionId() : resolved.source().nickname();
        this.cancel(player.getUniqueId(), false);
        this.messages.send(player, "teleporting", Map.of("plot", plotName));
        player.teleportAsync(home).whenComplete((success, error) -> this.onMain(() -> {
            if (error != null || !Boolean.TRUE.equals(success)) {
                this.messages.send(player, "teleport-failed");
                return;
            }
            this.menus.markUsed(player, key);
            player.playSound(player.getLocation(), this.plugin.settings().teleportSound(), 0.8f, 1.1f);
        }));
    }

    private void deny(Player player, AccessResult result) {
        switch (result) {
            case MISSING: {
                this.messages.send(player, "plot-gone");
                break;
            }
            case DENIED: {
                this.messages.send(player, "access-lost");
                break;
            }
            case INVALID_HOME: {
                this.messages.send(player, "invalid-home");
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled=true)
    public void onMove(PlayerMoveEvent event) {
        PendingTeleport value = this.pending.get(event.getPlayer().getUniqueId());
        if (value == null || value.task() == null || event.getTo() == null) {
            return;
        }
        Location from = value.origin();
        Location to = event.getTo();
        if (from.getWorld() != to.getWorld() || from.distanceSquared(to) > 0.04) {
            this.cancel(event.getPlayer().getUniqueId(), true);
            this.messages.send(event.getPlayer(), "teleport-cancelled");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.cancel(event.getPlayer().getUniqueId(), false);
    }

    public void shutdown() {
        for (UUID uuid : this.pending.keySet()) {
            this.cancel(uuid, false);
        }
    }

    private void cancel(UUID uuid, boolean cancelTask) {
        PendingTeleport removed = this.pending.remove(uuid);
        if (cancelTask && removed != null && removed.task() != null) {
            removed.task().cancel();
        }
    }

    private void onMain(Runnable runnable) {
        Bukkit.getScheduler().runTask((Plugin)this.plugin, runnable);
    }

    private record PendingTeleport(Location origin, BukkitTask task) {
    }
}

