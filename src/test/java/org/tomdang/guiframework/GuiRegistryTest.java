package org.tomdang.guiframework;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GuiRegistryTest {
    @Test
    void registersScreensCaseInsensitivelyAndRejectsDuplicates() {
        GuiRegistry registry = new GuiRegistry();
        registry.register(screen("critterdex", 54));

        assertEquals("critterdex", registry.require("CRITTERDEX").id());
        assertThrows(IllegalArgumentException.class, () -> registry.register(screen("CRITTERDEX", 54)));
    }

    @Test
    void sealedRegistryRejectsLateRegistration() {
        GuiRegistry registry = new GuiRegistry();
        registry.seal();
        assertThrows(IllegalStateException.class, () -> registry.register(screen("late", 9)));
    }

    @Test
    void rejectsInvalidInventorySizes() {
        GuiRegistry registry = new GuiRegistry();
        assertThrows(IllegalArgumentException.class, () -> registry.register(screen("bad", 10)));
    }

    private GuiScreen screen(String id, int size) {
        return new GuiScreen() {
            @Override public String id() { return id; }
            @Override public int size() { return size; }
            @Override public Component title(GuiRenderContext context) { return Component.text("Test"); }
            @Override public void render(GuiRenderContext context, GuiCanvas canvas) { }
        };
    }
}
