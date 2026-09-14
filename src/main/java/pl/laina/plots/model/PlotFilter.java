/*
 * Decompiled with CFR 0.152.
 */
package pl.laina.plots.model;

public enum PlotFilter {
    ALL,
    OWNED,
    SHARED;


    public PlotFilter next() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> OWNED;
            case 1 -> SHARED;
            case 2 -> ALL;
        };
    }
}

