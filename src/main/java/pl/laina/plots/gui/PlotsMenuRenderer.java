/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.minimessage.MiniMessage
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 */
package pl.laina.plots.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pl.laina.plots.config.PluginSettings;
import pl.laina.plots.config.ConfiguredIcon;
import pl.laina.plots.config.GuiIcon;
import pl.laina.plots.config.GuiIcons;
import pl.laina.plots.core.Page;
import pl.laina.plots.core.PlotCatalogue;
import pl.laina.plots.gui.LoadingHolder;
import pl.laina.plots.gui.MenuAction;
import pl.laina.plots.gui.PlotsMenuHolder;
import pl.laina.plots.message.Messages;
import pl.laina.plots.model.PlotData;
import pl.laina.plots.model.PlotFilter;
import pl.laina.plots.model.PlotKey;
import pl.laina.plots.model.PlotRelation;
import pl.laina.plots.model.PlotTeleportTarget;

public final class PlotsMenuRenderer {
    public static final int[] PLOT_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    private final JavaPlugin plugin;
    private final Messages messages;
    private final PlotCatalogue catalogue;
    private final NamespacedKey plotWorldKey;
    private final NamespacedKey plotRegionKey;
    private final NamespacedKey plotNameKey;
    private final NamespacedKey actionKey;

    public PlotsMenuRenderer(JavaPlugin plugin, Messages messages, PlotCatalogue catalogue) {
        this.plugin = plugin;
        this.messages = messages;
        this.catalogue = catalogue;
        this.plotWorldKey = new NamespacedKey((Plugin)plugin, "plot_world");
        this.plotRegionKey = new NamespacedKey((Plugin)plugin, "plot_region");
        this.plotNameKey = new NamespacedKey((Plugin)plugin, "plot_name");
        this.actionKey = new NamespacedKey((Plugin)plugin, "menu_action");
    }

    public Inventory loading(PluginSettings settings) {
        LoadingHolder holder = new LoadingHolder();
        Inventory inventory = Bukkit.createInventory((InventoryHolder)holder, (int)27, (Component)this.messages.parse(settings.guiTitle()));
        holder.attach(inventory);
        this.fill(inventory, settings.icons().get(GuiIcon.FILLER));
        inventory.setItem(13, this.item(settings.icons().get(GuiIcon.LOADING), "<yellow><bold>Wczytywanie\u2026</bold>", List.of("<gray>Szukam Twoich dzia\u0142ek.")));
        return inventory;
    }

    public Inventory render(List<PlotData> plots, PlotFilter filter, int requestedPage, PluginSettings settings, Optional<PlotKey> lastUsed) {
        Page<PlotData> page = this.catalogue.page(plots, filter, requestedPage, settings.plotsPerPage());
        PlotsMenuHolder holder = new PlotsMenuHolder(plots, filter, page.index());
        Component title = this.messages.parse(settings.guiTitle()).append(this.messages.parse(" <dark_gray>\u2022 " + (page.index() + 1) + "/" + page.totalPages()));
        Inventory inventory = Bukkit.createInventory((InventoryHolder)holder, (int)54, (Component)title);
        holder.attach(inventory);
        GuiIcons icons = settings.icons();
        this.fill(inventory, icons.get(GuiIcon.FILLER));
        for (int i = 0; i < Math.min(page.entries().size(), PLOT_SLOTS.length); ++i) {
            PlotData plot = page.entries().get(i);
            inventory.setItem(PLOT_SLOTS[i], this.plotItem(plot, lastUsed.filter(plot.key()::equals).isPresent(), icons));
        }
        if (page.isEmpty()) {
            inventory.setItem(22, this.item(icons.get(GuiIcon.EMPTY_STATE), "<yellow><bold>Brak dzia\u0142ek</bold>", List.of(filter == PlotFilter.ALL ? "<gray>Nie masz jeszcze \u017cadnej dost\u0119pnej dzia\u0142ki." : "<gray>Brak dzia\u0142ek w tym filtrze.", "<dark_gray>Postaw ProtectionStone lub zmie\u0144 filtr.")));
        }
        if (page.hasPrevious()) {
            inventory.setItem(45, this.action(icons.get(GuiIcon.PREVIOUS_PAGE), "<green>Poprzednia strona", MenuAction.PREVIOUS));
        }
        inventory.setItem(47, this.action(icons.get(GuiIcon.FILTER), "<aqua>Filtr: <white>" + this.filterName(filter), MenuAction.FILTER));
        inventory.setItem(49, this.item(icons.get(GuiIcon.SUMMARY), "<gold><bold>Podsumowanie</bold>", List.of("<gray>Widoczne: <white>" + page.totalEntries(), "<gray>Wszystkie dost\u0119pne: <white>" + plots.size(), "<dark_gray>LPM na dzia\u0142k\u0119 = teleport")));
        inventory.setItem(51, this.action(icons.get(GuiIcon.REFRESH), "<yellow>Od\u015bwie\u017c list\u0119", MenuAction.REFRESH));
        if (page.hasNext()) {
            inventory.setItem(53, this.action(icons.get(GuiIcon.NEXT_PAGE), "<green>Nast\u0119pna strona", MenuAction.NEXT));
        }
        return inventory;
    }

