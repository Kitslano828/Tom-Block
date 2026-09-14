package org.tomdang.content.blacksmith;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.tomdang.actorframework.audience.ActorAudienceKey;
import org.tomdang.actorframework.audience.ActorAudienceScope;
import org.tomdang.actorframework.collision.ActorCollisionPolicy;
import org.tomdang.actorframework.combat.ActorDamagePolicy;
import org.tomdang.actorframework.definition.ActorDefinition;
import org.tomdang.actorframework.interaction.ActorInteractionRegistry;
import org.tomdang.actorframework.interaction.LookAtPlayerInteraction;
import org.tomdang.actorframework.movement.ActorLookService;
import org.tomdang.actorframework.nameplate.ActorNameplate;
import org.tomdang.actorframework.nameplate.ActorNameplateLine;
import org.tomdang.actorframework.nameplate.ActorNameplateLineRole;
import org.tomdang.actorframework.registry.ActorRegistry;
import org.tomdang.actorframework.spawn.ActorSpawnPoint;
import org.tomdang.actorframework.spawn.ActorSpawnPointRegistry;
import org.tomdang.crafting.dialogue.OpenForgeDialogueAction;
import org.tomdang.crafting.interaction.OpenForgeInteraction;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.action.DialogueChoiceActionRegistry;
import org.tomdang.dialogueframework.advance.DialogueAdvanceService;
import org.tomdang.dialogueframework.integration.actor.ActorDialogueInteraction;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.dialogueframework.presentation.hud.indicator.DialogueHudIndicatorStyle;
import org.tomdang.dialogueframework.presentation.hud.indicator.DialogueHudIndicatorState;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkinRegistry;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.dialogueframework.theme.DialogueThemeDefinition;
import org.tomdang.dialogueframework.theme.DialogueThemeRegistry;
import org.tomdang.hud.glyph.HudGlyph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlacksmithContent {
	private final DialogueThemeRegistry dialogueThemeRegistry;
	private final DialogueHudSkinRegistry dialogueHudSkinRegistry;
	private final DialogueController dialogueController;
	private final DialogueSessionService dialogueSessionService;
	private final DialogueAdvanceService dialogueAdvanceService;
	private final ActorInteractionRegistry actorInteractionRegistry;
	private final ActorRegistry actorRegistry;
	private final ActorSpawnPointRegistry actorSpawnPointRegistry;
	private final ActorLookService actorLookService;
	private final DialogueChoiceActionRegistry dialogueChoiceActionRegistry;

	public BlacksmithContent(DialogueThemeRegistry dialogueThemeRegistry, DialogueHudSkinRegistry dialogueHudSkinRegistry,
	                         DialogueController dialogueController, DialogueSessionService dialogueSessionService, DialogueAdvanceService dialogueAdvanceService,
	                         ActorInteractionRegistry actorInteractionRegistry, ActorRegistry actorRegistry, ActorSpawnPointRegistry actorSpawnPointRegistry,
							 ActorLookService actorLookService, DialogueChoiceActionRegistry dialogueChoiceActionRegistry
	) {
		if (dialogueThemeRegistry == null) throw new IllegalArgumentException("dialogueThemeRegistry cannot be null");
		if (dialogueHudSkinRegistry == null) throw new IllegalArgumentException("dialogueHudSkinRegistry cannot be null");
		if (dialogueController == null) throw new IllegalArgumentException("dialogueController cannot be null");
		if (dialogueSessionService == null) throw new IllegalArgumentException("dialogueSessionService cannot be null");
		if (dialogueAdvanceService == null) throw new IllegalArgumentException("dialogueAdvanceService cannot be null");
		if (actorInteractionRegistry == null) throw new IllegalArgumentException("actorInteractionRegistry cannot be null");
		if (actorRegistry == null) throw new IllegalArgumentException("actorRegistry cannot be null");
		if (actorSpawnPointRegistry == null) throw new IllegalArgumentException("actorSpawnPointRegistry cannot be null");
		if (actorLookService == null) throw new IllegalArgumentException("actorLookService cannot be null");
		if (dialogueChoiceActionRegistry == null) throw new IllegalArgumentException("dialogueChoiceActionRegistry cannot be null");

		this.dialogueThemeRegistry = dialogueThemeRegistry;
		this.dialogueHudSkinRegistry = dialogueHudSkinRegistry;
		this.dialogueController = dialogueController;
		this.dialogueSessionService = dialogueSessionService;
		this.dialogueAdvanceService = dialogueAdvanceService;
		this.actorInteractionRegistry = actorInteractionRegistry;
		this.actorRegistry = actorRegistry;
		this.actorSpawnPointRegistry = actorSpawnPointRegistry;
		this.actorLookService = actorLookService;
		this.dialogueChoiceActionRegistry = dialogueChoiceActionRegistry;
	}

	public void register() {
		OpenForgeInteraction openForgeInteraction = new OpenForgeInteraction();
		actorInteractionRegistry.registerInteraction("BLACKSMITH_INTERACTION", openForgeInteraction);

		OpenForgeDialogueAction openForgeDialogueAction = new OpenForgeDialogueAction();
		dialogueChoiceActionRegistry.registerAction("OPEN_FORGE", openForgeDialogueAction);

		ActorNameplateLine nameplateLine  = new ActorNameplateLine(ActorNameplateLineRole.NAME, Component.text("Blacksmith"),true);
		List<ActorNameplateLine> lines = new ArrayList<>();
		lines.add(nameplateLine);
		ActorNameplate basicBlacksmithNameplate = new ActorNameplate(lines);

		ActorDefinition blacksmithActor = new ActorDefinition(
				"BLACKSMITH",
				"Blacksmith",
				ActorAudienceScope.GLOBAL,
				"VILLAGER",
				"BLACKSMITH_INTERACTION",
				ActorDamagePolicy.PROTECTED,
				ActorCollisionPolicy.PASS_THROUGH,
				basicBlacksmithNameplate
		);
		actorRegistry.registerActor(blacksmithActor);

		ActorDefinition packetSmith = new ActorDefinition(
				"PACKET_SMITH",
				"Blacksmith ",
				ActorAudienceScope.GLOBAL,
				"PLAYER_NPC",
				"BLACKSMITH_INTERACTION",
				ActorDamagePolicy.PROTECTED,
				ActorCollisionPolicy.PASS_THROUGH,
				basicBlacksmithNameplate
		);
		actorRegistry.registerActor(packetSmith);

		Location packetSmithSpawnLocation = new Location(Bukkit.getWorld("world"), 93.5, 76, 187.5);
		ActorSpawnPoint packetSmithSpawnPoint = new ActorSpawnPoint(
				"PACKET_SMITH_TEST",
				"PACKET_SMITH",
				ActorAudienceKey.global(),
				packetSmithSpawnLocation
		);
		actorSpawnPointRegistry.registerSpawnPoint(packetSmithSpawnPoint);

		Location forgeSpawnLocation = new Location(Bukkit.getWorld("world"), 95.5, 76, 187.5);
		ActorSpawnPoint forgeSpawnPoint = new ActorSpawnPoint(
				"TOWN_BLACKSMITH",
				"BLACKSMITH",
				ActorAudienceKey.global(),
				forgeSpawnLocation
		);
		actorSpawnPointRegistry.registerSpawnPoint(forgeSpawnPoint);

		HudGlyph hudGlyph = new HudGlyph(Key.key("tomblock", "dialogue"), "\uE001", 256);
		List<Key> dialogueLineFonts = List.of(
				Key.key("tomblock", "dialogue_line_1"),
				Key.key("tomblock", "dialogue_line_2"),
				Key.key("tomblock", "dialogue_line_3")
		);

		Key blacksmithFont = Key.key("tomblock", "dialogue_speaker");
		Key dialogueIndicatorFont = Key.key("tomblock", "dialogue_indicator");

		DialogueHudIndicatorStyle continueIndicatorStyle = new DialogueHudIndicatorStyle(
				"»", dialogueIndicatorFont, TextColor.fromHexString("#FFF1D0"), 12
		);
		DialogueHudIndicatorStyle choiceIndicatorStyle = new DialogueHudIndicatorStyle(
				"?", dialogueIndicatorFont, TextColor.fromHexString("#FFF1D0"), 12
		);
		DialogueHudSkin hudSkin = new DialogueHudSkin("BLACKSMITH_BOX", hudGlyph, 12, 12, dialogueLineFonts,
				blacksmithFont, 12, 12, TextColor.fromHexString("#D8D8D8"), TextColor.fromHexString("#FFF1D0"),
				Map.of(
						DialogueHudIndicatorState.CONTINUE, continueIndicatorStyle,
						DialogueHudIndicatorState.CHOICE_REQUIRED, choiceIndicatorStyle
				));
		dialogueHudSkinRegistry.registerSkin(hudSkin);
		DialogueThemeDefinition themeDefinition = new DialogueThemeDefinition("BLACKSMITH_THEME", "Blacksmith", "BLACKSMITH_BOX");
		dialogueThemeRegistry.registerTheme(themeDefinition);
		dialogueThemeRegistry.bindSource("BLACKSMITH", "BLACKSMITH_THEME");

		ActorDialogueInteraction interaction = new ActorDialogueInteraction("BLACKSMITH_TEST_DIALOGUE", dialogueController, dialogueSessionService, dialogueAdvanceService);

		LookAtPlayerInteraction lookInteraction = new LookAtPlayerInteraction(actorLookService, interaction);
		actorInteractionRegistry.registerInteraction("BLACKSMITH_DIALOGUE_INTERACTION", lookInteraction);

		ActorNameplateLine talkingSmithNamePlateLine1 = new ActorNameplateLine(ActorNameplateLineRole.STATUS, Component.text("QUEST").color(TextColor.fromHexString("#EFBF04")).decoration(TextDecoration.BOLD, true), false);
		ActorNameplateLine talkingSmithNamePlateLine2 = new ActorNameplateLine(ActorNameplateLineRole.NAME, Component.text("Blacksmith"), true);
		ActorNameplateLine talkingSmithNamePlateLine3 = new ActorNameplateLine(ActorNameplateLineRole.INTERACTION, Component.text("CLICK"), false);

		List<ActorNameplateLine> talkingSmithLines = new ArrayList<>();
		talkingSmithLines.add(talkingSmithNamePlateLine1);
		talkingSmithLines.add(talkingSmithNamePlateLine2);
		talkingSmithLines.add(talkingSmithNamePlateLine3);

		ActorNameplate talkingSmithNameplate = new ActorNameplate(talkingSmithLines);

		ActorDefinition actorDefinition = new ActorDefinition(
				"TALKING_BLACKSMITH",
				"Talking Blacksmith",
				ActorAudienceScope.GLOBAL,
				"VILLAGER",
				"BLACKSMITH_DIALOGUE_INTERACTION",
				ActorDamagePolicy.PROTECTED,
				ActorCollisionPolicy.PASS_THROUGH,
				talkingSmithNameplate
		);

		actorRegistry.registerActor(actorDefinition);
		dialogueThemeRegistry.bindSource("TALKING_BLACKSMITH", "BLACKSMITH_THEME");

		Location spawnLocation = new Location(Bukkit.getWorld("world"), 95.5,76,192.5);
		ActorSpawnPoint actorSpawnPoint = new ActorSpawnPoint("TALKING_BLACKSMITH_TEST", "TALKING_BLACKSMITH", ActorAudienceKey.global(), spawnLocation);
		actorSpawnPointRegistry.registerSpawnPoint(actorSpawnPoint);
	}

}
