package org.tomdang.hud.composition;

import org.junit.jupiter.api.Test;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.composition.layout.HudAlignment;
import org.tomdang.hud.composition.layout.HudAxis;
import org.tomdang.hud.composition.layout.HudPrimitive;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HudCompositorTest {
	@Test void resolvesCapacityPriorityOrderAndCompactModeDeterministically() {
		UUID player = UUID.randomUUID();
		PlayerHudSession session = new PlayerHudSession(player);
		session.put(element("low", 10, true));
		session.put(element("high", 30, false));
		session.put(element("middle", 20, true));
		HudRegionLayout layout = new HudRegionLayout(HudAnchor.TOP_RIGHT, -8, 8, 160, 2,
				HudAxis.VERTICAL, 2, HudAlignment.END);
		HudCompositor compositor = new HudCompositor(new HudLayoutPolicy(Map.of(HudRegion.QUEST_TRACKER, layout)));

		HudFrame frame = compositor.compose(session, 42);

		assertEquals(2, frame.region(HudRegion.QUEST_TRACKER).size());
		assertEquals("high", frame.region(HudRegion.QUEST_TRACKER).get(0).id().value());
		assertEquals(HudPresentationMode.FULL, frame.region(HudRegion.QUEST_TRACKER).get(0).mode());
		assertEquals("middle", frame.region(HudRegion.QUEST_TRACKER).get(1).id().value());
		assertEquals(HudPresentationMode.COMPACT, frame.region(HudRegion.QUEST_TRACKER).get(1).mode());
	}

	@Test void replacingStableIdDoesNotDuplicateElement() {
		PlayerHudSession session = new PlayerHudSession(UUID.randomUUID());
		session.put(element("quest", 10, false));
		session.put(element("quest", 40, false));
		HudFrame frame = new HudCompositor(new HudLayoutPolicy(Map.of())).compose(session, 0);
		assertEquals(1, frame.region(HudRegion.QUEST_TRACKER).size());
		assertEquals(40, frame.region(HudRegion.QUEST_TRACKER).getFirst().priority());
	}

	@Test void topRightAnchorTracksDifferentScaledViewports() {
		PlayerHudSession session = new PlayerHudSession(UUID.randomUUID());
		session.put(element("quest", 10, false));
		HudCompositor compositor = new HudCompositor(new HudLayoutPolicy(Map.of()));
		HudRect small = compositor.compose(session, 0, new HudViewport(320, 180, 3, HudInsets.NONE))
				.region(HudRegion.QUEST_TRACKER).getFirst().bounds();
		HudRect large = compositor.compose(session, 0, new HudViewport(640, 360, 2, HudInsets.NONE))
				.region(HudRegion.QUEST_TRACKER).getFirst().bounds();
		assertEquals(320, large.x() - small.x());
		assertEquals(small.y(), large.y());
	}

	@Test void expirationAndSuppressionAreResolvedBeforeLayout() {
		PlayerHudSession session = new PlayerHudSession(UUID.randomUUID());
		session.put(element("status", 1, false, HudRegion.STATUS, 10, Set.of()));
		session.put(element("dialogue", 5, false, HudRegion.DIALOGUE, Long.MAX_VALUE, Set.of(HudRegion.STATUS)));
		HudCompositor compositor = new HudCompositor(new HudLayoutPolicy(Map.of()));
		HudFrame suppressed = compositor.compose(session, 5);
		assertTrue(suppressed.region(HudRegion.STATUS).isEmpty());
		assertEquals(1, suppressed.region(HudRegion.DIALOGUE).size());

		session.remove(HudElementId.of("test", "dialogue"));
		assertTrue(compositor.compose(session, 10).region(HudRegion.STATUS).isEmpty());
	}

	@Test void elementOverrideCanMoveOrDisableOneElementWithoutChangingItsRegion() {
		PlayerHudSession session = new PlayerHudSession(UUID.randomUUID());
		HudElement movable = element("movable", 10, false);
		HudElement hidden = element("hidden", 9, false);
		session.put(movable);
		session.put(hidden);
		HudElementLayout override = new HudElementLayout(true, HudAnchor.TOP_LEFT, 17, 23, 90);
		HudElementLayout disabled = new HudElementLayout(false, HudAnchor.TOP_LEFT, 0, 0, 90);
		HudLayoutPolicy policy = new HudLayoutPolicy(Map.of(), Map.of(movable.id(), override, hidden.id(), disabled));

		HudFrame frame = new HudCompositor(policy).compose(session, 0, new HudViewport(320, 180, 3, HudInsets.NONE));

		assertEquals(1, frame.region(HudRegion.QUEST_TRACKER).size());
		assertEquals(new HudRect(17, 23, frame.region(HudRegion.QUEST_TRACKER).getFirst().bounds().width(),
				frame.region(HudRegion.QUEST_TRACKER).getFirst().bounds().height()),
				frame.region(HudRegion.QUEST_TRACKER).getFirst().bounds());
	}

	@Test void isolatesBrokenElementsAndReportsTheirFailure() {
		PlayerHudSession session = new PlayerHudSession(UUID.randomUUID());
		session.put(element("healthy", 1, false));
		session.put(new HudElement() {
			public HudElementId id() { return HudElementId.of("test", "broken"); }
			public HudRegion region() { return HudRegion.QUEST_TRACKER; }
			public int priority() { return 2; }
			public HudContent render(HudRenderContext context) { throw new IllegalStateException("broken presenter"); }
		});
		java.util.List<HudCompositionFailure> failures = new java.util.ArrayList<>();
		HudRegionLayout capacityOne = new HudRegionLayout(HudAnchor.TOP_RIGHT, -8, 8, 160, 1,
				HudAxis.VERTICAL, 0, HudAlignment.END);
		HudCompositor compositor = new HudCompositor(new HudLayoutPolicy(Map.of(HudRegion.QUEST_TRACKER, capacityOne)),
				new org.tomdang.hud.composition.layout.HudLayoutEngine(
						new org.tomdang.hud.composition.layout.MinecraftHudTextMetrics()),
				HudVisibilityPolicy.regionSuppression(), failures::add);
		HudFrame frame = compositor.compose(session, 0);
		assertEquals(1, frame.region(HudRegion.QUEST_TRACKER).size());
		assertEquals("healthy", frame.region(HudRegion.QUEST_TRACKER).getFirst().id().value());
		assertEquals(HudCompositionFailure.Stage.PRESENTATION, failures.getFirst().stage());
	}

	private HudElement element(String id, int priority, boolean compact) {
		return element(id, priority, compact, HudRegion.QUEST_TRACKER, Long.MAX_VALUE, Set.of());
	}

	private HudElement element(String id, int priority, boolean compact, HudRegion region, long expires, Set<HudRegion> suppresses) {
		return new HudElement() {
			@Override public HudElementId id() { return HudElementId.of("test", id); }
			@Override public HudRegion region() { return region; }
			@Override public int priority() { return priority; }
			@Override public boolean canCompact() { return compact; }
			@Override public long expiresAtTick() { return expires; }
			@Override public Set<HudRegion> suppressesRegions() { return suppresses; }
			@Override public HudContent render(HudRenderContext context) {
				return new HudContent(new HudPrimitive(HudTextCommand.text(id + ":" + context.mode())));
			}
		};
	}
}