    public Optional<PlotKey> readPlot(ItemStack item) {
        return this.readPlotTarget(item).map(PlotTeleportTarget::key);
    }

    public Optional<PlotTeleportTarget> readPlotTarget(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return Optional.empty();
        }
        String world = (String)item.getItemMeta().getPersistentDataContainer().get(this.plotWorldKey, PersistentDataType.STRING);
        String region = (String)item.getItemMeta().getPersistentDataContainer().get(this.plotRegionKey, PersistentDataType.STRING);
        if (world == null || region == null) {
            return Optional.empty();
        }
        try {
            String name = (String)item.getItemMeta().getPersistentDataContainer().get(this.plotNameKey, PersistentDataType.STRING);
            return Optional.of(new PlotTeleportTarget(new PlotKey(UUID.fromString(world), region), name));
        }
        catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public Optional<MenuAction> readAction(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return Optional.empty();
        }
        String raw = (String)item.getItemMeta().getPersistentDataContainer().get(this.actionKey, PersistentDataType.STRING);
        if (raw == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(MenuAction.valueOf(raw));
        }
        catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private ItemStack plotItem(PlotData plot, boolean lastUsed, GuiIcons icons) {
        String relation = plot.relation() == PlotRelation.OWNED ? "<green>W\u0142asna" : "<aqua>Wsp\u00f3\u0142dzielona";
        String owners = plot.ownerNames().isEmpty() ? "nieznany" : String.join((CharSequence)", ", plot.ownerNames());
        ArrayList<String> lore = new ArrayList<String>(List.of("<dark_gray>\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501", "<gray>Status: " + relation, "<gray>W\u0142a\u015bciciel: <white>" + this.escape(owners), "<gray>\u015awiat: <white>" + this.escape(plot.worldName()), "<gray>Home: <white>" + plot.homeX() + ", " + plot.homeY() + ", " + plot.homeZ(), "<gray>Cz\u0142onkowie: <white>" + plot.memberCount(), ""));
        if (lastUsed) {
            lore.add("<gold>\u2605 Ostatnio u\u017cywana");
        }
        lore.add("<yellow>\u25b6 Kliknij, aby si\u0119 teleportowa\u0107");
        ItemStack item = this.item(icons.forPlot(plot.relation()), (plot.relation() == PlotRelation.OWNED ? "<green>" : "<aqua>") + "<bold>" + this.escape(plot.displayName()) + "</bold>", lore);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(this.plotWorldKey, PersistentDataType.STRING, plot.key().worldId().toString());
        meta.getPersistentDataContainer().set(this.plotRegionKey, PersistentDataType.STRING, plot.key().regionId());
        if (plot.regionName() != null && !plot.regionName().isBlank()) {
            meta.getPersistentDataContainer().set(this.plotNameKey, PersistentDataType.STRING, plot.regionName());
        }
        if (lastUsed) {
            meta.setEnchantmentGlintOverride(Boolean.valueOf(true));
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack action(ConfiguredIcon icon, String name, MenuAction action) {
        ItemStack item = this.item(icon, name, List.of());
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(this.actionKey, PersistentDataType.STRING, action.name());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack item(ConfiguredIcon icon, String name, List<String> lore) {
        ItemStack item = new ItemStack(icon.material());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(this.messages.parse("<italic:false>" + name));
        meta.lore(lore.stream().map(line -> this.messages.parse("<italic:false>" + line)).toList());
        icon.applyTo(meta);
        item.setItemMeta(meta);
        return item;
    }

    private void fill(Inventory inventory, ConfiguredIcon icon) {
        ItemStack filler = this.item(icon, " ", List.of());
        for (int slot = 0; slot < inventory.getSize(); ++slot) {
            inventory.setItem(slot, filler);
        }
    }

    private String filterName(PlotFilter filter) {
        return switch (filter) {
            default -> throw new MatchException(null, null);
            case PlotFilter.ALL -> "Wszystkie";
            case PlotFilter.OWNED -> "W\u0142asne";
            case PlotFilter.SHARED -> "Wsp\u00f3\u0142dzielone";
        };
    }

    private String escape(String input) {
        return MiniMessage.miniMessage().escapeTags(input);
    }
}
