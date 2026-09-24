package pl.laina.plots.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PlotClickActionTest {
    @Test
    void leftClickTeleportsAndRightClickTogglesFavorite() {
        assertEquals(PlotClickAction.TELEPORT, PlotClickAction.from(true, false, false));
        assertEquals(PlotClickAction.TOGGLE_FAVORITE, PlotClickAction.from(false, true, false));
    }

    @Test
    void shiftAndUnsupportedClicksDoNothing() {
        assertEquals(PlotClickAction.NONE, PlotClickAction.from(true, false, true));
        assertEquals(PlotClickAction.NONE, PlotClickAction.from(false, false, false));
    }
}
