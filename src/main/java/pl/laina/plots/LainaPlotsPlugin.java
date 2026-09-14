/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.espi.protectionstones.ProtectionStones
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.PluginCommand
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.event.Listener
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 */
package pl.laina.plots;

import dev.espi.protectionstones.ProtectionStones;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import pl.laina.plots.command.PlotsCommand;
import pl.laina.plots.config.PluginSettings;
import pl.laina.plots.core.PlotAccessPolicy;
import pl.laina.plots.core.PlotCatalogue;
import pl.laina.plots.core.PlotMapper;
import pl.laina.plots.gui.GuiListener;
import pl.laina.plots.gui.PlotsMenuController;
import pl.laina.plots.gui.PlotsMenuRenderer;
import pl.laina.plots.message.Messages;
import pl.laina.plots.service.ProtectionStonesPlotGateway;
import pl.laina.plots.teleport.TeleportCoordinator;

public final class LainaPlotsPlugin
extends JavaPlugin {
    private PluginSettings settings;
    private TeleportCoordinator teleports;

    public void onEnable() {
        ProtectionStones protectionStones;
        block5: {
            block4: {
                this.saveDefaultConfig();
                this.reloadSettings();
                Plugin dependency = this.getServer().getPluginManager().getPlugin("ProtectionStones");
                if (!(dependency instanceof ProtectionStones)) break block4;
                protectionStones = (ProtectionStones)dependency;
                if (dependency.isEnabled()) break block5;
            }
            this.getLogger().severe("ProtectionStones nie jest zainstalowany lub aktywny. Wy\u0142\u0105czam LainaPlots.");
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
            return;
        }
        Messages messages = new Messages(this);
        ProtectionStonesPlotGateway gateway = new ProtectionStonesPlotGateway(this, protectionStones);
        PlotMapper mapper = new PlotMapper();
        PlotCatalogue catalogue = new PlotCatalogue();
        PlotsMenuRenderer renderer = new PlotsMenuRenderer(this, messages, catalogue);
        PlotsMenuController menus = new PlotsMenuController(this, gateway, mapper, renderer, messages);
        this.teleports = new TeleportCoordinator(this, gateway, new PlotAccessPolicy(), menus, messages);
        PluginCommand command = this.getCommand("dzialki");
        if (command == null) {
            this.getLogger().severe("Brakuje komendy dzialki w plugin.yml. Wy\u0142\u0105czam plugin.");
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
            return;
        }
        PlotsCommand executor = new PlotsCommand(this, menus, messages);
        command.setExecutor((CommandExecutor)executor);
        command.setTabCompleter((TabCompleter)executor);
        this.getServer().getPluginManager().registerEvents((Listener)new GuiListener(menus, this.teleports), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)this.teleports, (Plugin)this);
        this.getLogger().info("LainaPlots w\u0142\u0105czony. GUI: /dzialki, alias: /plots.");
    }

    public void onDisable() {
        if (this.teleports != null) {
            this.teleports.shutdown();
        }
    }

    public void reloadSettings() {
        this.reloadConfig();
        this.settings = PluginSettings.load(this);
    }

    public PluginSettings settings() {
        return this.settings;
    }
}

