/*
 * Decompiled with CFR 0.152.
 */
package pl.laina.plots.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import pl.laina.plots.model.PlotKey;

public record PlotSource(PlotKey key, String nickname, String worldName, Set<UUID> owners, List<String> ownerNames, Set<UUID> members, double homeX, double homeY, double homeZ, boolean validHome) {
    public PlotSource {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(worldName, "worldName");
        owners = Set.copyOf(owners);
        ownerNames = List.copyOf(ownerNames);
        members = Set.copyOf(members);
    }
}

