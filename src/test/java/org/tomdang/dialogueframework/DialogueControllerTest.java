package org.tomdang.dialogueframework;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.tomdang.dialogueframework.action.DialogueChoiceActionContext;
import org.tomdang.dialogueframework.action.DialogueChoiceActionService;
import org.tomdang.dialogueframework.context.DialogueContext;
import org.tomdang.dialogueframework.definition.DialogueChoice;
import org.tomdang.dialogueframework.presentation.DialoguePresentation;
import org.tomdang.dialogueframework.session.DialogueChoiceSelectionResult;
import org.tomdang.dialogueframework.session.DialogueSession;
import org.tomdang.dialogueframework.session.DialogueSessionService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

public class DialogueControllerTest {

	private final DialogueSessionService dialogueSessionService = mock(DialogueSessionService.class);
	private final DialoguePresentation dialoguePresentation = mock(DialoguePresentation.class);
	private final DialogueChoiceActionService dialogueChoiceActionService = mock(DialogueChoiceActionService.class);
	private final Player player = mock(Player.class);

	private final DialogueController controller = new DialogueController(dialogueSessionService, dialoguePresentation, dialogueChoiceActionService);

	@Test
	void continuingChoiceUpdatesPresentation() {
		DialogueChoice choice = mock(DialogueChoice.class);
		DialogueContext context = mock(DialogueContext.class);
		DialogueSession session = mock(DialogueSession.class);

		UUID uuid = UUID.randomUUID();
		String choiceID = "CONTINUE";

		DialogueChoiceSelectionResult choiceSelectionResult = new DialogueChoiceSelectionResult(choice, context, session);

		when(player.getUniqueId()).thenReturn(uuid);
		when(dialogueSessionService.selectChoice(uuid, choiceID)).thenReturn(choiceSelectionResult);

		DialogueChoiceSelectionResult result = controller.selectChoice(player, choiceID);
		assertSame(choiceSelectionResult, result);

		verify(dialoguePresentation).show(player, session);
		verify(dialoguePresentation, never()).close(player);

		verifyNoInteractions(dialogueChoiceActionService);
	}

	@Test
	void endingChoiceClosesPresentation() {
		DialogueChoice choice = mock(DialogueChoice.class);
		DialogueContext context = mock(DialogueContext.class);

		UUID uuid =UUID.randomUUID();
		String choiceID = "GOODBYE";

		DialogueChoiceSelectionResult selectionResult = new DialogueChoiceSelectionResult(choice, context, null);

		when(player.getUniqueId()).thenReturn(uuid);
		when(dialogueSessionService.selectChoice(uuid, choiceID)).thenReturn(selectionResult);

		DialogueChoiceSelectionResult result = controller.selectChoice(player, choiceID);

		assertSame(selectionResult, result);

		verify(dialoguePresentation).close(player);
		verify(dialoguePresentation, never()).show(any(Player.class), any(DialogueSession.class));
		verifyNoInteractions(dialogueChoiceActionService);
	}

	@Test
	void choiceWithActionExecutesAction() {
		DialogueChoice choice = mock(DialogueChoice.class);
		DialogueContext context = mock(DialogueContext.class);

		UUID uuid = UUID.randomUUID();
		String choiceID = "OPEN_FORGE";
		String actionID = "OPEN_FORGE";

		when(player.getUniqueId()).thenReturn(uuid);
		when(choice.getActionID()).thenReturn(actionID);

		DialogueChoiceSelectionResult selectionResult = new DialogueChoiceSelectionResult(choice, context, null);

		when(dialogueSessionService.selectChoice(uuid, choiceID)).thenReturn(selectionResult);

		DialogueChoiceSelectionResult result = controller.selectChoice(player, choiceID);

		assertSame(selectionResult, result);

		verify(dialoguePresentation).close(player);

		ArgumentCaptor<DialogueChoiceActionContext> contextCaptor = ArgumentCaptor.forClass(DialogueChoiceActionContext.class);

		verify(dialogueChoiceActionService).executeAction(eq("OPEN_FORGE"), contextCaptor.capture());
		DialogueChoiceActionContext capturedContext = contextCaptor.getValue();

		assertSame(player, capturedContext.player());
		assertSame(choice, capturedContext.selectedChoice());
		assertSame(context, capturedContext.context());
	}
}
