/*
 * Decompiled with CFR 0.152.
 */
package pl.laina.plots.core;

import java.util.Optional;
import java.util.UUID;
import pl.laina.plots.model.AccessResult;
import pl.laina.plots.model.PlotSource;

public final class PlotAccessPolicy {
    public AccessResult validate(Optional<PlotSource> current, UUID viewer) {
        if (current.isEmpty()) {
            return AccessResult.MISSING;
        }
        PlotSource plot = current.get();
        if (!plot.owners().contains(viewer) && !plot.members().contains(viewer)) {
            return AccessResult.DENIED;
        }
        if (!plot.validHome()) {
            return AccessResult.INVALID_HOME;
        }
        return AccessResult.ALLOWED;
    }
}

