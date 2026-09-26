package org.tomdang.guiframework;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class GuiLayoutLoaderTest {
    @Test
    void loadsNamedSlots() {
        GuiLayout layout = load("""
                menu:
                  title: Critterdex
                  size: 54
                  slots:
                    previous: 45
                    close: 49
                    next: 53
                """);

        assertEquals("Critterdex", layout.title());
        assertEquals(54, layout.size());
        assertEquals(49, layout.slot("close"));
    }

    @Test
    void rejectsDuplicateOrOutOfRangeSlots() {
        assertThrows(IllegalArgumentException.class, () -> load("""
                menu:
                  title: Bad
                  size: 9
                  slots:
                    one: 2
                    two: 2
                """));
        assertThrows(IllegalArgumentException.class, () -> load("""
                menu:
                  title: Bad
                  size: 9
                  slots:
                    outside: 9
                """));
    }

    private GuiLayout load(String yaml) {
        return new GuiLayoutLoader().load(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    }
}
