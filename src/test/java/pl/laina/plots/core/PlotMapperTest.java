package pl.laina.plots.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pl.laina.plots.model.PlotKey;
import pl.laina.plots.model.PlotRelation;
import pl.laina.plots.model.PlotSource;

class PlotMapperTest {
    @Test
    void ownerTakesPriorityWhenViewerIsAlsoListedAsMember() {
        UUID viewer = UUID.randomUUID();
        PlotSource source = new PlotSource(new PlotKey(UUID.randomUUID(), "region"), "Name", "world", Set.of(viewer), List.of("Owner"), Set.of(viewer), 1.0, 64.0, 2.0);

        assertEquals(PlotRelation.OWNED, new PlotMapper().map(source, viewer).relation());
    }
}
