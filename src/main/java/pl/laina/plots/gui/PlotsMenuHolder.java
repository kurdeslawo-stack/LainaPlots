/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.inventory.Inventory
 */
package pl.laina.plots.gui;

import java.util.List;
import org.bukkit.inventory.Inventory;
import pl.laina.plots.gui.ProtectedGuiHolder;
import pl.laina.plots.model.PlotData;
import pl.laina.plots.model.PlotFilter;

public final class PlotsMenuHolder
implements ProtectedGuiHolder {
    private final List<PlotData> plots;
    private final PlotFilter filter;
    private final int page;
    private Inventory inventory;

    public PlotsMenuHolder(List<PlotData> plots, PlotFilter filter, int page) {
        this.plots = List.copyOf(plots);
        this.filter = filter;
        this.page = page;
    }

    public List<PlotData> plots() {
        return this.plots;
    }

    public PlotFilter filter() {
        return this.filter;
    }

    public int page() {
        return this.page;
    }

    public void attach(Inventory inventory) {
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return this.inventory;
    }
}

