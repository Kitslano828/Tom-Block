package org.tomdang.combat.attackspeed;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.combat.damage.BasicAttackCalculationProfile;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.combat.CustomItemCombatProfile;
import org.tomdang.player.playerresource.PlayerStatsService;
import org.tomdang.player.stats.PlayerStatType;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AttackRecoveryStatSelectorTest {
	private final AttackRecoveryStatSelector selector = new AttackRecoveryStatSelector();
	private final Player player = mock(Player.class);
	private final PlayerStatsService stats = mock(PlayerStatsService.class);

	@Test
	void ordinaryAttacksUseAttackSpeed() {
		when(stats.getTotalStat(player, PlayerStatType.ATTACK_SPEED)).thenReturn(250.0);
		assertEquals(250.0, selector.select(player, null, stats));
	}

	@Test
	void netHuntingDoesNotUseCombatAttackSpeed() {
		CustomItem net = mock(CustomItem.class);
		when(net.getCombatProfile()).thenReturn(new CustomItemCombatProfile(Optional.empty(), Optional.empty(),
				OptionalLong.of(20), Set.of(), BasicAttackCalculationProfile.JELLYFISH_HUNTING));
		assertEquals(0.0, selector.select(player, net, stats));
		verify(stats, never()).getTotalStat(player, PlayerStatType.ATTACK_SPEED);
	}
}
