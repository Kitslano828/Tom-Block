package org.tomdang.content.blacksmith;

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
import org.tomdang.actorframework.registry.ActorRegistry;
import org.tomdang.actorframework.spawn.ActorSpawnPoint;
import org.tomdang.actorframework.spawn.ActorSpawnPointRegistry;
import org.tomdang.crafting.interaction.OpenForgeInteraction;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.advance.DialogueAdvanceService;
import org.tomdang.dialogueframework.definition.DialogueChoice;
import org.tomdang.dialogueframework.definition.DialogueDefinition;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.integration.actor.ActorDialogueInteraction;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkinRegistry;
import org.tomdang.dialogueframework.registry.DialogueRegistry;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.dialogueframework.theme.DialogueThemeDefinition;
import org.tomdang.dialogueframework.theme.DialogueThemeRegistry;

import java.util.List;

public class BlacksmithContent {
	private final DialogueThemeRegistry dialogueThemeRegistry;
	private final DialogueHudSkinRegistry dialogueHudSkinRegistry;
	private final DialogueRegistry dialogueRegistry;
	private final DialogueController dialogueController;
	private final DialogueSessionService dialogueSessionService;
	private final DialogueAdvanceService dialogueAdvanceService;
	private final ActorInteractionRegistry actorInteractionRegistry;
	private final ActorRegistry actorRegistry;
	private final ActorSpawnPointRegistry actorSpawnPointRegistry;
	private final ActorLookService actorLookService;

	public BlacksmithContent(DialogueThemeRegistry dialogueThemeRegistry, DialogueHudSkinRegistry dialogueHudSkinRegistry, DialogueRegistry dialogueRegistry,
	                         DialogueController dialogueController, DialogueSessionService dialogueSessionService, DialogueAdvanceService dialogueAdvanceService,
	                         ActorInteractionRegistry actorInteractionRegistry, ActorRegistry actorRegistry, ActorSpawnPointRegistry actorSpawnPointRegistry,
							 ActorLookService actorLookService
	) {
		if (dialogueThemeRegistry == null) throw new IllegalArgumentException("dialogueThemeRegistry cannot be null");
		if (dialogueHudSkinRegistry == null) throw new IllegalArgumentException("dialogueHudSkinRegistry cannot be null");
		if (dialogueRegistry == null) throw new IllegalArgumentException("dialogueRegistry cannot be null");
		if (dialogueController == null) throw new IllegalArgumentException("dialogueController cannot be null");
		if (dialogueSessionService == null) throw new IllegalArgumentException("dialogueSessionService cannot be null");
		if (dialogueAdvanceService == null) throw new IllegalArgumentException("dialogueAdvanceService cannot be null");
		if (actorInteractionRegistry == null) throw new IllegalArgumentException("actorInteractionRegistry cannot be null");
		if (actorRegistry == null) throw new IllegalArgumentException("actorRegistry cannot be null");
		if (actorSpawnPointRegistry == null) throw new IllegalArgumentException("actorSpawnPointRegistry cannot be null");
		if (actorLookService == null) throw new IllegalArgumentException("actorLookService cannot be null");

		this.dialogueThemeRegistry = dialogueThemeRegistry;
		this.dialogueHudSkinRegistry = dialogueHudSkinRegistry;
		this.dialogueRegistry = dialogueRegistry;
		this.dialogueController = dialogueController;
		this.dialogueSessionService = dialogueSessionService;
		this.dialogueAdvanceService = dialogueAdvanceService;
		this.actorInteractionRegistry = actorInteractionRegistry;
		this.actorRegistry = actorRegistry;
		this.actorSpawnPointRegistry = actorSpawnPointRegistry;
		this.actorLookService = actorLookService;
	}

	public void register() {
		OpenForgeInteraction openForgeInteraction = new OpenForgeInteraction();
		actorInteractionRegistry.registerInteraction("BLACKSMITH_INTERACTION", openForgeInteraction);

		ActorDefinition blacksmithActor = new ActorDefinition(
				"BLACKSMITH",
				"Blacksmith",
				ActorAudienceScope.GLOBAL,
				"VILLAGER",
				"BLACKSMITH_INTERACTION",
				ActorDamagePolicy.PROTECTED,
				ActorCollisionPolicy.PASS_THROUGH
		);
		actorRegistry.registerActor(blacksmithActor);

		ActorDefinition packetSmith = new ActorDefinition(
				"PACKET_SMITH",
				"Gay JimmyBot",
				ActorAudienceScope.GLOBAL,
				"PLAYER_NPC",
				"BLACKSMITH_INTERACTION",
				ActorDamagePolicy.PROTECTED,
				ActorCollisionPolicy.PASS_THROUGH
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

		DialogueHudSkin hudSkin = new DialogueHudSkin("BLACKSMITH_BOX", "dialogue/blacksmith_box");
		dialogueHudSkinRegistry.registerSkin(hudSkin);
		DialogueThemeDefinition themeDefinition = new DialogueThemeDefinition("BLACKSMITH_THEME", "Blacksmith", "BLACKSMITH_BOX");
		dialogueThemeRegistry.registerTheme(themeDefinition);
		dialogueThemeRegistry.bindSource("BLACKSMITH", "BLACKSMITH_THEME");

		DialogueChoice continueChoice =
				new DialogueChoice("CONTINUE", "Continue", "GOODBYE");
		DialogueChoice checkUpChoice =
				new DialogueChoice("CHECKUP", "How are you?", "CHECKUP");
		DialogueNode firstNode = new DialogueNode("GREETING", "Welcome to my forge.", List.of(checkUpChoice, continueChoice));
		DialogueNode secondNode = new DialogueNode("CHECKUP", "I am doing alright! How are you?", List.of(continueChoice));
		DialogueNode thirdNode = new DialogueNode("GOODBYE", "Come back whenever you need something forged.", List.of());
		DialogueDefinition definition = new DialogueDefinition("BLACKSMITH_TEST_DIALOGUE", "GREETING", List.of(firstNode, secondNode, thirdNode));
		dialogueRegistry.registerDialogue(definition);

		ActorDialogueInteraction interaction = new ActorDialogueInteraction("BLACKSMITH_TEST_DIALOGUE", dialogueController, dialogueSessionService, dialogueAdvanceService);

		LookAtPlayerInteraction lookInteraction = new LookAtPlayerInteraction(actorLookService, interaction);
		actorInteractionRegistry.registerInteraction("BLACKSMITH_DIALOGUE_INTERACTION", lookInteraction);

		ActorDefinition actorDefinition = new ActorDefinition("TALKING_BLACKSMITH", "Talking Blacksmith", ActorAudienceScope.GLOBAL,
				"VILLAGER", "BLACKSMITH_DIALOGUE_INTERACTION", ActorDamagePolicy.PROTECTED, ActorCollisionPolicy.PASS_THROUGH);
		actorRegistry.registerActor(actorDefinition);
		dialogueThemeRegistry.bindSource("TALKING_BLACKSMITH", "BLACKSMITH_THEME");

		Location spawnLocation = new Location(Bukkit.getWorld("world"), 95.5,76,192.5);
		ActorSpawnPoint actorSpawnPoint = new ActorSpawnPoint("TALKING_BLACKSMITH_TEST", "TALKING_BLACKSMITH", ActorAudienceKey.global(), spawnLocation);
		actorSpawnPointRegistry.registerSpawnPoint(actorSpawnPoint);
	}

}
