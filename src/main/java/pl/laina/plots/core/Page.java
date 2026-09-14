/*
 * Decompiled with CFR 0.152.
 */
package pl.laina.plots.core;

import java.util.List;

public record Page<T>(List<T> entries, int index, int totalPages, int totalEntries) {
    public Page {
        entries = List.copyOf(entries);
    }

    public boolean isEmpty() {
        return this.totalEntries == 0;
    }

    public boolean hasPrevious() {
        return this.index > 0;
    }

    public boolean hasNext() {
        return this.index + 1 < this.totalPages;
    }
}

