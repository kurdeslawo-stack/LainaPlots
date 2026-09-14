/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.espi.protectionstones.PSPlayer
 *  dev.espi.protectionstones.PSRegion
 *  dev.espi.protectionstones.ProtectionStones
 *  dev.espi.protectionstones.utils.UUIDCache
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 */
package pl.laina.plots.service;

import dev.espi.protectionstones.PSPlayer;
import dev.espi.protectionstones.PSRegion;
import dev.espi.protectionstones.ProtectionStones;
import dev.espi.protectionstones.utils.UUIDCache;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pl.laina.plots.model.PlotKey;
import pl.laina.plots.model.PlotSource;
import pl.laina.plots.service.PlotGateway;

public final class ProtectionStonesPlotGateway
implements PlotGateway {
    private final JavaPlugin plugin;
    private final ProtectionStones protectionStones;

    public ProtectionStonesPlotGateway(JavaPlugin plugin, ProtectionStones protectionStones) {
        this.plugin = plugin;
        this.protectionStones = protectionStones;
    }

    @Override
    public CompletableFuture<List<PlotSource>> findAccessible(Player player) {
        List<World> worlds = List.copyOf(Bukkit.getWorlds());
        UUID viewer = player.getUniqueId();
        return this.async(() -> {
            boolean includeMembers = this.protectionStones.getConfigOptions().allowHomeTeleportForMembers;
            PSPlayer psPlayer = PSPlayer.fromUUID((UUID)viewer);
            LinkedHashMap<PlotKey, PlotSource> unique = new LinkedHashMap<PlotKey, PlotSource>();
            for (World world : worlds) {
                for (PSRegion region : psPlayer.getPSRegions(world, includeMembers)) {
                    if (!this.isUsableHome(region)) continue;
                    PlotSource source = this.map(region);
                    unique.putIfAbsent(source.key(), source);
                }
            }
            return new ArrayList(unique.values());
        });
    }

    @Override
    public CompletableFuture<Optional<PlotGateway.ResolvedPlot>> resolve(Player player, PlotKey key) {
        UUID viewer = player.getUniqueId();
        World world = Bukkit.getWorld((UUID)key.worldId());
        if (world == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return this.async(() -> {
            boolean includeMembers = this.protectionStones.getConfigOptions().allowHomeTeleportForMembers;
            return PSPlayer.fromUUID((UUID)viewer).getPSRegions(world, includeMembers).stream().filter(region -> region.getId().equals(key.regionId())).filter(this::isUsableHome).findFirst().map(region -> new PlotGateway.ResolvedPlot(this.map(region), region.getHome()));
        });
    }

    private boolean isUsableHome(PSRegion region) {
        return region != null && region.getTypeOptions() != null && !region.getTypeOptions().preventPsHome;
    }

    private PlotSource map(PSRegion region) {
        Location home = region.getHome();
        boolean valid = this.isValid(home, region.getWorld());
        double x = valid ? home.getX() : 0.0;
        double y = valid ? home.getY() : 0.0;
        double z = valid ? home.getZ() : 0.0;
        Set<UUID> owners = Set.copyOf(region.getOwners());
        Set<UUID> members = Set.copyOf(region.getMembers());
        List<String> ownerNames = owners.stream().map(this::playerName).sorted(String.CASE_INSENSITIVE_ORDER).toList();
        return new PlotSource(new PlotKey(region.getWorld().getUID(), region.getId()), region.getName(), region.getWorld().getName(), owners, ownerNames, members, x, y, z, valid);
    }

    private String playerName(UUID uuid) {
        String cached = UUIDCache.getNameFromUUID((UUID)uuid);
        return cached == null || cached.isBlank() ? uuid.toString().substring(0, 8) : cached;
    }

    private boolean isValid(Location location, World expectedWorld) {
        return location != null && location.getWorld() != null && location.getWorld().getUID().equals(expectedWorld.getUID()) && Double.isFinite(location.getX()) && Double.isFinite(location.getY()) && Double.isFinite(location.getZ()) && location.getY() >= (double)expectedWorld.getMinHeight() && location.getY() < (double)expectedWorld.getMaxHeight();
    }

    private <T> CompletableFuture<T> async(Callable<T> operation) {
        CompletableFuture future = new CompletableFuture();
        Bukkit.getScheduler().runTaskAsynchronously((Plugin)this.plugin, () -> {
            try {
                future.complete(operation.call());
            }
            catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }
}
