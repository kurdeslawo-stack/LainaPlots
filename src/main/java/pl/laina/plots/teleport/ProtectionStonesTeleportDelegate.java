package pl.laina.plots.teleport;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import org.bukkit.entity.Player;
import pl.laina.plots.model.PlotKey;
import pl.laina.plots.model.PlotTeleportTarget;

public final class ProtectionStonesTeleportDelegate {
    private final ProtectionStonesHomeCommand homeCommand;
    private final BiConsumer<Player, String> messageSender;
    private final BiConsumer<Player, PlotKey> usageRecorder;
    private final BiPredicate<Player, PlotTeleportTarget> crossWorldResolver;

    public ProtectionStonesTeleportDelegate(String baseCommand, BiConsumer<Player, String> messageSender, BiConsumer<Player, PlotKey> usageRecorder, BiPredicate<Player, PlotTeleportTarget> crossWorldResolver) {
        this.homeCommand = new ProtectionStonesHomeCommand(baseCommand);
        this.messageSender = Objects.requireNonNull(messageSender, "messageSender");
        this.usageRecorder = Objects.requireNonNull(usageRecorder, "usageRecorder");
        this.crossWorldResolver = Objects.requireNonNull(crossWorldResolver, "crossWorldResolver");
    }

    public void request(Player player, PlotTeleportTarget target) {
        player.closeInventory();
        ProtectionStonesHomeCommand.Plan plan = this.homeCommand.plan(player.getWorld().getUID(), target);
        if (plan.status() == ProtectionStonesHomeCommand.Status.CROSS_WORLD_UNNAMED) {
            this.messageSender.accept(player, "cross-world-unavailable");
            return;
        }
        if (plan.status() == ProtectionStonesHomeCommand.Status.INVALID_IDENTIFIER) {
            this.messageSender.accept(player, "invalid-region-identifier");
            return;
        }
        if (!player.getWorld().getUID().equals(target.key().worldId()) && !this.crossWorldResolver.test(player, target)) {
            this.messageSender.accept(player, "cross-world-resolution-failed");
            return;
        }
        if (!player.performCommand(plan.command())) {
            this.messageSender.accept(player, "protectionstones-command-unavailable");
            return;
        }
        this.usageRecorder.accept(player, target.key());
    }
}
