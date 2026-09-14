package pl.laina.plots.teleport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import pl.laina.plots.model.PlotKey;
import pl.laina.plots.model.PlotTeleportTarget;

class ProtectionStonesTeleportDelegateTest {
    @Test
    void delegatesOwnerAndMemberClicksAsPlayerCommandsWithoutDirectTeleport() {
        UUID worldId = UUID.randomUUID();
        ArrayList<String> invokedMethods = new ArrayList<>();
        ArrayList<String> commands = new ArrayList<>();
        World world = proxy(World.class, (proxy, method, args) -> method.getName().equals("getUID") ? worldId : defaultValue(method.getReturnType()));
        Player player = proxy(Player.class, (proxy, method, args) -> {
            invokedMethods.add(method.getName());
            return switch (method.getName()) {
                case "getWorld" -> world;
                case "performCommand" -> {
                    commands.add((String)args[0]);
                    yield true;
                }
                default -> defaultValue(method.getReturnType());
            };
        });
        ArrayList<PlotKey> used = new ArrayList<>();
        ProtectionStonesTeleportDelegate delegate = new ProtectionStonesTeleportDelegate("ps", (p, key) -> {}, (p, key) -> used.add(key), (p, target) -> true);
        PlotTeleportTarget owner = new PlotTeleportTarget(new PlotKey(worldId, "owner-id"), null);
        PlotTeleportTarget member = new PlotTeleportTarget(new PlotKey(worldId, "member-id"), null);

        delegate.request(player, owner);
        delegate.request(player, member);

        assertEquals(List.of("ps home owner-id", "ps home member-id"), commands);
        assertEquals(List.of(owner.key(), member.key()), used);
        assertTrue(invokedMethods.contains("performCommand"));
        assertFalse(invokedMethods.contains("teleport"));
        assertFalse(invokedMethods.contains("teleportAsync"));
    }

    @Test
    void unnamedCrossWorldClickDoesNotDispatchOrTeleport() {
        UUID currentWorld = UUID.randomUUID();
        ArrayList<String> invokedMethods = new ArrayList<>();
        World world = proxy(World.class, (proxy, method, args) -> method.getName().equals("getUID") ? currentWorld : defaultValue(method.getReturnType()));
        Player player = proxy(Player.class, (proxy, method, args) -> {
            invokedMethods.add(method.getName());
            return method.getName().equals("getWorld") ? world : defaultValue(method.getReturnType());
        });
        ArrayList<String> messages = new ArrayList<>();
        ProtectionStonesTeleportDelegate delegate = new ProtectionStonesTeleportDelegate("ps", (p, key) -> messages.add(key), (p, key) -> {}, (p, target) -> true);

        delegate.request(player, new PlotTeleportTarget(new PlotKey(UUID.randomUUID(), "remote"), null));

        assertEquals(List.of("cross-world-unavailable"), messages);
        assertFalse(invokedMethods.contains("performCommand"));
        assertFalse(invokedMethods.contains("teleport"));
        assertFalse(invokedMethods.contains("teleportAsync"));
    }

    @Test
    void crossWorldNameCollisionCannotDispatchToWrongRegion() {
        UUID currentWorld = UUID.randomUUID();
        ArrayList<String> invokedMethods = new ArrayList<>();
        World world = proxy(World.class, (proxy, method, args) -> method.getName().equals("getUID") ? currentWorld : defaultValue(method.getReturnType()));
        Player player = proxy(Player.class, (proxy, method, args) -> {
            invokedMethods.add(method.getName());
            return method.getName().equals("getWorld") ? world : defaultValue(method.getReturnType());
        });
        ArrayList<String> messages = new ArrayList<>();
        ProtectionStonesTeleportDelegate delegate = new ProtectionStonesTeleportDelegate("ps", (p, key) -> messages.add(key), (p, key) -> {}, (p, target) -> false);

        delegate.request(player, new PlotTeleportTarget(new PlotKey(UUID.randomUUID(), "remote"), "local-id-collision"));

        assertEquals(List.of("cross-world-resolution-failed"), messages);
        assertFalse(invokedMethods.contains("performCommand"));
        assertFalse(invokedMethods.contains("teleport"));
        assertFalse(invokedMethods.contains("teleportAsync"));
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return (T)Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte)0;
        if (type == short.class) return (short)0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        return 0.0d;
    }
}
