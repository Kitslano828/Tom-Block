package org.tomdang.hud.composition;

import org.junit.jupiter.api.Test;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.composition.layout.HudPrimitive;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HudRuntimeTest {
	@Test void reconcilesShowHideAndPlayerCloseThroughTransport() {
		RecordingTransport transport = new RecordingTransport();
		HudRuntime runtime = new HudRuntime(new PlayerHudSessionRegistry(),
				new HudCompositor(new HudLayoutPolicy(Map.of())), List.of(transport));
		UUID player = UUID.randomUUID();
		HudElementId id = HudElementId.of("test", "notice");
		HudElement element = new HudElement() {
			public HudElementId id() { return id; }
			public HudRegion region() { return HudRegion.NOTIFICATION; }
			public int priority() { return 1; }
			public HudContent render(HudRenderContext context) { return new HudContent(new HudPrimitive(HudTextCommand.text("Notice"))); }
		};

		runtime.show(player, element, 1);
		assertEquals(1, transport.frames.getLast().region(HudRegion.NOTIFICATION).size());
		runtime.hide(player, id, 2);
		assertTrue(transport.frames.getLast().region(HudRegion.NOTIFICATION).isEmpty());
		runtime.close(player);
		assertEquals(List.of(player), transport.cleared);
	}

	@Test void rejectsTwoTransportsOwningTheSameRegion() {
		RecordingTransport first = new RecordingTransport();
		RecordingTransport second = new RecordingTransport();
		assertThrows(IllegalArgumentException.class, () -> new HudRuntime(new PlayerHudSessionRegistry(),
				new HudCompositor(new HudLayoutPolicy(Map.of())), List.of(first, second)));
	}

	private static final class RecordingTransport implements HudTransport {
		private final List<HudFrame> frames = new ArrayList<>();
		private final List<UUID> cleared = new ArrayList<>();
		@Override public Set<HudRegion> regions() { return Set.of(HudRegion.NOTIFICATION); }
		@Override public void apply(HudFrame frame) { frames.add(frame); }
		@Override public void clear(UUID playerId) { cleared.add(playerId); }
	}
}
