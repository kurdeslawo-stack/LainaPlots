/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.minimessage.MiniMessage
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.java.JavaPlugin
 */
package pl.laina.plots.message;

import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Messages {
    private final JavaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Component component(String key) {
        return this.component(key, Map.of());
    }

    public Component component(String key, Map<String, String> placeholders) {
        FileConfiguration config = this.plugin.getConfig();
        String raw = config.getString("messages." + key, "<red>Brak wiadomo\u015bci: " + key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            raw = raw.replace("<" + entry.getKey() + ">", MiniMessage.miniMessage().escapeTags(entry.getValue()));
        }
        return this.miniMessage.deserialize(raw);
    }

    public Component parse(String text) {
        return this.miniMessage.deserialize(text);
    }

    public void send(Player player, String key) {
        player.sendMessage(this.component(key));
    }

    public void send(Player player, String key, Map<String, String> placeholders) {
        player.sendMessage(this.component(key, placeholders));
    }
}
