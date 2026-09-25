package org.tomdang.quest.presentation;
import org.bukkit.configuration.file.YamlConfiguration;import java.io.*;import java.nio.charset.StandardCharsets;
public final class QuestOfferConfigurationLoader{
	public QuestOfferRegistry load(InputStream input){if(input==null)throw new IllegalArgumentException("Quest offer input cannot be null");var yaml=YamlConfiguration.loadConfiguration(new InputStreamReader(input,StandardCharsets.UTF_8));var root=yaml.getConfigurationSection("offers");if(root==null)throw new IllegalArgumentException("Missing offers section");var result=new QuestOfferRegistry();for(String id:root.getKeys(false)){var section=root.getConfigurationSection(id);if(section==null)throw new IllegalArgumentException("Offer "+id+" must be a section");result.register(new QuestOfferDefinition(required(section,"actor"),required(section,"quest")));}return result;}
	private String required(org.bukkit.configuration.ConfigurationSection section,String key){String value=section.getString(key);if(value==null||value.isBlank())throw new IllegalArgumentException("Missing "+key);return value;}
}
