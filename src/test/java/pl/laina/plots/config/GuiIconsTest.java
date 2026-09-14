package pl.laina.plots.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.junit.jupiter.api.Test;
import pl.laina.plots.model.PlotRelation;

class GuiIconsTest {
    @Test
    void loadsConfiguredMaterialAndCustomModelData() throws Exception {
        GuiIcons icons = load("gui:\n  icons:\n    filter:\n      material: DIAMOND\n      custom-model-data: 1234\n", new ArrayList<>());

        assertEquals(Material.DIAMOND, icons.get(GuiIcon.FILTER).material());
        assertEquals(1234, icons.get(GuiIcon.FILTER).customModelData());
    }

    @Test
    void zeroCustomModelDataDoesNotTouchItemMeta() throws Exception {
        GuiIcons icons = load("gui:\n  icons:\n    filter:\n      material: HOPPER\n      custom-model-data: 0\n", new ArrayList<>());
        AtomicInteger invocations = new AtomicInteger();
        ItemMeta meta = proxy(ItemMeta.class, (proxy, method, args) -> {
            invocations.incrementAndGet();
            return null;
        });

        icons.get(GuiIcon.FILTER).applyTo(meta);

        assertEquals(0, invocations.get());
    }

    @Test
    void missingCustomModelDataDoesNotSetIt() throws Exception {
        GuiIcons icons = load("gui:\n  icons:\n    summary:\n      material: BOOK\n", new ArrayList<>());

        assertEquals(0, icons.get(GuiIcon.SUMMARY).customModelData());
    }

    @Test
    void positiveCustomModelDataUsesPaperComponentFloats() throws Exception {
        ConfiguredIcon icon = new ConfiguredIcon(Material.BOOK, 77);
        AtomicReference<List<Float>> floats = new AtomicReference<>();
        CustomModelDataComponent component = proxy(CustomModelDataComponent.class, (proxy, method, args) -> {
            if (method.getName().equals("setFloats")) {
                floats.set((List<Float>)args[0]);
            }
            return null;
        });
        AtomicReference<Object> applied = new AtomicReference<>();
        ItemMeta meta = proxy(ItemMeta.class, (proxy, method, args) -> switch (method.getName()) {
            case "getCustomModelDataComponent" -> component;
            case "setCustomModelDataComponent" -> {
                applied.set(args[0]);
                yield null;
            }
            default -> null;
        });

        icon.applyTo(meta);

        assertEquals(List.of(77.0f), floats.get());
        assertSame(component, applied.get());
    }

    @Test
    void invalidMaterialWarnsAndUsesPerIconFallback() throws Exception {
        ArrayList<String> warnings = new ArrayList<>();
        GuiIcons icons = load("gui:\n  icons:\n    refresh:\n      material: DEFINITELY_NOT_A_MATERIAL\n", warnings);

        assertEquals(Material.SUNFLOWER, icons.get(GuiIcon.REFRESH).material());
        assertEquals(1, warnings.size());
        assertTrue(warnings.getFirst().contains("gui.icons.refresh.material"));
    }

    @Test
    void missingIconsSectionUsesAllFallbacks() {
        GuiIcons icons = GuiIcons.load(null, message -> {});

        for (GuiIcon icon : GuiIcon.values()) {
            assertEquals(icon.fallbackMaterial(), icons.get(icon).material());
            assertEquals(0, icons.get(icon).customModelData());
        }
    }

    @Test
    void ownerAndMemberUseTheirOwnConfiguredIcons() throws Exception {
        GuiIcons icons = load("gui:\n  icons:\n    owner-plot:\n      material: DIAMOND_BLOCK\n    member-plot:\n      material: EMERALD_BLOCK\n", new ArrayList<>());

        assertEquals(Material.DIAMOND_BLOCK, icons.forPlot(PlotRelation.OWNED).material());
        assertEquals(Material.EMERALD_BLOCK, icons.forPlot(PlotRelation.SHARED).material());
    }

    private static GuiIcons load(String yaml, List<String> warnings) throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(yaml);
        return GuiIcons.load(config.getConfigurationSection("gui.icons"), warnings::add, material -> true);
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return (T)Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }
}
