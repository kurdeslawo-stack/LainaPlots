/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 */
package pl.laina.plots.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import pl.laina.plots.model.PlotSource;

public interface PlotGateway {
    public CompletableFuture<List<PlotSource>> findAccessible(Player var1);

}
