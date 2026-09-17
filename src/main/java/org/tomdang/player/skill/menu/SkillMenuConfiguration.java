package org.tomdang.player.skill.menu;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.player.skill.SkillType;
import org.tomdang.player.stats.menu.BorderedMenuSlotCalculator;

import java.io.Reader;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Presentation-only settings; skill XP and rewards remain in the skill model. */
public record SkillMenuConfiguration(String title, Material backgroundMaterial, Material overviewAccent,
		List<Integer> accentSlots, Material progressEmpty, Material closeMaterial,
		Material backMaterial, int closeSlot, int backSlot, int detailSlot,
		Map<SkillType, Entry> entries) {
	public record Entry(String name, String description, Material material, Material accent,
	                    Material progressFilled,
	                    TextColor color, int slot) { }

	public SkillMenuConfiguration {
		if (title == null || title.isBlank() || backgroundMaterial == null || overviewAccent == null
				|| accentSlots == null || progressEmpty == null || closeMaterial == null
				|| backMaterial == null || entries == null || entries.size() != SkillType.values().length)
			throw new IllegalArgumentException("Incomplete skills menu configuration");
		entries = Map.copyOf(entries);
		accentSlots = List.copyOf(accentSlots);
		Set<Integer> border = Set.copyOf(new BorderedMenuSlotCalculator().borderSlots());
		if (accentSlots.stream().anyMatch(slot -> slot == null || !border.contains(slot)))
			throw new IllegalArgumentException("Accent slots must be on the outer border");
		if (!border.contains(closeSlot) || !border.contains(backSlot) || border.contains(detailSlot)
				|| (detailSlot >= 28 && detailSlot <= 34)
				|| closeSlot == backSlot) throw new IllegalArgumentException("Invalid navigation or detail slots");
		java.util.HashSet<Integer> occupied = new java.util.HashSet<>();
		occupied.add(closeSlot);
		for (SkillType skill : SkillType.values()) {
			Entry entry = entries.get(skill);
			if (entry == null || entry.name() == null || entry.name().isBlank()
					|| entry.description() == null || entry.material() == null || entry.accent() == null
					|| entry.progressFilled() == null
					|| entry.color() == null || border.contains(entry.slot()) || !occupied.add(entry.slot()))
				throw new IllegalArgumentException("Invalid or overlapping skill entry: " + skill);
		}
	}

	public static SkillMenuConfiguration load(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");
		ConfigurationSection root = YamlConfiguration.loadConfiguration(reader).getConfigurationSection("skills-menu");
		if (root == null) throw new IllegalArgumentException("Missing skills-menu section");
		ConfigurationSection navigation = root.getConfigurationSection("navigation");
		ConfigurationSection configuredSkills = root.getConfigurationSection("skills");
		if (navigation == null || configuredSkills == null) throw new IllegalArgumentException("Missing skills menu sections");
		List<Integer> accentSlots = root.getIntegerList("accent-slots");
		if (!root.isList("accent-slots") || accentSlots.isEmpty())
			throw new IllegalArgumentException("Missing accent-slots list");
		EnumMap<SkillType, Entry> entries = new EnumMap<>(SkillType.class);
		for (SkillType skill : SkillType.values()) {
			ConfigurationSection section = configuredSkills.getConfigurationSection(skill.name().toLowerCase());
			if (section == null) throw new IllegalArgumentException("Missing skill presentation: " + skill);
			entries.put(skill, new Entry(text(section, "name"), text(section, "description"),
					material(section, "material"), material(section, "accent-material"),
					material(section, "progress-material"),
					color(section, "color"), slot(section, "slot")));
		}
		return new SkillMenuConfiguration(text(root, "title"), material(root, "background-material"),
				material(root, "overview-accent-material"), accentSlots, material(root, "progress-empty-material"),
				material(navigation, "close-material"), material(navigation, "back-material"),
				slot(navigation, "close-slot"), slot(navigation, "back-slot"),
				slot(navigation, "detail-slot"), entries);
	}

	private static String text(ConfigurationSection section, String key) {
		String value = section.getString(key);
		if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing " + section.getCurrentPath() + "." + key);
		return value;
	}
	private static int slot(ConfigurationSection section, String key) {
		if (!section.isInt(key)) throw new IllegalArgumentException("Invalid slot: " + key);
		int value = section.getInt(key);
		if (value < 0 || value >= 54) throw new IllegalArgumentException("Slot out of range: " + key);
		return value;
	}
	private static Material material(ConfigurationSection section, String key) {
		Material value = Material.matchMaterial(text(section, key));
		if (value == null || value == Material.AIR || value == Material.CAVE_AIR || value == Material.VOID_AIR)
			throw new IllegalArgumentException("Invalid material: " + key);
		return value;
	}
	private static TextColor color(ConfigurationSection section, String key) {
		String hex = text(section, key);
		if (!hex.matches("#[0-9a-fA-F]{6}")) throw new IllegalArgumentException("Invalid color: " + hex);
		return TextColor.fromHexString(hex);
	}
}
