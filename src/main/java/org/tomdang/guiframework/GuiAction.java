package org.tomdang.guiframework;

@FunctionalInterface
public interface GuiAction {
    GuiActionResult execute(GuiActionContext context);
}
