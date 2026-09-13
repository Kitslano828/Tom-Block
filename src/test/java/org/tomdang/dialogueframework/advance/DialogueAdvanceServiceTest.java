package org.tomdang.dialogueframework.advance;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.dialogueframework.DialogueController;
import org.tomdang.dialogueframework.context.DialogueContext;
import org.tomdang.dialogueframework.definition.DialogueChoice;
import org.tomdang.dialogueframework.definition.DialogueNode;
import org.tomdang.dialogueframework.presentation.choice.DialogueChoicePresentation;
import org.tomdang.dialogueframework.presentation.hud.DialogueDisplayState;
import org.tomdang.dialogueframework.presentation.hud.DialogueDisplayStateRegistry;
import org.tomdang.dialogueframework.presentation.hud.DialogueTextAnimationService;
import org.tomdang.dialogueframework.presentation.hud.page.DialoguePage;
import org.tomdang.dialogueframework.presentation.hud.page.DialoguePaginationService;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkin;
import org.tomdang.dialogueframework.presentation.hud.skin.DialogueHudSkinRegistry;
import org.tomdang.dialogueframework.session.DialogueChoiceSelectionResult;
import org.tomdang.dialogueframework.session.DialogueSession;
import org.tomdang.dialogueframework.session.DialogueSessionService;
import org.tomdang.dialogueframework.theme.DialogueThemeDefinition;
import org.tomdang.dialogueframework.theme.DialogueThemeRegistry;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DialogueAdvanceServiceTest {

	private final UUID playerUUID = UUID.randomUUID();
	private final DialogueContext context = new DialogueContext("BLACKSMITH", UUID.randomUUID());

	private DialogueSessionService sessionService;
	private DialogueDisplayStateRegistry displayStateRegistry;
	private DialogueTextAnimationService animationService;
	private DialogueController controller;
	private DialogueChoicePresentation choicePresentation;
	private DialoguePaginationService paginationService;
	private DialogueThemeRegistry themeRegistry;
	private DialogueHudSkinRegistry skinRegistry;
	private DialogueAdvanceService advanceService;
	private Player player;
	private DialogueSession session;
	private DialogueDisplayState displayState;
	private DialogueNode node;
	private DialogueHudSkin skin;
	private DialoguePage firstPage;

	@BeforeEach
	void setUp() {
		sessionService = mock(DialogueSessionService.class);
		displayStateRegistry = mock(DialogueDisplayStateRegistry.class);
		animationService = mock(DialogueTextAnimationService.class);
		controller = mock(DialogueController.class);
		choicePresentation = mock(DialogueChoicePresentation.class);
		paginationService = mock(DialoguePaginationService.class);
		themeRegistry = mock(DialogueThemeRegistry.class);
		skinRegistry = mock(DialogueHudSkinRegistry.class);
		player = mock(Player.class);
		session = mock(DialogueSession.class);
		displayState = mock(DialogueDisplayState.class);
		node = mock(DialogueNode.class);
		skin = mock(DialogueHudSkin.class);
		firstPage = new DialoguePage(List.of("First page"), 0, 10);

		advanceService = new DialogueAdvanceService(
				sessionService,
				displayStateRegistry,
				animationService,
				controller,
				choicePresentation,
				paginationService,
				themeRegistry,
				skinRegistry
		);

		when(player.getUniqueId()).thenReturn(playerUUID);
		when(sessionService.getActiveSession(playerUUID)).thenReturn(session);
		when(session.getCurrentNodeID()).thenReturn("GREETING");
		when(session.getCurrentNode()).thenReturn(node);
		when(session.getDialogueContext()).thenReturn(context);
		when(node.getNodeID()).thenReturn("GREETING");
		when(node.getDialogueText()).thenReturn("First page");
		when(displayStateRegistry.getState(playerUUID)).thenReturn(displayState);
		when(displayState.getDisplayNodeID()).thenReturn("GREETING");
		when(displayState.getCurrentPageIndex()).thenReturn(0);
		when(themeRegistry.resolveThemeForSource("BLACKSMITH"))
				.thenReturn(new DialogueThemeDefinition("BLACKSMITH_THEME", "Blacksmith", "BLACKSMITH_BOX"));
		when(skinRegistry.lookupSkin("BLACKSMITH_BOX")).thenReturn(skin);
		when(paginationService.paginate(skin, "First page")).thenReturn(List.of(firstPage));
	}

	@Test
	void unfinishedPageIsRevealedBeforeAnyNavigation() {
		when(displayState.isCurrentPageFullyRevealed()).thenReturn(false);

		DialogueAdvanceResult result = advanceService.advance(player);

		assertEquals(DialogueAdvanceResult.TEXT_REVEALED, result);
		verify(animationService).revealAll(player, session);
		verify(displayState, never()).advancePage(1, firstPage.getBeginningIndex());
		verify(controller, never()).endDialogue(player);
		verify(choicePresentation, never()).showChoices(player, session);
	}

	@Test
	void completedPageAdvancesWhenAnotherPageExists() {
		DialoguePage secondPage = new DialoguePage(List.of("Second page"), 11, 22);
		when(displayState.isCurrentPageFullyRevealed()).thenReturn(true);
		when(paginationService.paginate(skin, "First page")).thenReturn(List.of(firstPage, secondPage));

		DialogueAdvanceResult result = advanceService.advance(player);

		assertEquals(DialogueAdvanceResult.PAGE_ADVANCED, result);
		verify(displayState).advancePage(2, 11);
		verify(animationService).refresh(player, session);
		verify(controller, never()).endDialogue(player);
		verify(choicePresentation, never()).showChoices(player, session);
	}

	@Test
	void completedPageWithoutChoicesStillAdvancesWhenAnotherPageExists() {
		DialoguePage secondPage = new DialoguePage(List.of("Second page"), 11, 22);
		when(displayState.isCurrentPageFullyRevealed()).thenReturn(true);
		when(node.getDialogueChoices()).thenReturn(List.of());
		when(paginationService.paginate(skin, "First page")).thenReturn(List.of(firstPage, secondPage));

		DialogueAdvanceResult result = advanceService.advance(player);

		assertEquals(DialogueAdvanceResult.PAGE_ADVANCED, result);
		verify(displayState).advancePage(2, 11);
		verify(controller, never()).endDialogue(player);
	}

	@Test
	void finalPageWithoutChoicesEndsDialogue() {
		when(displayState.isCurrentPageFullyRevealed()).thenReturn(true);
		when(node.getDialogueChoices()).thenReturn(List.of());

		DialogueAdvanceResult result = advanceService.advance(player);

		assertEquals(DialogueAdvanceResult.DIALOGUE_ENDED, result);
		verify(controller).endDialogue(player);
	}

	@Test
	void finalPageWithOneChoiceAdvancesNodeWhenSessionRemainsActive() {
		DialogueChoice choice = new DialogueChoice("CONTINUE", "Continue", "NEXT");
		when(displayState.isCurrentPageFullyRevealed()).thenReturn(true);
		when(node.getDialogueChoices()).thenReturn(List.of(choice));
		when(controller.selectChoice(player, "CONTINUE"))
				.thenReturn(new DialogueChoiceSelectionResult(choice, context, session));

		DialogueAdvanceResult result = advanceService.advance(player);

		assertEquals(DialogueAdvanceResult.NODE_ADVANCED, result);
		verify(controller).selectChoice(player, "CONTINUE");
	}

	@Test
	void finalPageWithOneChoiceCanEndDialogue() {
		DialogueChoice choice = new DialogueChoice("LEAVE", "Leave", null);
		when(displayState.isCurrentPageFullyRevealed()).thenReturn(true);
		when(node.getDialogueChoices()).thenReturn(List.of(choice));
		when(controller.selectChoice(player, "LEAVE"))
				.thenReturn(new DialogueChoiceSelectionResult(choice, context, null));

		DialogueAdvanceResult result = advanceService.advance(player);

		assertEquals(DialogueAdvanceResult.DIALOGUE_ENDED, result);
	}

	@Test
	void finalPageWithSeveralChoicesShowsChoicesOnce() {
		DialogueChoice firstChoice = new DialogueChoice("FIRST", "First", null);
		DialogueChoice secondChoice = new DialogueChoice("SECOND", "Second", null);
		when(displayState.isCurrentPageFullyRevealed()).thenReturn(true);
		when(displayState.areChoicesShown()).thenReturn(false);
		when(node.getDialogueChoices()).thenReturn(List.of(firstChoice, secondChoice));

		DialogueAdvanceResult result = advanceService.advance(player);

		assertEquals(DialogueAdvanceResult.CHOICE_REQUIRED, result);
		verify(choicePresentation).showChoices(player, session);
		verify(displayState).markChoicesShown();
		verify(animationService).refresh(player, session);
	}

	@Test
	void alreadyShownChoicesAreNotPresentedAgain() {
		DialogueChoice firstChoice = new DialogueChoice("FIRST", "First", null);
		DialogueChoice secondChoice = new DialogueChoice("SECOND", "Second", null);
		when(displayState.isCurrentPageFullyRevealed()).thenReturn(true);
		when(displayState.areChoicesShown()).thenReturn(true);
		when(node.getDialogueChoices()).thenReturn(List.of(firstChoice, secondChoice));

		DialogueAdvanceResult result = advanceService.advance(player);

		assertEquals(DialogueAdvanceResult.CHOICE_REQUIRED, result);
		verify(choicePresentation, never()).showChoices(player, session);
		verify(displayState, never()).markChoicesShown();
		verify(animationService, never()).refresh(player, session);
	}
}
