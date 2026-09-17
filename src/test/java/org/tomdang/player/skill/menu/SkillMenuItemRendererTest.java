package org.tomdang.player.skill.menu;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.player.skill.SkillProgress;
import org.tomdang.player.skill.SkillType;
import org.tomdang.player.skill.SkillXpCurve;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.presentation.PlayerStatPresentation;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.Component;

import static org.junit.jupiter.api.Assertions.*;

class SkillMenuItemRendererTest {
	private final PlayerStatPresentationRegistry statPresentations = presentations();
	private final SkillMenuItemRenderer renderer = new SkillMenuItemRenderer(statPresentations);
	private final SkillMenuConfiguration.Entry mining = new SkillMenuConfiguration.Entry(
			"Mining", "Mine blocks", Material.DIAMOND_PICKAXE, Material.LIGHT_BLUE_STAINED_GLASS_PANE,
			Material.CYAN_STAINED_GLASS_PANE,
			net.kyori.adventure.text.format.TextColor.fromHexString("#55FFFF"), 21);

	@Test
	void overviewExplainsLevelZeroAndClickTarget() {
		var item = renderer.render(SkillType.MINING, mining, SkillProgress.fromTotalXp(0), false);
		assertEquals("Mining 0", plain(item.displayName()));
		String lore = item.lore().stream().map(this::plain).reduce("", (a, b) -> a + " " + b);
		assertTrue(lore.contains("Progress to Level I: 0.0%"));
		assertTrue(lore.contains("0 / 35 XP"));
		assertTrue(lore.contains("+4 Mining Fortune"));
		assertTrue(lore.contains("Click to view!"));
	}

	@Test
	void detailShowsCurrentAndNextRewardButMaxLevelHasNoNext() {
		var mid = renderer.render(SkillType.MINING, mining,
				SkillProgress.fromTotalXp(SkillXpCurve.totalXpForLevel(5) + 10), true);
		String midLore = mid.lore().stream().map(this::plain).reduce("", (a, b) -> a + " " + b);
		assertTrue(midLore.contains("+20 Mining Fortune"));
		assertTrue(midLore.contains("+24 Mining Fortune"));
		var max = renderer.render(SkillType.MINING, mining,
				SkillProgress.fromTotalXp(SkillXpCurve.totalXpForLevel(100)), true);
		String maxLore = max.lore().stream().map(this::plain).reduce("", (a, b) -> a + " " + b);
		assertTrue(maxLore.contains("MAX LEVEL"));
		assertFalse(maxLore.contains("Next level:"));
		assertEquals("Mining C", plain(max.displayName()));
	}

	@Test
	void romanLevelsMatchSkyblockStyleWithoutInventingLevelOneHundredOne() {
		assertEquals("V", SkillMenuItemRenderer.roman(5));
		assertEquals("XLIX", SkillMenuItemRenderer.roman(49));
		assertEquals("C", SkillMenuItemRenderer.roman(100));
	}

	@Test
	void rewardAmountUsesStatPresentationColorNotSkillTheme() {
		var item = renderer.render(SkillType.MINING, mining, SkillProgress.fromTotalXp(0), true);
		Component reward = item.lore().stream().filter(line -> plain(line).startsWith("Current reward:")).findFirst().orElseThrow();
		assertEquals(TextColor.fromHexString("#EDC140"), reward.children().get(0).color());
		assertEquals(net.kyori.adventure.text.format.NamedTextColor.GRAY, reward.children().get(1).color());
	}

	private static PlayerStatPresentationRegistry presentations() {
		PlayerStatPresentationRegistry registry = new PlayerStatPresentationRegistry();
		registry.register(new PlayerStatPresentation(PlayerStatType.MINING_FORTUNE, "❀",
				TextColor.fromHexString("#EDC140"), "Mining drops", Material.GOLDEN_PICKAXE, true));
		registry.register(new PlayerStatPresentation(PlayerStatType.DAMAGE, "⚔",
				TextColor.fromHexString("#FF5555"), "Attack damage", Material.IRON_SWORD, true));
		return registry;
	}

	private String plain(net.kyori.adventure.text.Component component) {
		return PlainTextComponentSerializer.plainText().serialize(component);
	}
}
