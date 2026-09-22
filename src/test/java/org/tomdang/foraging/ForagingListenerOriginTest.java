package org.tomdang.foraging;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ForagingListenerOriginTest {
	@Test void playerPlacedLogBypassesTreeRewards() {
		ForagingService service = mock(ForagingService.class);
		BlockBreakEvent event = mock(BlockBreakEvent.class);
		Block block = mock(Block.class);
		when(event.getBlock()).thenReturn(block);
		ForagingListener listener = new ForagingListener(service, ignored -> false);

		listener.onBreak(event);

		verifyNoInteractions(service);
	}
}
