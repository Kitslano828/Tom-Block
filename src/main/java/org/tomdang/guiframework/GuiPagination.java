package org.tomdang.guiframework;

import java.util.List;

/** Immutable, clamped page calculation shared by collection-style menus. */
public record GuiPagination<T>(List<T> entries, int page, int pageCount, int totalEntries) {
    public GuiPagination {
        entries = List.copyOf(entries);
    }

    public static <T> GuiPagination<T> of(List<T> source, int requestedPage, int pageSize) {
        if (source == null) throw new IllegalArgumentException("Pagination source is required");
        if (pageSize < 1) throw new IllegalArgumentException("Page size must be positive");
        int pageCount = Math.max(1, (source.size() + pageSize - 1) / pageSize);
        int page = Math.max(0, Math.min(requestedPage, pageCount - 1));
        int from = Math.min(page * pageSize, source.size());
        int to = Math.min(from + pageSize, source.size());
        return new GuiPagination<>(source.subList(from, to), page, pageCount, source.size());
    }

    public boolean hasPrevious() { return page > 0; }
    public boolean hasNext() { return page + 1 < pageCount; }
}
