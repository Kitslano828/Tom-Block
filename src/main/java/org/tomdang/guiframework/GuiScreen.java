package org.tomdang.guiframework;

import net.kyori.adventure.text.Component;

public interface GuiScreen {
    String id();
    int size();
    Component title(GuiRenderContext context);
    void render(GuiRenderContext context, GuiCanvas canvas);
}
