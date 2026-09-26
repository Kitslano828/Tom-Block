package org.tomdang.guiframework;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GuiPaginationTest {
    @Test
    void slicesAndClampsPages() {
        GuiPagination<Integer> second = GuiPagination.of(List.of(1, 2, 3, 4, 5), 1, 2);
        assertEquals(List.of(3, 4), second.entries());
        assertTrue(second.hasPrevious());
        assertTrue(second.hasNext());
        assertEquals(3, second.pageCount());

        GuiPagination<Integer> clamped = GuiPagination.of(List.of(1, 2, 3), 99, 2);
        assertEquals(1, clamped.page());
        assertEquals(List.of(3), clamped.entries());
    }

    @Test
    void emptyCollectionsStillHaveAStableFirstPage() {
        GuiPagination<Object> page = GuiPagination.of(List.of(), 4, 21);
        assertEquals(0, page.page());
        assertEquals(1, page.pageCount());
        assertTrue(page.entries().isEmpty());
    }
}
