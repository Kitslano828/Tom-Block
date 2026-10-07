package org.tomdang.quest.presentation;

import org.junit.jupiter.api.Test;
import org.tomdang.hud.composition.HudPresentationMode;
import org.tomdang.hud.composition.HudRenderContext;
import org.tomdang.hud.composition.HudViewport;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.composition.layout.HudNode;
import org.tomdang.hud.composition.layout.HudPrimitive;
import org.tomdang.hud.composition.layout.HudPadding;
import org.tomdang.hud.composition.layout.HudStack;
import org.tomdang.hud.composition.layout.HudOverlay;
import org.tomdang.hud.composition.HudAnchor;
import org.tomdang.hud.composition.HudCompositor;
import org.tomdang.hud.composition.HudElementLayout;
import org.tomdang.hud.composition.HudLayoutPolicy;
import org.tomdang.hud.composition.HudRegion;
import org.tomdang.hud.composition.PlayerHudSession;
import org.tomdang.hud.text.TomBlockHudFonts;
import org.tomdang.quest.definition.QuestDefinition;
import org.tomdang.quest.definition.QuestObjectiveDefinition;
import org.tomdang.quest.definition.QuestObjectiveType;
import org.tomdang.quest.definition.QuestRepeatability;
import org.tomdang.quest.definition.QuestStageDefinition;
import org.tomdang.quest.progress.QuestProgress;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackedQuestHudElementTest {
	@Test void rendersConfiguredQuestCopyAndCompactFallback() {
		UUID player = UUID.randomUUID();
		QuestObjectiveDefinition objective = new QuestObjectiveDefinition("RETURN", QuestObjectiveType.COMPLETE_DIALOGUE,
				"WILL_RETURN", 1, false, Map.of(
				"tracker-title", "RETURN TO WILL",
				"tracker-description", "Speak with Will about the hunt.",
				"tracker-progress", "WILL SPOKEN TO"));
		QuestStageDefinition stage = new QuestStageDefinition("RETURN", "Return", List.of(objective), null, Map.of());
		QuestDefinition quest = new QuestDefinition("HUNT", "First Steps", "", "HUNTING",
				QuestRepeatability.ONCE, Set.of(), "RETURN", Map.of("RETURN", stage));
		QuestProgress progress = QuestProgress.start(player, "HUNT", "RETURN", Instant.EPOCH);
		TrackedQuestHudElement element = new TrackedQuestHudElement(quest, progress);
		var full = element.render(new HudRenderContext(player, 1, HudPresentationMode.FULL, HudViewport.DEFAULT, 180));
		assertTrue(faceTexts(full.root()).contains("RETURN TO WILL"));
		assertTrue(texts(full.root()).stream().noneMatch(value -> value.endsWith(":")));
		assertTrue(texts(full.root()).contains("x WILL SPOKEN TO"));

		var compact = element.render(new HudRenderContext(player, 1, HudPresentationMode.COMPACT, HudViewport.DEFAULT, 180));
		assertEquals(List.of("FIRST STEPS", "TRACKED QUEST"), faceTexts(compact.root()).stream().distinct().toList());
	}

	@Test void configuredTrackerIsPinnedInsideTheTopRightMargin() {
		UUID player = UUID.randomUUID();
		QuestObjectiveDefinition objective = new QuestObjectiveDefinition("OBSERVE", QuestObjectiveType.OBSERVE_CRITTER,
				"GLIMMERFLY", 1, false, Map.of("tracker-description",
				"Watch how the nearby Glimmerfly moves before approaching it."));
		QuestStageDefinition stage = new QuestStageDefinition("START", "Observe the Glimmerfly",
				List.of(objective), null, Map.of());
		QuestDefinition quest = new QuestDefinition("HUNT", "A Hunter's First Steps", "", "HUNTING",
				QuestRepeatability.ONCE, Set.of(), "START", Map.of("START", stage));
		TrackedQuestHudElement element = new TrackedQuestHudElement(quest,
				QuestProgress.start(player, "HUNT", "START", Instant.EPOCH));
		HudElementLayout placement = new HudElementLayout(true, HudAnchor.TOP_RIGHT, -8, 8, 180);
		HudLayoutPolicy policy = new HudLayoutPolicy(Map.of(), Map.of(TrackedQuestHudElement.ID, placement));
		PlayerHudSession session = new PlayerHudSession(player);
		session.put(element);

		var snapshot = new HudCompositor(policy).compose(session, 1).region(HudRegion.QUEST_TRACKER).getFirst();
		assertEquals(HudViewport.DEFAULT.width() - 8, snapshot.bounds().right());
		assertEquals(8, snapshot.bounds().y());
		assertTrue(snapshot.bounds().width() <= 180);
		assertTrue(texts(snapshot.content().root()).stream()
				.noneMatch("Watch how the nearby Glimmerfly moves before approaching it."::equals));
		assertTrue(textCommands(snapshot.content().root()).stream().noneMatch(HudTextCommand::wrap));
		assertEquals(TomBlockHudFonts.VANILLA, textCommands(snapshot.content().root()).getFirst().style().font());
		assertTrue(textCommands(snapshot.content().root()).stream()
				.allMatch(command -> TomBlockHudFonts.VANILLA.equals(command.style().font())));
	}

	private List<String> texts(HudNode node) {
		if (node instanceof HudPrimitive primitive && primitive.command() instanceof HudTextCommand text) return List.of(text.text());
		if (node instanceof HudPadding padding) return texts(padding.child());
		if (node instanceof HudStack stack) return stack.children().stream().flatMap(child -> texts(child).stream()).toList();
		if (node instanceof HudOverlay overlay) return overlay.children().stream().flatMap(child -> texts(child).stream()).toList();
		return List.of();
	}

	private List<String> faceTexts(HudNode node) {
		return textCommands(node).stream().filter(text -> !text.style().styleId().endsWith("-edge"))
				.map(HudTextCommand::text).toList();
	}

	private List<HudTextCommand> textCommands(HudNode node) {
		if (node instanceof HudPrimitive primitive && primitive.command() instanceof HudTextCommand text) return List.of(text);
		if (node instanceof HudPadding padding) return textCommands(padding.child());
		if (node instanceof HudStack stack) return stack.children().stream()
				.flatMap(child -> textCommands(child).stream()).toList();
		if (node instanceof HudOverlay overlay) return overlay.children().stream()
				.flatMap(child -> textCommands(child).stream()).toList();
		return List.of();
	}
}
