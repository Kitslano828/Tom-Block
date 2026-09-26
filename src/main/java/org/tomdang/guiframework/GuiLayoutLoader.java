package org.tomdang.guiframework;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class GuiLayoutLoader {
    public GuiLayout load(InputStream input) {
        if (input == null) throw new IllegalArgumentException("GUI layout input is required");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));
        ConfigurationSection menu = yaml.getConfigurationSection("menu");
        if (menu == null) throw new IllegalArgumentException("GUI layout is missing menu section");
        String title = requireText(menu, "title");
        int size = menu.getInt("size", -1);
        ConfigurationSection configuredSlots = menu.getConfigurationSection("slots");
        if (configuredSlots == null) throw new IllegalArgumentException("GUI layout is missing menu.slots");
        Map<String, Integer> slots = new LinkedHashMap<>();
        Set<Integer> occupied = new HashSet<>();
        for (String key : configuredSlots.getKeys(false)) {
            if (!configuredSlots.isInt(key)) throw new IllegalArgumentException("GUI slot " + key + " must be an integer");
            int slot = configuredSlots.getInt(key);
            if (slot < 0 || slot >= size) throw new IllegalArgumentException("GUI slot " + key + " is outside menu size: " + slot);
            if (!occupied.add(slot)) throw new IllegalArgumentException("GUI layout assigns slot " + slot + " more than once");
            slots.put(key, slot);
        }
        return new GuiLayout(title, size, slots);
    }

    private String requireText(ConfigurationSection section, String path) {
        String value = section.getString(path);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("GUI layout is missing menu." + path);
        return value;
    }
}
