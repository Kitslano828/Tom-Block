package org.tomdang.region.bukkit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.tomdang.region.tracking.RegionMembershipSnapshot;
import org.tomdang.region.tracking.RegionMembershipTransition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegionTrackingDebugServiceTest {
	@Test
	void reportsTransitionsOnlyWhileEnabledAndClearsOnQuit() {
		RegionTrackingDebugService debug = new RegionTrackingDebugService();
		Player player = mock(Player.class);
		UUID id = UUID.randomUUID();
		when(player.getUniqueId()).thenReturn(id);
		RegionMembershipTransition change = new RegionMembershipTransition(RegionMembershipSnapshot.EMPTY,
				new RegionMembershipSnapshot(List.of("village"), List.of("village"), Optional.of("village")),
				List.of("village"), List.of());
		debug.onTransition(player, change);
		verify(player, never()).sendMessage(any(Component.class));
		assertEquals(change, debug.last(id).orElseThrow());
		debug.toggle(id);
		debug.onTransition(player, change);
		ArgumentCaptor<Component> message = ArgumentCaptor.forClass(Component.class);
		verify(player).sendMessage(message.capture());
		assertTrue(PlainTextComponentSerializer.plainText().serialize(message.getValue()).contains("+village"));
		debug.clear(id);
		assertFalse(debug.isWatching(id));
		assertTrue(debug.last(id).isEmpty());
	}
}
