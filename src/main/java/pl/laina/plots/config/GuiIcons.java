package pl.laina.plots.config;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import pl.laina.plots.model.PlotRelation;

public final class GuiIcons {
    private final Map<GuiIcon, ConfiguredIcon> icons;

    private GuiIcons(Map<GuiIcon, ConfiguredIcon> icons) {
        this.icons = Map.copyOf(icons);
    }

    public static GuiIcons load(ConfigurationSection section, Consumer<String> warningLogger) {
        return load(section, warningLogger, Material::isItem);
    }

    static GuiIcons load(ConfigurationSection section, Consumer<String> warningLogger, Predicate<Material> itemValidator) {
        EnumMap<GuiIcon, ConfiguredIcon> loaded = new EnumMap<>(GuiIcon.class);
        for (GuiIcon icon : GuiIcon.values()) {
            loaded.put(icon, loadIcon(section, icon, warningLogger, itemValidator));
        }
        return new GuiIcons(loaded);
    }

    private static ConfiguredIcon loadIcon(ConfigurationSection section, GuiIcon icon, Consumer<String> warningLogger, Predicate<Material> itemValidator) {
        Material material = icon.fallbackMaterial();
        int customModelData = 0;
        if (section != null) {
            String configured = section.getString(icon.configKey() + ".material");
            if (configured != null && !configured.isBlank()) {
                Material matched = Material.matchMaterial(configured);
                if (matched == null || matched == Material.AIR || matched == Material.CAVE_AIR || matched == Material.VOID_AIR || !itemValidator.test(matched)) {
                    warningLogger.accept("Nieprawidłowy material gui.icons." + icon.configKey() + ".material: '" + configured + "'. Używam " + icon.fallbackMaterial() + ".");
                } else {
                    material = matched;
                }
            }
            if (section.contains(icon.configKey() + ".custom-model-data")) {
                customModelData = Math.max(0, section.getInt(icon.configKey() + ".custom-model-data"));
            }
        }
        return new ConfiguredIcon(material, customModelData);
    }

    public ConfiguredIcon get(GuiIcon icon) {
        return this.icons.get(icon);
    }

    public ConfiguredIcon forPlot(PlotRelation relation) {
        return this.get(GuiIcon.forPlot(relation));
    }
}
