package org.tomdang.player.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.stats.PlayerStatType;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SetStatCommandTest {

	@Test
	void setsAnyStatUsingItsStorageKey() {
		Fixture fixture = fixture();

		fixture.command().onCommand(fixture.player(), fixture.bukkitCommand(), "setstat",
				new String[]{"ability-haste", "125.5"});

		assertEquals(125.5, fixture.profile().getStats().get(PlayerStatType.ABILITY_HASTE), 0.000001);
		verify(fixture.player()).sendMessage("Set base Ability Haste to 125.5.");
	}

	@Test
	void acceptsEnumStyleStatNames() {
		Fixture fixture = fixture();

		fixture.command().onCommand(fixture.player(), fixture.bukkitCommand(), "setstat",
				new String[]{"MAX_HEALTH", "250"});

		assertEquals(250, fixture.profile().getStats().get(PlayerStatType.MAX_HEALTH), 0.000001);
	}

	@Test
	void resetRestoresEveryBaseStat() {
		Fixture fixture = fixture();
		for (PlayerStatType statType : PlayerStatType.values()) {
			fixture.profile().getStats().set(statType, statType.getDefaultValue() + 25);
		}

		fixture.command().onCommand(fixture.player(), fixture.bukkitCommand(), "setstat", new String[]{"reset"});

		for (PlayerStatType statType : PlayerStatType.values()) {
			assertEquals(statType.getDefaultValue(), fixture.profile().getStats().get(statType), 0.000001);
		}
		verify(fixture.player()).sendMessage("Reset all base stats to their default values.");
	}

	@Test
	void invalidInputDoesNotChangeStats() {
		Fixture fixture = fixture();
		double originalDefense = fixture.profile().getStats().get(PlayerStatType.DEFENSE);

		fixture.command().onCommand(fixture.player(), fixture.bukkitCommand(), "setstat", new String[]{"unknown", "10"});
		fixture.command().onCommand(fixture.player(), fixture.bukkitCommand(), "setstat", new String[]{"defense", "text"});
		fixture.command().onCommand(fixture.player(), fixture.bukkitCommand(), "setstat", new String[]{"defense", "NaN"});
		fixture.command().onCommand(fixture.player(), fixture.bukkitCommand(), "setstat", new String[]{"defense", "-1"});

		assertEquals(originalDefense, fixture.profile().getStats().get(PlayerStatType.DEFENSE), 0.000001);
	}

	@Test
	void invalidUsageAndUnavailableProfileAreHandled() {
		Fixture fixture = fixture();
		fixture.command().onCommand(fixture.player(), fixture.bukkitCommand(), "setstat", new String[]{});

		Player missingPlayer = mock(Player.class);
		when(missingPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
		fixture.command().onCommand(missingPlayer, fixture.bukkitCommand(), "setstat",
				new String[]{"defense", "10"});

		verify(fixture.player()).sendMessage("Usage: /setstat <stat> <amount> or /setstat reset");
		verify(missingPlayer).sendMessage("Your player profile is not loaded.");
	}

	@Test
	void consoleSenderIsRejectedWithoutProfileLookup() {
		PlayerProfileService profileService = mock(PlayerProfileService.class);
		CommandSender sender = mock(CommandSender.class);

		new SetStatCommand(profileService).onCommand(sender, mock(Command.class), "setstat",
				new String[]{"defense", "10"});

		verify(sender).sendMessage("This command can only be used by a player.");
	}

	@Test
	void tabCompletionListsResetAndMatchingStorageKeys() {
		Fixture fixture = fixture();

		assertEquals(List.of("ability-haste"), fixture.command().onTabComplete(
				fixture.player(), fixture.bukkitCommand(), "setstat", new String[]{"ability"}));
		assertEquals(List.of("reset"), fixture.command().onTabComplete(
				fixture.player(), fixture.bukkitCommand(), "setstat", new String[]{"res"}));
		assertEquals(List.of(), fixture.command().onTabComplete(
				fixture.player(), fixture.bukkitCommand(), "setstat", new String[]{"defense", "10"}));
	}

	@Test
	void nullProfileServiceIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new SetStatCommand(null));
	}

	private Fixture fixture() {
		UUID uuid = UUID.randomUUID();
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(uuid);
		PlayerProfile profile = new PlayerProfile(uuid);
		PlayerProfileService profileService = mock(PlayerProfileService.class);
		when(profileService.getPlayerProfileFromMap(uuid)).thenReturn(profile);
		return new Fixture(player, profile, mock(Command.class), new SetStatCommand(profileService));
	}

	private record Fixture(Player player, PlayerProfile profile, Command bukkitCommand, SetStatCommand command) {
	}
}
