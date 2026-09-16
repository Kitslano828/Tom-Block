package org.tomdang.bootstrap;

import org.junit.jupiter.api.Test;
import org.tomdang.TomBlock;
import org.tomdang.player.stats.PlayerStatType;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerStatRuleBootStrapTest {

	@Test
	void loadsAndRegistersBundledStatRules() {
		TomBlock plugin = mock(TomBlock.class);
		InputStream resource = getClass().getClassLoader().getResourceAsStream("stat-rules.yml");
		when(plugin.getResource("stat-rules.yml")).thenReturn(resource);

		PlayerStatRuleBootStrap bootStrap = new PlayerStatRuleBootStrap(plugin);

		assertEquals(PlayerStatType.values().length, bootStrap.getRegistry().asMap().size());
		assertFalse(bootStrap.getRegistry().get(PlayerStatType.MAX_HEALTH).getCap().isPresent());
		assertEquals(250,
				bootStrap.getRegistry().get(PlayerStatType.ATTACK_SPEED).getCap().orElseThrow());
		assertEquals(400,
				bootStrap.getRegistry().get(PlayerStatType.SPEED).getCap().orElseThrow());
	}

	@Test
	void rejectsMissingResourceAndNullPlugin() {
		TomBlock plugin = mock(TomBlock.class);
		when(plugin.getResource("stat-rules.yml")).thenReturn(null);

		assertThrows(IllegalStateException.class, () -> new PlayerStatRuleBootStrap(plugin));
		assertThrows(IllegalArgumentException.class, () -> new PlayerStatRuleBootStrap(null));
	}
}
