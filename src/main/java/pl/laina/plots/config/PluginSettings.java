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

import java.util.Locale;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public record PluginSettings(String guiTitle, int plotsPerPage, int teleportDelaySeconds, Sound teleportSound) {
    public static PluginSettings load(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        int perPage = Math.max(1, Math.min(28, config.getInt("plots-per-page", 28)));
        int delay = Math.max(0, Math.min(60, config.getInt("teleport-delay-seconds", 0)));
        Sound sound = PluginSettings.parseSound(plugin, config.getString("teleport-sound", "ENTITY_ENDERMAN_TELEPORT"));
        return new PluginSettings(config.getString("gui-title", "<green><bold>Twoje dzia\u0142ki</bold>"), perPage, delay, sound);
    }

    private static Sound parseSound(JavaPlugin plugin, String configured) {
        NamespacedKey key;
        Sound sound;
        Object normalized = configured.toLowerCase(Locale.ROOT);
        if (!((String)normalized).contains(":")) {
            normalized = "minecraft:" + (String)(((String)normalized).contains(".") ? normalized : ((String)normalized).replace('_', '.'));
        }
        Sound sound2 = sound = (key = NamespacedKey.fromString((String)normalized)) == null ? null : (Sound)Registry.SOUND_EVENT.get(key);
        if (sound != null) {
            return sound;
        }
        plugin.getLogger().warning("Nieznany d\u017awi\u0119k '" + configured + "'. U\u017cywam ENTITY_ENDERMAN_TELEPORT.");
        return Sound.ENTITY_ENDERMAN_TELEPORT;
    }
}

