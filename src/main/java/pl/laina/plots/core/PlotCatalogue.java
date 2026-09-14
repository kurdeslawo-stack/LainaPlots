/*
 * Decompiled with CFR 0.152.
 */
package pl.laina.plots.core;

import java.util.Comparator;
import java.util.List;
import pl.laina.plots.core.Page;
import pl.laina.plots.model.PlotData;
import pl.laina.plots.model.PlotFilter;
import pl.laina.plots.model.PlotRelation;

public final class PlotCatalogue {
    private static final Comparator<PlotData> ORDER = Comparator.comparing((PlotData p) -> p.relation() == PlotRelation.OWNED ? 0 : 1).thenComparing(PlotData::displayName, String.CASE_INSENSITIVE_ORDER).thenComparing(PlotData::worldName, String.CASE_INSENSITIVE_ORDER).thenComparing(p -> p.key().regionId());

    public Page<PlotData> page(List<PlotData> all, PlotFilter filter, int requestedPage, int pageSize) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be positive");
        }
        List<PlotData> filtered = all.stream().filter(plot -> this.matches((PlotData)plot, filter)).sorted(ORDER).toList();
        int totalPages = Math.max(1, (filtered.size() + pageSize - 1) / pageSize);
        int page = Math.max(0, Math.min(requestedPage, totalPages - 1));
        int from = Math.min(filtered.size(), page * pageSize);
        int to = Math.min(filtered.size(), from + pageSize);
        return new Page<PlotData>(filtered.subList(from, to), page, totalPages, filtered.size());
    }

    private boolean matches(PlotData plot, PlotFilter filter) {
        return switch (filter) {
            default -> throw new MatchException(null, null);
            case PlotFilter.ALL -> true;
            case PlotFilter.OWNED -> {
                if (plot.relation() == PlotRelation.OWNED) {
                    yield true;
                }
                yield false;
            }
            case PlotFilter.SHARED -> plot.relation() == PlotRelation.SHARED;
        };
    }
}
