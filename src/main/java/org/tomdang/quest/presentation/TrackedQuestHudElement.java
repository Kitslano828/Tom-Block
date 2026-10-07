package org.tomdang.quest.presentation;

import org.tomdang.hud.composition.HudContent;
import org.tomdang.hud.composition.HudElement;
import org.tomdang.hud.composition.HudElementId;
import org.tomdang.hud.composition.HudPresentationMode;
import org.tomdang.hud.composition.HudRegion;
import org.tomdang.hud.composition.HudRenderContext;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.composition.draw.HudTextStyle;
import org.tomdang.hud.composition.layout.HudAlignment;
import org.tomdang.hud.composition.layout.HudNode;
import org.tomdang.hud.composition.layout.HudPrimitive;
import org.tomdang.hud.composition.layout.HudStack;
import org.tomdang.hud.composition.layout.HudTextEffects;
import org.tomdang.quest.definition.QuestDefinition;
import org.tomdang.quest.definition.QuestObjectiveDefinition;
import org.tomdang.quest.progress.QuestProgress;
import org.tomdang.hud.text.HudTextWrapper;
import org.tomdang.hud.text.TomBlockHudFonts;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Quest-owned view model; it has no knowledge of boss bars, packets, or scoreboards. */
public final class TrackedQuestHudElement implements HudElement {
	public static final HudElementId ID = HudElementId.of("tomblock", "tracked-quest");
	private final QuestDefinition quest;
	private final QuestProgress progress;

	public TrackedQuestHudElement(QuestDefinition quest, QuestProgress progress) {
		this.quest = java.util.Objects.requireNonNull(quest);
		this.progress = java.util.Objects.requireNonNull(progress);
	}

	@Override public HudElementId id() { return ID; }
	@Override public HudRegion region() { return HudRegion.QUEST_TRACKER; }
	@Override public int priority() { return 40; }
	@Override public boolean canCompact() { return true; }

	@Override public HudContent render(HudRenderContext context) {
		var stage = quest.stages().get(progress.currentStageId());
		QuestObjectiveDefinition objective = stage.objectives().stream()
				.filter(value -> progress.amount(stage.id(), value.id()) < value.requiredAmount())
				.findFirst().orElse(stage.objectives().getLast());
		List<HudNode> heading = new ArrayList<>();
		wrapped(heading, quest.displayName().toUpperCase(Locale.ROOT), "quest-title", 0xFCA800, true, context.availableWidth());
		heading.add(text("TRACKED QUEST", "quest-subtitle", 0xCE3303, false, context.availableWidth(), false));
		if (context.mode() == HudPresentationMode.COMPACT)
			return new HudContent(HudStack.vertical(0, HudAlignment.END, heading));
		List<HudNode> objectiveRows = new ArrayList<>();
		wrapped(objectiveRows, objective.parameters().getOrDefault("tracker-title",
				stage.displayName().toUpperCase(Locale.ROOT)), "quest-objective", 0xFCA800, false, context.availableWidth());
		wrapped(objectiveRows, objective.parameters().getOrDefault("tracker-description", readable(objective.id())),
				"quest-description", 0xECBE74, false, context.availableWidth());
		long amount = progress.amount(stage.id(), objective.id());
		boolean done = amount >= objective.requiredAmount();
		String label = objective.parameters().getOrDefault("tracker-progress", readable(objective.id()).toUpperCase(Locale.ROOT));
		String count = objective.requiredAmount() > 1 ? " " + amount + "/" + objective.requiredAmount() : "";
		wrapped(objectiveRows, (done ? "V " : "x ") + label + count, "quest-progress",
				done ? 0x55FF55 : 0xFC5454, false, context.availableWidth());
		HudNode guidance = text("Type /quest for guidance!", "quest-guidance", 0xA8A8A8, false,
				context.availableWidth(), false);
		return new HudContent(HudStack.vertical(3, HudAlignment.END, List.of(
				HudStack.vertical(0, HudAlignment.END, heading),
				HudStack.vertical(1, HudAlignment.END, objectiveRows), guidance)));
	}

	private static HudNode text(String value, String style, int color, boolean bold, int width, boolean wrap) {
		return HudTextEffects.edged(new HudTextCommand(value,
				new HudTextStyle(style, color, bold, false, font(style)), width, wrap));
	}

	private void wrapped(List<HudNode> rows, String value, String style, int color, boolean bold, int width) {
		var metrics = TomBlockHudFonts.profile(font(style)).orElseThrow().widths();
		for (String line : new HudTextWrapper(metrics).wrap(value, width))
			rows.add(text(line, style, color, bold, 0, false));
	}

	private static net.kyori.adventure.key.Key font(String style) {
		return switch (style) {
			case "quest-title", "quest-subtitle", "quest-objective", "quest-description",
					"quest-progress", "quest-guidance" -> TomBlockHudFonts.VANILLA;
			default -> throw new IllegalArgumentException("Unknown quest text style " + style);
		};
	}

	private static String readable(String value) {
		String lower = value.toLowerCase(Locale.ROOT).replace('_', ' ');
		return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
	}

}
