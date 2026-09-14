/*
 * Decompiled with CFR 0.152.
 */
package pl.laina.plots.model;

import java.util.Objects;
import java.util.UUID;

public record PlotKey(UUID worldId, String regionId) {
    public PlotKey {
        Objects.requireNonNull(worldId, "worldId");
        Objects.requireNonNull(regionId, "regionId");
    }
}

