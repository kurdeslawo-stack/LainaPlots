package pl.laina.plots.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pl.laina.plots.model.PlotData;
import pl.laina.plots.model.PlotFilter;
import pl.laina.plots.model.PlotKey;
import pl.laina.plots.model.PlotRelation;

class PlotCatalogueTest {
    private final PlotCatalogue catalogue = new PlotCatalogue();

    @Test
    void keepsTwentyEightPlotsPerPageWithoutPaginationRegression() {
        ArrayList<PlotData> plots = new ArrayList<>();
        for (int i = 0; i < 35; ++i) {
            plots.add(plot("plot-" + i, "Plot " + i, PlotRelation.OWNED));
        }

        Page<PlotData> first = this.catalogue.page(plots, PlotFilter.ALL, 0, 28);
        Page<PlotData> second = this.catalogue.page(plots, PlotFilter.ALL, 1, 28);

        assertEquals(28, first.entries().size());
        assertTrue(first.hasNext());
        assertEquals(7, second.entries().size());
        assertTrue(second.hasPrevious());
        assertFalse(second.hasNext());
    }

    @Test
    void filtersOwnedAndSharedPlots() {
        List<PlotData> plots = List.of(plot("owned", "Owned", PlotRelation.OWNED), plot("shared", "Shared", PlotRelation.SHARED));

        assertEquals(List.of(PlotRelation.OWNED), this.catalogue.page(plots, PlotFilter.OWNED, 0, 28).entries().stream().map(PlotData::relation).toList());
        assertEquals(List.of(PlotRelation.SHARED), this.catalogue.page(plots, PlotFilter.SHARED, 0, 28).entries().stream().map(PlotData::relation).toList());
        assertEquals(2, this.catalogue.page(plots, PlotFilter.ALL, 0, 28).totalEntries());
    }

    @Test
    void sortsOwnersBeforeMembersThenByName() {
        List<PlotData> plots = List.of(plot("3", "Zulu", PlotRelation.SHARED), plot("2", "beta", PlotRelation.OWNED), plot("1", "Alpha", PlotRelation.OWNED));

        List<String> names = this.catalogue.page(plots, PlotFilter.ALL, 0, 28).entries().stream().map(PlotData::displayName).toList();

        assertEquals(List.of("Alpha", "beta", "Zulu"), names);
    }

    private static PlotData plot(String id, String name, PlotRelation relation) {
        UUID world = UUID.nameUUIDFromBytes("world".getBytes());
        return new PlotData(new PlotKey(world, id), null, name, "world", List.of("Owner"), 0, relation, 0, 64, 0);
    }
}
