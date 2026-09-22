package org.tomdang.player.playerdata;

import org.bukkit.configuration.file.YamlConfiguration;

public record PostgresConfiguration(
        boolean enabled,
        String jdbcUrl,
        String username,
        String password,
        int maximumPoolSize,
        long connectionTimeoutMs
) {
    public static PostgresConfiguration from(YamlConfiguration yaml, java.util.Map<String, String> environment) {
        boolean enabled = yaml.getBoolean("database.enabled", false);
        String url = required(yaml, "database.jdbc-url", "jdbc:postgresql://127.0.0.1:5432/tomblock");
        String username = required(yaml, "database.username", "tomblock");
        String passwordEnvironment = required(yaml, "database.password-environment", "TOMBLOCK_DB_PASSWORD");
        String password = environment.get(passwordEnvironment);
        if (enabled && (password == null || password.isBlank())) {
            throw new IllegalStateException("PostgreSQL is enabled but environment variable "
                    + passwordEnvironment + " is missing or blank");
        }
        int poolSize = yaml.getInt("database.maximum-pool-size", 4);
        long timeout = yaml.getLong("database.connection-timeout-ms", 5000L);
        if (poolSize < 1 || poolSize > 32) throw new IllegalArgumentException("maximum-pool-size must be between 1 and 32");
        if (timeout < 250 || timeout > 60000) throw new IllegalArgumentException("connection-timeout-ms must be between 250 and 60000");
        return new PostgresConfiguration(enabled, url, username, password, poolSize, timeout);
    }

    private static String required(YamlConfiguration yaml, String path, String fallback) {
        String value = yaml.getString(path, fallback);
        if (value == null || value.isBlank()) throw new IllegalArgumentException(path + " cannot be blank");
        return value.trim();
    }
}
