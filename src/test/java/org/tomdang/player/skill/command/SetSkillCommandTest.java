package org.tomdang.player.skill.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.skill.SkillXpCurve;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SetSkillCommandTest {
	private final UUID playerId = UUID.randomUUID();
	private final Player player = mock(Player.class);
	private final PlayerProfile profile = new PlayerProfile(playerId);
	private final PlayerProfileService profiles = mock(PlayerProfileService.class);
	private final Command command = mock(Command.class);
	private final SetSkillCommand subject = new SetSkillCommand(profiles);

	SetSkillCommandTest() {
		when(player.getUniqueId()).thenReturn(playerId);
		when(profiles.getPlayerProfileFromMap(playerId)).thenReturn(profile);
	}

	@Test
	void levelSetsThresholdXpForEachSkill() {
		subject.onCommand(player, command, "setskill", new String[]{"mining", "lvl", "5"});
		assertEquals(SkillXpCurve.totalXpForLevel(5), profile.getMiningXP());
		assertEquals(5, profile.getMiningLVL());
		subject.onCommand(player, command, "setskill", new String[]{"combat", "lvl", "3"});
		assertEquals(SkillXpCurve.totalXpForLevel(3), profile.getCombatXP());
		assertEquals(3, profile.getCombatLvl());
	}

	@Test
	void xpDeterminesLevelAndIsCappedAtLevelOneHundred() {
		long threshold = SkillXpCurve.totalXpForLevel(5);
		subject.onCommand(player, command, "setskill", new String[]{"mining", "xp", Long.toString(threshold - 1)});
		assertEquals(4, profile.getMiningLVL());
		subject.onCommand(player, command, "setskill", new String[]{"combat", "xp", Long.toString(Long.MAX_VALUE)});
		assertEquals(100, profile.getCombatLvl());
		assertEquals(SkillXpCurve.totalXpForLevel(100), profile.getCombatXP());
	}

	@Test
	void rejectsInvalidInputWithoutChangingXp() {
		subject.onCommand(player, command, "setskill", new String[]{"unknown", "lvl", "5"});
		subject.onCommand(player, command, "setskill", new String[]{"mining", "bad", "5"});
		subject.onCommand(player, command, "setskill", new String[]{"mining", "lvl", "101"});
		subject.onCommand(player, command, "setskill", new String[]{"mining", "lvl", "1.5"});
		subject.onCommand(player, command, "setskill", new String[]{"mining", "xp", "-1"});
		assertEquals(0, profile.getMiningXP());
		assertEquals(0, profile.getCombatXP());
	}

	@Test
	void suggestsSkillsAndModesAndRejectsConsole() {
		assertEquals(java.util.List.of("mining"), subject.onTabComplete(player, command, "setskill", new String[]{"mi"}));
		assertEquals(java.util.List.of("lvl"), subject.onTabComplete(player, command, "setskill", new String[]{"mining", "l"}));
		CommandSender console = mock(CommandSender.class);
		assertTrue(subject.onCommand(console, command, "setskill", new String[]{"mining", "lvl", "5"}));
		verify(console).sendMessage("This command can only be used by a player.");
	}
}
