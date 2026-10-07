package org.tomdang.worldtime;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class WorldCalendarConfigurationLoader {
    public WorldCalendarSettings load(InputStream input) {
        if (input == null) throw new IllegalArgumentException("World calendar resource is required");
        var yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));
        ConfigurationSection root = require(yaml, "calendar");
        var months = new ArrayList<CalendarMonth>();
        for (var raw : root.getMapList("months")) months.add(new CalendarMonth(
                String.valueOf(raw.get("name")), Season.valueOf(String.valueOf(raw.get("season")))));
        ConfigurationSection initial = require(root, "initial-date");
        return new WorldCalendarSettings(
                root.getLong("real-seconds-per-day") * 1000,
                root.getLong("daylight-real-seconds") * 1000,
                root.getInt("days-per-month"), months,
                new GameDate(initial.getLong("year"), initial.getInt("month"), initial.getInt("day")),
                initial.getInt("hour"), initial.getInt("minute"),
                root.getLong("synchronization-interval-ticks", 20));
    }

    private static ConfigurationSection require(ConfigurationSection parent, String path) {
        ConfigurationSection value = parent.getConfigurationSection(path);
        if (value == null) throw new IllegalArgumentException("Missing world calendar section " + path);
        return value;
    }
}
