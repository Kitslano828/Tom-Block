package org.tomdang.hud.composition;

import org.junit.jupiter.api.Test;
import org.tomdang.hud.composition.draw.*;
import org.tomdang.hud.composition.layout.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HudLayoutEngineTest {
	private final HudLayoutEngine layouts = new HudLayoutEngine(text -> {
		int raw = text.text().length() * 5;
		int lines = text.wrap() && text.maxWidth() > 0 ? Math.max(1, (raw + text.maxWidth() - 1) / text.maxWidth()) : 1;
		return new HudSize(text.maxWidth() > 0 ? Math.min(raw, text.maxWidth()) : raw, lines * 9);
	});

	@Test void verticalStackUsesGapAndEndAlignment() {
		HudNode tree = HudStack.vertical(3, HudAlignment.END, List.of(
				new HudPrimitive(HudTextCommand.text("123456")),
				new HudPrimitive(HudTextCommand.text("12"))));
		HudElementSnapshot owner = snapshot(tree);
		var commands = layouts.layout(owner, new HudPoint(20, 10), 1);
		assertEquals(new HudRect(20, 10, 30, 9), commands.get(0).bounds());
		assertEquals(new HudRect(40, 22, 10, 9), commands.get(1).bounds());
	}

	@Test void wrappingChangesMeasuredHeightWithoutEscapingMaximumWidth() {
		HudTextCommand text = new HudTextCommand("123456789012", HudTextStyle.DEFAULT, 20, true);
		HudSize size = layouts.measure(new HudPrimitive(text));
		assertEquals(20, size.width());
		assertEquals(27, size.height());
	}

	@Test void representativeRegionsDoNotOverlapOnDefaultViewport() {
		HudViewport viewport = HudViewport.DEFAULT;
		HudLayoutPolicy policy = new HudLayoutPolicy(java.util.Map.of());
		HudRect quest = rectangle(layouts.anchor(viewport, policy.region(HudRegion.QUEST_TRACKER), new HudSize(150, 54)), 150, 54);
		HudRect status = rectangle(layouts.anchor(viewport, policy.region(HudRegion.STATUS), new HudSize(220, 20)), 220, 20);
		HudRect dialogue = rectangle(layouts.anchor(viewport, policy.region(HudRegion.DIALOGUE), new HudSize(240, 50)), 240, 50);
		assertFalse(quest.intersects(status));
		assertFalse(quest.intersects(dialogue));
	}

	private HudElementSnapshot snapshot(HudNode node) {
		return new HudElementSnapshot(HudElementId.of("test", "layout"), HudRegion.NOTIFICATION,
				1, HudPresentationMode.FULL, new HudContent(node), new HudRect(0, 0, 30, 31));
	}
	private HudRect rectangle(HudPoint point, int width, int height) { return new HudRect(point.x(), point.y(), width, height); }
}
