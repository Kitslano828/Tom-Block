package org.tomdang.hud.dialogue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.presentation.hud.DialogueHudRenderer;
import org.tomdang.dialogueframework.presentation.hud.indicator.DialogueHudIndicatorState;
import org.tomdang.dialogueframework.presentation.hud.page.DialoguePage;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.dialogueframework.presentation.hud.layout.DialogueHudLayoutComposer;
import org.tomdang.dialogueframework.theme.DialogueThemeDefinition;
import org.tomdang.hud.composition.*;
import org.tomdang.hud.presentation.*;
import org.tomdang.hud.spacing.HudSpacingService;
import org.tomdang.hud.text.TomBlockBitmapTextWidthService;
import java.util.*;

/** Projects the existing dialogue state machine into the shared HUD compositor. */
public final class HudEngineDialogueRenderer implements DialogueHudRenderer {
	private static final HudElementSpec SPEC = HudElementSpec.persistent(
			HudElementId.of("tomblock", "dialogue"), HudRegion.DIALOGUE, 100);
	private final ProductionHudService hud;
	private final DialogueHudLayoutComposer composer;
	public HudEngineDialogueRenderer(ProductionHudService hud) {
		this.hud = Objects.requireNonNull(hud);
		this.composer = new DialogueHudLayoutComposer(new HudSpacingService(), new TomBlockBitmapTextWidthService());
	}
	@Override public void render(Player player, DialogueThemeDefinition definition, DialogueHudSkin skin,
			DialogueNode node, DialoguePage page, int revealedCharacterCount, DialogueHudIndicatorState state) {
		if (player == null || definition == null || page == null || state == null)
			throw new IllegalArgumentException("Dialogue render arguments cannot be null");
		int remaining = Math.max(0, revealedCharacterCount - page.getBeginningIndex());
		List<String> visible = new ArrayList<>();
		for (String line : page.getLines()) {
			int shown = Math.min(line.length(), remaining);
			visible.add(line.substring(0, shown));
			remaining = Math.max(0, remaining - line.length());
		}
		var component = composer.compose(skin, visible, definition.speakerName(), state);
		hud.show(player.getUniqueId(), SPEC,
				new HudDialogueModel(component, skin.getBackgroundGlyph().getPixelWidth(), 64), Bukkit.getCurrentTick());
	}
	@Override public void clear(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		hud.hide(player.getUniqueId(), SPEC, Bukkit.getCurrentTick());
	}
}
