package org.tomdang.player.playerdata;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;

/** Owns the PostgreSQL pool and applies all versioned schema migrations. */
public final class PostgresDatabase implements AutoCloseable {
    private final HikariDataSource dataSource;

    public PostgresDatabase(PostgresConfiguration configuration) {
        migrate(configuration);

        HikariConfig hikari = new HikariConfig();
        // Paper uses an isolated plugin class loader, so JDBC service discovery
        // alone is not reliable even though the driver is bundled in the JAR.
        hikari.setDriverClassName("org.postgresql.Driver");
        hikari.setJdbcUrl(configuration.jdbcUrl());
        hikari.setUsername(configuration.username());
        hikari.setPassword(configuration.password());
        hikari.setMaximumPoolSize(configuration.maximumPoolSize());
        hikari.setMinimumIdle(1);
        hikari.setConnectionTimeout(configuration.connectionTimeoutMs());
        hikari.setPoolName("TomBlock-PostgreSQL");
        dataSource = new HikariDataSource(hikari);
    }

    private static void migrate(PostgresConfiguration configuration) {
        Flyway.configure(PostgresDatabase.class.getClassLoader())
                .dataSource(configuration.jdbcUrl(), configuration.username(), configuration.password())
                .defaultSchema("tomblock")
                .schemas("tomblock")
                .createSchemas(true)
                // Databases created by the prototype already contain V1's two
                // tables. Baseline them at V1, then apply V2 and later changes.
                .baselineOnMigrate(true)
                .baselineVersion(MigrationVersion.fromVersion("1"))
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    public DataSource dataSource() {
        return dataSource;
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
