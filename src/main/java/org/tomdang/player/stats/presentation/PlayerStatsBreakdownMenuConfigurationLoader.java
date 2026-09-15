package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.player.stats.evaluation.PlayerStatContributionSource;
import java.io.Reader;
import java.util.*;

public class PlayerStatsBreakdownMenuConfigurationLoader {
	public PlayerStatsBreakdownMenuConfiguration load(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");
		ConfigurationSection root = section(YamlConfiguration.loadConfiguration(reader), "breakdown-menu", "Root");
		ConfigurationSection empty = section(root, "empty", "Breakdown menu");
		ConfigurationSection back = section(root, "back-button", "Breakdown menu");
		ConfigurationSection close = section(root, "close-button", "Breakdown menu");
		ConfigurationSection sourceSection = section(root, "sources", "Breakdown menu");
		EnumMap<PlayerStatContributionSource, PlayerStatContributionPresentation> sources = new EnumMap<>(PlayerStatContributionSource.class);
		for (String id : sourceSection.getKeys(false)) {
			PlayerStatContributionSource source;
			try { source = PlayerStatContributionSource.valueOf(id.toUpperCase(Locale.ROOT)); }
			catch (IllegalArgumentException exception) { throw new IllegalArgumentException("Unknown contribution source: " + id, exception); }
			ConfigurationSection entry = section(sourceSection, id, "Contribution source " + id);
			if (sources.put(source, new PlayerStatContributionPresentation(source, text(entry, "name", id), material(entry, "material", id), color(entry, "color", id))) != null) throw new IllegalArgumentException("Duplicate contribution source: " + source);
		}
		return new PlayerStatsBreakdownMenuConfiguration(text(root, "title-format", "Breakdown menu"), integer(root, "size", "Breakdown menu"), integer(root, "summary-slot", "Breakdown menu"),
				material(empty, "material", "Empty contribution"), text(empty, "name", "Empty contribution"), color(empty, "color", "Empty contribution"),
				material(back, "material", "Back button"), text(back, "name", "Back button"), color(back, "color", "Back button"), integer(back, "slot", "Back button"),
				material(close, "material", "Close button"), text(close, "name", "Close button"), color(close, "color", "Close button"), integer(close, "slot", "Close button"), sources);
	}
	private ConfigurationSection section(ConfigurationSection p, String k, String c) { if (!p.isConfigurationSection(k)) throw new IllegalArgumentException(c + " has a missing section: " + k); return p.getConfigurationSection(k); }
	private String text(ConfigurationSection s, String k, String c) { String v=s.getString(k); if(v==null||v.isBlank()) throw new IllegalArgumentException(c+" has a missing or blank "+k); return v; }
	private int integer(ConfigurationSection s, String k, String c) { if(!s.isInt(k)) throw new IllegalArgumentException(c+" has an invalid integer: "+k); return s.getInt(k); }
	private TextColor color(ConfigurationSection s,String k,String c){String v=text(s,k,c);if(!v.matches("^#[0-9a-fA-F]{6}$"))throw new IllegalArgumentException(c+" has an invalid color: "+v);return TextColor.fromHexString(v);}
	private Material material(ConfigurationSection s,String k,String c){String v=text(s,k,c);Material m=Material.matchMaterial(v.toUpperCase(Locale.ROOT));if(m==null||m==Material.AIR||m==Material.CAVE_AIR||m==Material.VOID_AIR)throw new IllegalArgumentException(c+" has an invalid material: "+v);return m;}
}
