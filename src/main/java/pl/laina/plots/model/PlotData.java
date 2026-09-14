/*
 * Decompiled with CFR 0.152.
 */
package pl.laina.plots.model;

import java.util.List;
import pl.laina.plots.model.PlotKey;
import pl.laina.plots.model.PlotRelation;

public record PlotData(PlotKey key, String displayName, String worldName, List<String> ownerNames, int memberCount, PlotRelation relation, int homeX, int homeY, int homeZ) {
}

