/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryDragEvent
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 */
package pl.laina.plots.gui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import pl.laina.plots.gui.MenuAction;
import pl.laina.plots.gui.PlotsMenuController;
import pl.laina.plots.gui.PlotsMenuHolder;
import pl.laina.plots.gui.ProtectedGuiHolder;
import pl.laina.plots.teleport.ProtectionStonesTeleportDelegate;

public final class GuiListener
implements Listener {
    private final PlotsMenuController menus;
    private final ProtectionStonesTeleportDelegate teleports;

    public GuiListener(PlotsMenuController menus, ProtectionStonesTeleportDelegate teleports) {
        this.menus = menus;
        this.teleports = teleports;
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder(false) instanceof ProtectedGuiHolder)) {
            return;
        }
        event.setCancelled(true);
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        if (!event.isLeftClick() || event.isShiftClick()) {
            return;
        }
        ItemStack clicked = event.getCurrentItem();
        this.menus.renderer().readPlotTarget(clicked).ifPresent(target -> this.teleports.request(player, target));
        this.menus.renderer().readAction(clicked).ifPresent(action -> this.handleAction(player, (MenuAction)((Object)action), event));
    }

    private void handleAction(Player player, MenuAction action, InventoryClickEvent event) {
        InventoryHolder inventoryHolder = event.getView().getTopInventory().getHolder(false);
        if (!(inventoryHolder instanceof PlotsMenuHolder)) {
            return;
        }
        PlotsMenuHolder holder = (PlotsMenuHolder)inventoryHolder;
        switch (action) {
            case PREVIOUS: {
                this.menus.show(player, holder.plots(), holder.filter(), holder.page() - 1);
                break;
            }
            case NEXT: {
                this.menus.show(player, holder.plots(), holder.filter(), holder.page() + 1);
                break;
            }
            case FILTER: {
                this.menus.show(player, holder.plots(), holder.filter().next(), 0);
                break;
            }
            case REFRESH: {
                this.menus.refresh(player);
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder(false) instanceof ProtectedGuiHolder) {
            event.setCancelled(true);
        }
    }
}
