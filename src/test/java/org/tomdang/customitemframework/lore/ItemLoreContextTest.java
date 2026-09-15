package org.tomdang.customitemframework.lore;

import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatSnapshot;
import org.tomdang.player.stats.PlayerStatType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemLoreContextTest {

	@Test
	void delegatesEffectiveStatLookupsToSnapshot() {
		PlayerStatSnapshot snapshot = new PlayerStatSnapshot(Map.of(PlayerStatType.ABILITY_HASTE, 175.0));
		ItemLoreContext context = new ItemLoreContext(snapshot);

		assertSame(snapshot, context.getStatSnapshot());
		assertEquals(175, context.getEffectiveStat(PlayerStatType.ABILITY_HASTE), 0.000001);
		assertEquals(PlayerStatType.MAX_HEALTH.getDefaultValue(),
				context.getEffectiveStat(PlayerStatType.MAX_HEALTH), 0.000001);
	}

	@Test
	void defaultContextUsesDefaultStatValues() {
		ItemLoreContext context = ItemLoreContext.defaults();

		assertEquals(PlayerStatType.ABILITY_HASTE.getDefaultValue(),
				context.getEffectiveStat(PlayerStatType.ABILITY_HASTE), 0.000001);
	}

	@Test
	void nullSnapshotAndStatTypeAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> new ItemLoreContext(null));
		assertThrows(IllegalArgumentException.class, () -> ItemLoreContext.defaults().getEffectiveStat(null));
	}
}
