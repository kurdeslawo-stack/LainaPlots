package pl.laina.plots.teleport;

import java.util.Objects;
import java.util.UUID;
import pl.laina.plots.model.PlotTeleportTarget;

public final class ProtectionStonesHomeCommand {
    private final String baseCommand;

    public ProtectionStonesHomeCommand(String baseCommand) {
        if (!isSafeArgument(baseCommand)) {
            throw new IllegalArgumentException("Invalid ProtectionStones base command");
        }
        this.baseCommand = baseCommand;
    }

    public Plan plan(UUID currentWorldId, PlotTeleportTarget target) {
        Objects.requireNonNull(currentWorldId, "currentWorldId");
        Objects.requireNonNull(target, "target");
        String identifier;
        if (currentWorldId.equals(target.key().worldId())) {
            identifier = target.key().regionId();
        } else if (target.regionName() != null) {
            identifier = target.regionName();
        } else {
            return new Plan(Status.CROSS_WORLD_UNNAMED, null);
        }
        if (!isSafeArgument(identifier)) {
            return new Plan(Status.INVALID_IDENTIFIER, null);
        }
        return new Plan(Status.READY, this.baseCommand + " home " + identifier);
    }

    private static boolean isSafeArgument(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (int i = 0; i < value.length(); ++i) {
            if (Character.isWhitespace(value.charAt(i)) || Character.isISOControl(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public enum Status {
        READY,
        CROSS_WORLD_UNNAMED,
        INVALID_IDENTIFIER
    }

    public record Plan(Status status, String command) {
    }
}
