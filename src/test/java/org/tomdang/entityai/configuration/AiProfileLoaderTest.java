package org.tomdang.entityai.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class AiProfileLoaderTest {
    @Test void loadsOrderedDataDrivenGoals() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString("""
                ai:
                  navigator: direct_flight
                  goals:
                    FLEE: {type: FLEE_FROM_PLAYER, priority: 100, speed: 1.4}
                    IDLE: {type: IDLE, priority: 0}
                """);
        AiProfile profile = new AiProfileLoader().load(yaml.getConfigurationSection("ai"));
        assertEquals("DIRECT_FLIGHT", profile.navigator());
        assertEquals(2, profile.goals().size());
        assertEquals("1.4", profile.goals().get(0).parameters().get("speed"));
    }

    @Test void rejectsUnknownNavigatorAndGoalType() throws Exception {
        YamlConfiguration navigator = new YamlConfiguration();
        navigator.loadFromString("ai: {navigator: TELEPORT, goals: {IDLE: {type: IDLE, priority: 0}}}");
        assertThrows(IllegalArgumentException.class,
                () -> new AiProfileLoader().load(navigator.getConfigurationSection("ai")));

        YamlConfiguration goal = new YamlConfiguration();
        goal.loadFromString("ai: {navigator: DIRECT_FLIGHT, goals: {BAD: {type: MAGIC, priority: 1}}}");
        assertThrows(IllegalArgumentException.class,
                () -> new AiProfileLoader().load(goal.getConfigurationSection("ai")));
    }
}
