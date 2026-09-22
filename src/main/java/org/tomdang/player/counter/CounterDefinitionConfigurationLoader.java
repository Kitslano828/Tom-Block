package org.tomdang.player.counter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class CounterDefinitionConfigurationLoader {
    public List<CounterDefinition> load(InputStream input) {
        if (input == null) throw new IllegalArgumentException("Counter definition input cannot be null");
        YamlConfiguration yaml;
        try (input) {
            yaml = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read counter definitions", exception);
        }
        ConfigurationSection counters = yaml.getConfigurationSection("counters");
        if (counters == null) throw new IllegalArgumentException("Missing counters section");

        var definitions = new ArrayList<CounterDefinition>();
        for (String rawKey : counters.getKeys(false)) {
            String path = "counters." + rawKey;
            ConfigurationSection section = yaml.getConfigurationSection(path);
            if (section == null) throw new IllegalArgumentException(path + " must be a section");
            definitions.add(new CounterDefinition(
                    CounterKey.of(rawKey),
                    required(section, "display-name"),
                    required(section, "category"),
                    section.getString("description", ""),
                    section.getString("unit", "COUNT"),
                    section.getLong("default", 0),
                    section.getLong("minimum", 0),
                    section.contains("maximum") ? section.getLong("maximum") : null,
                    section.getBoolean("enabled", true)
            ));
        }
        return List.copyOf(definitions);
    }

    private static String required(ConfigurationSection section, String key) {
        String value = section.getString(key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException(section.getCurrentPath() + "." + key + " cannot be blank");
        return value;
    }
}
