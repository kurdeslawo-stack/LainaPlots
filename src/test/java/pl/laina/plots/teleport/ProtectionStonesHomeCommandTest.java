package pl.laina.plots.teleport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import pl.laina.plots.model.PlotKey;
import pl.laina.plots.model.PlotTeleportTarget;

class ProtectionStonesHomeCommandTest {
    private final ProtectionStonesHomeCommand commands = new ProtectionStonesHomeCommand("ps");
    private final UUID currentWorld = UUID.randomUUID();

    @Test
    void sameWorldUsesExactRegionId() {
        PlotTeleportTarget target = new PlotTeleportTarget(new PlotKey(this.currentWorld, "ps123_45_67"), "FriendlyName");

        ProtectionStonesHomeCommand.Plan plan = this.commands.plan(this.currentWorld, target);

        assertEquals(ProtectionStonesHomeCommand.Status.READY, plan.status());
        assertEquals("ps home ps123_45_67", plan.command());
    }

    @Test
    void crossWorldUsesProtectionStonesGlobalRegionName() {
        PlotTeleportTarget target = new PlotTeleportTarget(new PlotKey(UUID.randomUUID(), "remote-id"), "remote_home");

        ProtectionStonesHomeCommand.Plan plan = this.commands.plan(this.currentWorld, target);

        assertEquals(ProtectionStonesHomeCommand.Status.READY, plan.status());
        assertEquals("ps home remote_home", plan.command());
    }

    @Test
    void crossWorldWithoutNameIsRejectedSafely() {
        PlotTeleportTarget target = new PlotTeleportTarget(new PlotKey(UUID.randomUUID(), "remote-id"), null);

        ProtectionStonesHomeCommand.Plan plan = this.commands.plan(this.currentWorld, target);

        assertEquals(ProtectionStonesHomeCommand.Status.CROSS_WORLD_UNNAMED, plan.status());
        assertNull(plan.command());
    }

    @Test
    void unsafeIdentifierCannotInjectAnotherCommandArgument() {
        PlotTeleportTarget target = new PlotTeleportTarget(new PlotKey(this.currentWorld, "id with spaces"), null);

        assertEquals(ProtectionStonesHomeCommand.Status.INVALID_IDENTIFIER, this.commands.plan(this.currentWorld, target).status());
    }
}
