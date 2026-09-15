package org.tomdang.customitemframework.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.customitemframework.refresh.PlayerInventoryItemRefreshResult;
import org.tomdang.customitemframework.refresh.PlayerInventoryItemRefreshService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshItemsCommandTest {

	@Test
	void refreshesPlayerAndReportsResult() {
		PlayerInventoryItemRefreshService refreshService = mock(PlayerInventoryItemRefreshService.class);
		RefreshItemsCommand refreshItemsCommand = new RefreshItemsCommand(refreshService);
		Player player = mock(Player.class);
		Command command = mock(Command.class);
		when(refreshService.refresh(player)).thenReturn(new PlayerInventoryItemRefreshResult(41, 3, 37, 1));

		assertTrue(refreshItemsCommand.onCommand(player, command, "refreshitems", new String[0]));

		verify(refreshService).refresh(player);
		verify(player).sendMessage("Item refresh complete: inspected 41, updated 3, skipped 37, failed 1.");
	}

	@Test
	void rejectsConsoleAndUnexpectedArgumentsWithoutRefreshing() {
		PlayerInventoryItemRefreshService refreshService = mock(PlayerInventoryItemRefreshService.class);
		RefreshItemsCommand refreshItemsCommand = new RefreshItemsCommand(refreshService);
		Command command = mock(Command.class);
		CommandSender console = mock(CommandSender.class);
		Player player = mock(Player.class);

		assertTrue(refreshItemsCommand.onCommand(console, command, "refreshitems", new String[0]));
		assertTrue(refreshItemsCommand.onCommand(player, command, "refreshitems", new String[]{"extra"}));

		verify(console).sendMessage("This command can only be used by a player.");
		verify(player).sendMessage("Usage: /refreshitems");
		verify(refreshService, never()).refresh(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void rejectsMissingService() {
		assertThrows(IllegalArgumentException.class, () -> new RefreshItemsCommand(null));
	}
}
