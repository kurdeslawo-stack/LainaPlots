package pl.laina.plots.config;

import org.bukkit.Material;
import pl.laina.plots.model.PlotRelation;

public enum GuiIcon {
    FILTER("filter", Material.HOPPER),
    SUMMARY("summary", Material.BOOK),
    PREVIOUS_PAGE("previous-page", Material.ARROW),
    NEXT_PAGE("next-page", Material.ARROW),
    REFRESH("refresh", Material.SUNFLOWER),
    FILLER("filler", Material.BLACK_STAINED_GLASS_PANE),
    OWNER_PLOT("owner-plot", Material.GRASS_BLOCK),
    MEMBER_PLOT("member-plot", Material.CYAN_STAINED_GLASS),
    LOADING("loading", Material.CLOCK),
    EMPTY_STATE("empty-state", Material.FLOWER_POT);

    private final String configKey;
    private final Material fallbackMaterial;

    GuiIcon(String configKey, Material fallbackMaterial) {
        this.configKey = configKey;
        this.fallbackMaterial = fallbackMaterial;
    }

    public String configKey() {
        return this.configKey;
    }

    public Material fallbackMaterial() {
        return this.fallbackMaterial;
    }

    public static GuiIcon forPlot(PlotRelation relation) {
        return relation == PlotRelation.OWNED ? OWNER_PLOT : MEMBER_PLOT;
    }
}
