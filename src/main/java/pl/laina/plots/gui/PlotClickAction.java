package pl.laina.plots.gui;

enum PlotClickAction {
    TELEPORT,
    TOGGLE_FAVORITE,
    NONE;

    static PlotClickAction from(boolean leftClick, boolean rightClick, boolean shiftClick) {
        if (shiftClick) {
            return NONE;
        }
        if (leftClick) {
            return TELEPORT;
        }
        if (rightClick) {
            return TOGGLE_FAVORITE;
        }
        return NONE;
    }
}
