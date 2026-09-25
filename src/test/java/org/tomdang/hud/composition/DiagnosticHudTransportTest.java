package org.tomdang.hud.composition;

import org.junit.jupiter.api.Test;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.composition.layout.HudPrimitive;
import org.tomdang.hud.composition.transport.DiagnosticHudTransport;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagnosticHudTransportTest {
	@Test void describesSemanticRegionBoundsAndTypedCommands() {
		PlayerHudSession session = new PlayerHudSession(UUID.randomUUID());
		session.put(new HudElement() {
			public HudElementId id() { return HudElementId.of("test", "notice"); }
			public HudRegion region() { return HudRegion.NOTIFICATION; }
			public int priority() { return 1; }
			public HudContent render(HudRenderContext context) {
				return new HudContent(new HudPrimitive(HudTextCommand.text("Hello")));
			}
		});
		HudFrame frame = new HudCompositor(new HudLayoutPolicy(Map.of())).compose(session, 1);
		String description = new DiagnosticHudTransport(ignored -> {}).describe(frame);
		assertTrue(description.contains("NOTIFICATION"));
		assertTrue(description.contains("HudTextCommand"));
		assertTrue(description.contains("viewport=320x180"));
	}
}
