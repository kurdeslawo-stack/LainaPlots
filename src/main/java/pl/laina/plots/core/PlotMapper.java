/*
 * Decompiled with CFR 0.152.
 */
package pl.laina.plots.core;

import java.util.UUID;
import pl.laina.plots.model.PlotData;
import pl.laina.plots.model.PlotRelation;
import pl.laina.plots.model.PlotSource;

public final class PlotMapper {
    public PlotData map(PlotSource source, UUID viewer) {
        PlotRelation relation;
        if (source.owners().contains(viewer)) {
            relation = PlotRelation.OWNED;
        } else if (source.members().contains(viewer)) {
            relation = PlotRelation.SHARED;
        } else {
            throw new IllegalArgumentException("Viewer has no access to plot " + source.key().regionId());
        }
        Object name = source.nickname() == null || source.nickname().isBlank() ? "Dzia\u0142ka " + PlotMapper.shortId(source.key().regionId()) : source.nickname();
        return new PlotData(source.key(), source.nickname(), (String)name, source.worldName(), source.ownerNames(), source.members().size(), relation, PlotMapper.floor(source.homeX()), PlotMapper.floor(source.homeY()), PlotMapper.floor(source.homeZ()));
    }

    private static String shortId(String id) {
        return id.length() <= 14 ? id : id.substring(0, 14) + "\u2026";
    }

    private static int floor(double value) {
        return (int)Math.floor(value);
    }
}
