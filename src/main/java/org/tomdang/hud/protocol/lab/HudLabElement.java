package org.tomdang.hud.protocol.lab;

import org.tomdang.hud.composition.*;
import org.tomdang.hud.composition.draw.*;
import org.tomdang.hud.composition.layout.*;
import java.util.List;

/** Static Phase 2 scene. It is deliberately unrelated to gameplay state. */
public final class HudLabElement implements HudElement {
	public static final HudElementId ID = HudElementId.of("tomblock-lab", "protocol-scene");
	@Override public HudElementId id() { return ID; }
	@Override public HudRegion region() { return HudRegion.DEBUG; }
	@Override public int priority() { return 10_000; }
	@Override public HudContent render(HudRenderContext context) {
		HudNode title = new HudPrimitive(new HudTextCommand("TOMBLOCK HUD 0.1",
				new HudTextStyle("lab-title", 0xFFFFFF, true, false), context.availableWidth(), false));
		HudNode description = new HudPrimitive(new HudTextCommand("GUI-scaled top-right anchor",
				new HudTextStyle("lab-body", 0xFFFFFF, false, false), context.availableWidth(), true));
		HudNode icon = new HudPrimitive(new HudImageCommand("lab-icon", 16, 8));
		HudNode bar = new HudPrimitive(new HudProgressBarCommand("lab-progress", 7, 10, 170, 8));
		HudNode row = new HudStack(HudAxis.HORIZONTAL, 4, HudAlignment.CENTER, List.of(icon, description));
		return new HudContent(HudStack.vertical(3, HudAlignment.END, List.of(title, row, bar)));
	}
}
