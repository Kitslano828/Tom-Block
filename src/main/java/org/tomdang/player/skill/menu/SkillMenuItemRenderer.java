package org.tomdang.player.skill.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.tomdang.player.skill.SkillProgress;
import org.tomdang.player.skill.SkillType;
import org.tomdang.player.stats.PlayerStatValueFormatter;
import org.tomdang.player.stats.presentation.PlayerStatMenuItemDefinition;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SkillMenuItemRenderer {
	private final PlayerStatPresentationRegistry statPresentations;

	public SkillMenuItemRenderer(PlayerStatPresentationRegistry statPresentations) {
		if (statPresentations == null) throw new IllegalArgumentException("statPresentations cannot be null");
		this.statPresentations = statPresentations;
	}
	public PlayerStatMenuItemDefinition render(SkillType skill, SkillMenuConfiguration.Entry entry,
	                                         SkillProgress progress, boolean detail) {
		if (skill == null || entry == null || progress == null) throw new IllegalArgumentException("Skill item inputs are required");
		List<Component> lore = new ArrayList<>();
		lore.add(Component.text(entry.description(), NamedTextColor.GRAY));
		lore.add(Component.empty());
		if (detail) {
			lore.add(Component.text("Total XP: ", NamedTextColor.GRAY)
					.append(Component.text(String.format(Locale.ROOT, "%,d", progress.totalXp()), NamedTextColor.GOLD)));
			if (progress.maxLevel()) lore.add(Component.text("MAX LEVEL", NamedTextColor.GOLD));
			else {
				lore.add(Component.text("This level: ", NamedTextColor.GRAY)
						.append(Component.text(xpProgress(progress), entry.color())));
				lore.add(Component.text(String.format(Locale.ROOT, "Progress: %.1f%%", progress.fraction() * 100), entry.color()));
			}
			lore.add(Component.empty());
			lore.add(reward("Current reward: ", skill, progress.level()));
			if (!progress.maxLevel()) lore.add(reward("Next level: ", skill, progress.level() + 1));
		} else if (progress.maxLevel()) {
			lore.add(Component.text("MAX LEVEL", NamedTextColor.GOLD));
			lore.add(Component.text("Total XP: ", NamedTextColor.GRAY)
					.append(Component.text(String.format(Locale.ROOT, "%,d", progress.totalXp()), entry.color())));
			lore.add(Component.empty());
			lore.add(reward("Level " + roman(progress.level()) + " Reward: ", skill, progress.level()));
		} else {
			lore.add(Component.text("Progress to Level " + roman(progress.level() + 1) + ": ", NamedTextColor.GRAY)
					.append(Component.text(String.format(Locale.ROOT, "%.1f%%", progress.fraction() * 100), NamedTextColor.GOLD)));
			lore.add(progressBar(progress));
			lore.add(Component.text(xpProgress(progress) + " XP", NamedTextColor.GOLD));
			lore.add(Component.empty());
			lore.add(Component.text("Level " + roman(progress.level() + 1) + " Rewards:", NamedTextColor.GRAY));
			lore.add(reward("  ", skill, progress.level() + 1));
		}
		if (!detail) {
			lore.add(Component.empty());
			lore.add(Component.text("Click to view!", NamedTextColor.YELLOW));
		}
		return new PlayerStatMenuItemDefinition(entry.material(),
				Component.text(entry.name() + " " + roman(progress.level()), entry.color())
						.decoration(TextDecoration.ITALIC, false), unitalic(lore));
	}

	private Component reward(String prefix, SkillType skill, int level) {
		return Component.text(prefix, NamedTextColor.GRAY)
				.append(Component.text("+" + PlayerStatValueFormatter.format(skill.rewardAt(level)),
						statPresentations.get(skill.rewardStat()).color()))
				.append(Component.text(" " + skill.rewardStat().getDisplayName(), NamedTextColor.GRAY));
	}

	private String xpProgress(SkillProgress progress) {
		return String.format(Locale.ROOT, "%,d / %,d", progress.xpIntoLevel(), progress.xpNeededForNextLevel());
	}

	private Component progressBar(SkillProgress progress) {
		int filled = (int) Math.floor(progress.fraction() * 20);
		return Component.text("━".repeat(filled), NamedTextColor.GREEN)
				.append(Component.text("━".repeat(20 - filled), NamedTextColor.DARK_GRAY));
	}

	static String roman(int level) {
		if (level < 0 || level > 100) throw new IllegalArgumentException("level must be 0..100");
		if (level == 0) return "0";
		int remaining = level;
		StringBuilder result = new StringBuilder();
		int[] values = {100, 90, 50, 40, 10, 9, 5, 4, 1};
		String[] symbols = {"C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
		for (int index = 0; index < values.length; index++)
			while (remaining >= values[index]) { result.append(symbols[index]); remaining -= values[index]; }
		return result.toString();
	}

	public PlayerStatMenuItemDefinition navigation(Material material, String name) {
		return new PlayerStatMenuItemDefinition(material,
				Component.text(name, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false), List.of());
	}

	private List<Component> unitalic(List<Component> lore) {
		return lore.stream().map(line -> line.decoration(TextDecoration.ITALIC, false)).toList();
	}
}
