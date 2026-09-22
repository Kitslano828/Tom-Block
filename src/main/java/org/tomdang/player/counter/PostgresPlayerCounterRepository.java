package org.tomdang.player.counter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;

public final class PostgresPlayerCounterRepository implements PlayerCounterRepository {
    private final DataSource dataSource;

    public PostgresPlayerCounterRepository(DataSource dataSource) {
        if (dataSource == null) throw new IllegalArgumentException("dataSource cannot be null");
        this.dataSource = dataSource;
    }

    @Override
    public void register(CounterDefinition definition) {
        String sql = """
                INSERT INTO tomblock.counter_definitions
                    (counter_key, display_name, category, description, unit,
                     default_value, minimum_value, maximum_value, enabled)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (counter_key) DO UPDATE SET
                    display_name = EXCLUDED.display_name,
                    category = EXCLUDED.category,
                    description = EXCLUDED.description,
                    unit = EXCLUDED.unit,
                    default_value = EXCLUDED.default_value,
                    minimum_value = EXCLUDED.minimum_value,
                    maximum_value = EXCLUDED.maximum_value,
                    enabled = EXCLUDED.enabled,
                    updated_at = CURRENT_TIMESTAMP
                """;
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            writeDefinition(statement, definition);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw failure("register counter " + definition.key().value(), exception);
        }
    }

    @Override
    public Optional<CounterDefinition> findDefinition(CounterKey key) {
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM tomblock.counter_definitions WHERE counter_key = ?")) {
            statement.setString(1, key.value());
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(readDefinition(result)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw failure("find counter " + key.value(), exception);
        }
    }

    @Override
    public Collection<CounterDefinition> definitions() {
        var definitions = new ArrayList<CounterDefinition>();
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM tomblock.counter_definitions ORDER BY category, counter_key");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) definitions.add(readDefinition(result));
            return java.util.List.copyOf(definitions);
        } catch (SQLException exception) {
            throw failure("list counter definitions", exception);
        }
    }

    @Override
    public long get(UUID playerId, CounterKey key) {
        CounterDefinition definition = requireEnabled(key);
        String sql = "SELECT amount FROM tomblock.player_counters WHERE player_id = ? AND counter_key = ?";
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, playerId);
            statement.setString(2, key.value());
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong(1) : definition.defaultValue();
            }
        } catch (SQLException exception) {
            throw failure("read player counter " + key.value(), exception);
        }
    }

    @Override
    public long set(UUID playerId, CounterKey key, long amount) {
        CounterDefinition definition = requireEnabled(key);
        InMemoryPlayerCounterRepository.validateRange(definition, amount);
        String sql = """
                INSERT INTO tomblock.player_counters (player_id, counter_key, amount)
                VALUES (?, ?, ?)
                ON CONFLICT (player_id, counter_key) DO UPDATE SET
                    amount = EXCLUDED.amount,
                    updated_at = CURRENT_TIMESTAMP
                RETURNING amount
                """;
        try (var connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, playerId);
            statement.setString(2, key.value());
            statement.setLong(3, amount);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        } catch (SQLException exception) {
            throw failure("set player counter " + key.value(), exception);
        }
    }

    @Override
    public long increment(UUID playerId, CounterKey key, long delta) {
        CounterDefinition definition = requireEnabled(key);
        String sql = """
                INSERT INTO tomblock.player_counters (player_id, counter_key, amount)
                VALUES (?, ?, ?)
                ON CONFLICT (player_id, counter_key) DO UPDATE SET
                    amount = tomblock.player_counters.amount + ?,
                    updated_at = CURRENT_TIMESTAMP
                RETURNING amount
                """;
        long initial;
        try {
            initial = Math.addExact(definition.defaultValue(), delta);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Counter overflow", exception);
        }
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, playerId);
                statement.setString(2, key.value());
                statement.setLong(3, initial);
                statement.setLong(4, delta);
                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    long amount = result.getLong(1);
                    InMemoryPlayerCounterRepository.validateRange(definition, amount);
                    connection.commit();
                    return amount;
                }
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw failure("increment player counter " + key.value(), exception);
        }
    }

    private CounterDefinition requireEnabled(CounterKey key) {
        CounterDefinition definition = findDefinition(key)
                .orElseThrow(() -> new IllegalArgumentException("Unknown counter: " + key.value()));
        if (!definition.enabled()) throw new IllegalStateException("Counter is disabled: " + key.value());
        return definition;
    }

    private static void writeDefinition(PreparedStatement statement, CounterDefinition definition) throws SQLException {
        statement.setString(1, definition.key().value());
        statement.setString(2, definition.displayName());
        statement.setString(3, definition.category());
        statement.setString(4, definition.description());
        statement.setString(5, definition.unit());
        statement.setLong(6, definition.defaultValue());
        statement.setLong(7, definition.minimumValue());
        if (definition.maximumValue() == null) statement.setNull(8, Types.BIGINT);
        else statement.setLong(8, definition.maximumValue());
        statement.setBoolean(9, definition.enabled());
    }

    private static CounterDefinition readDefinition(ResultSet result) throws SQLException {
        long maximum = result.getLong("maximum_value");
        return new CounterDefinition(
                CounterKey.of(result.getString("counter_key")),
                result.getString("display_name"), result.getString("category"),
                result.getString("description"), result.getString("unit"),
                result.getLong("default_value"), result.getLong("minimum_value"),
                result.wasNull() ? null : maximum, result.getBoolean("enabled"));
    }

    private static IllegalStateException failure(String action, SQLException exception) {
        return new IllegalStateException("Failed to " + action, exception);
    }
}
