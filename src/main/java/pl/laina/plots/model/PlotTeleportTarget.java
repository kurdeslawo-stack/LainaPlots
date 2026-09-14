package pl.laina.plots.model;

import java.util.Objects;

public record PlotTeleportTarget(PlotKey key, String regionName) {
    public PlotTeleportTarget {
        Objects.requireNonNull(key, "key");
        regionName = regionName == null || regionName.isBlank() ? null : regionName;
    }
}
