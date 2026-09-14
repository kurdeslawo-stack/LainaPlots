/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.inventory.Inventory
 */
package pl.laina.plots.gui;

import org.bukkit.inventory.Inventory;
import pl.laina.plots.gui.ProtectedGuiHolder;

public final class LoadingHolder
implements ProtectedGuiHolder {
    private Inventory inventory;

    public void attach(Inventory inventory) {
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return this.inventory;
    }
}

