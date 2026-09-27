package org.tomdang.dialogueframework.integration.actor;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.definition.ActorDefinition;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.interaction.ActorInteractionContext;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.advance.DialogueAdvanceService;
import org.tomdang.dialogueframework.context.DialogueContext;
import org.tomdang.dialogueframework.definition.DialogueDefinition;
import org.tomdang.dialogueframework.session.DialogueSession;
import org.tomdang.dialogueframework.session.DialogueSessionService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ActorDialogueInteractionTest {
	private final DialogueController controller = mock(DialogueController.class);
	private final DialogueSessionService sessions = mock(DialogueSessionService.class);
	private final DialogueAdvanceService advance = mock(DialogueAdvanceService.class);
	private final Player player = mock(Player.class);
	private final ActorInstance actor = mock(ActorInstance.class);

	@Test
	void replacesStaleDialogueFromSameActorInOneInteraction() {
		UUID playerId = UUID.randomUUID();
		UUID actorId = UUID.randomUUID();
		ActorDefinition definition = mock(ActorDefinition.class);
		DialogueSession stale = mock(DialogueSession.class);
		DialogueDefinition staleDefinition = mock(DialogueDefinition.class);
		when(player.getUniqueId()).thenReturn(playerId);
		when(actor.getInstanceID()).thenReturn(actorId);
		when(actor.getActorDefinition()).thenReturn(definition);
		when(definition.getActorID()).thenReturn("CRITTER_HUNTER_WILL");
		when(stale.getDialogueContext()).thenReturn(DialogueContext.actor("CRITTER_HUNTER_WILL", actorId));
		when(stale.getDialogueDefinition()).thenReturn(staleDefinition);
		when(staleDefinition.getDialogueID()).thenReturn("CRITTER_HUNTER_WILL_REMINDER");
		when(sessions.getActiveSession(playerId)).thenReturn(stale);

		new ActorDialogueInteraction("CRITTER_HUNTER_WILL_LESSON", controller, sessions, advance)
				.interact(new ActorInteractionContext(player, actor));

		verify(controller).endDialogue(player);
		verify(controller).startDialogue(eq(player), eq("CRITTER_HUNTER_WILL_LESSON"),
				eq(DialogueContext.actor("CRITTER_HUNTER_WILL", actorId)));
		verifyNoInteractions(advance);
	}

	@Test
	void advancesWhenTheRequestedDialogueIsAlreadyActive() {
		UUID playerId = UUID.randomUUID();
		UUID actorId = UUID.randomUUID();
		DialogueSession active = mock(DialogueSession.class);
		DialogueDefinition activeDefinition = mock(DialogueDefinition.class);
		when(player.getUniqueId()).thenReturn(playerId);
		when(actor.getInstanceID()).thenReturn(actorId);
		when(active.getDialogueContext()).thenReturn(DialogueContext.actor("CRITTER_HUNTER_WILL", actorId));
		when(active.getDialogueDefinition()).thenReturn(activeDefinition);
		when(activeDefinition.getDialogueID()).thenReturn("CRITTER_HUNTER_WILL_LESSON");
		when(sessions.getActiveSession(playerId)).thenReturn(active);

		new ActorDialogueInteraction("CRITTER_HUNTER_WILL_LESSON", controller, sessions, advance)
				.interact(new ActorInteractionContext(player, actor));

		verify(advance).advance(player);
		verify(controller, never()).endDialogue(player);
		verify(controller, never()).startDialogue(any(), anyString(), any());
	}
}
