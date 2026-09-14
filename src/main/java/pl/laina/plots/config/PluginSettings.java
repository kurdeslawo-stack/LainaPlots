/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.NamespacedKey
 *  org.bukkit.Registry
 *  org.bukkit.Sound
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.plugin.java.JavaPlugin
 */
package pl.laina.plots.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public record PluginSettings(String guiTitle, int plotsPerPage, GuiIcons icons) {
    public static PluginSettings load(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        int perPage = Math.max(1, Math.min(28, config.getInt("plots-per-page", 28)));
        GuiIcons icons = GuiIcons.load(config.getConfigurationSection("gui.icons"), plugin.getLogger()::warning);
        return new PluginSettings(config.getString("gui-title", "<green><bold>Twoje działki</bold>"), perPage, icons);
    }
}
