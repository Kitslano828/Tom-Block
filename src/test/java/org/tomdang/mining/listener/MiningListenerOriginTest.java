package org.tomdang.mining.listener;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.jupiter.api.Test;
import org.tomdang.mining.MiningProgressService;
import org.tomdang.mining.MiningService;

import static org.mockito.Mockito.*;

class MiningListenerOriginTest {
	@Test void playerPlacedResourceBypassesMiningProgressAndRewards() {
		MiningService mining = mock(MiningService.class);
		MiningProgressService progress = mock(MiningProgressService.class);
		BlockBreakEvent event = mock(BlockBreakEvent.class);
		Block block = mock(Block.class);
		Player player = mock(Player.class);
		when(event.getBlock()).thenReturn(block);
		when(event.getPlayer()).thenReturn(player);
		MiningListener listener = new MiningListener(mining, progress, ignored -> false);

		listener.onBlockBreak(event);

		verifyNoInteractions(mining, progress);
	}
}
