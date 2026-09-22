package org.tomdang.player.playerdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class PostgresConfigurationTest {
    @Test
    void databaseIsDisabledByDefaultWithoutASecret() {
        PostgresConfiguration configuration = PostgresConfiguration.from(new YamlConfiguration(), Map.of());
        assertFalse(configuration.enabled());
        assertEquals("jdbc:postgresql://127.0.0.1:5432/tomblock", configuration.jdbcUrl());
    }

    @Test
    void enabledDatabaseRequiresPasswordEnvironmentVariable() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("database.enabled", true);
        assertThrows(IllegalStateException.class, () -> PostgresConfiguration.from(yaml, Map.of()));
    }

    @Test
    void readsSecretWithoutPuttingItInYaml() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("database.enabled", true);
        yaml.set("database.password-environment", "TEST_DB_PASSWORD");
        PostgresConfiguration configuration = PostgresConfiguration.from(
                yaml, Map.of("TEST_DB_PASSWORD", "local-test-secret"));
        assertEquals("local-test-secret", configuration.password());
    }
}
