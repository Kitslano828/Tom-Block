package org.tomdang.worldtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public final class FileWorldClockStateStore implements WorldClockStateStore {
    private final Path path;
    public FileWorldClockStateStore(Path path) {
        if (path == null) throw new IllegalArgumentException("State path is required");
        this.path = path;
    }

    @Override public WorldClockState load(WorldClockState fallback) {
        if (!Files.isRegularFile(path)) return fallback;
        Properties values = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            values.load(input);
            return new WorldClockState(
                    Double.parseDouble(values.getProperty("base-virtual-millis")),
                    Long.parseLong(values.getProperty("anchor-real-millis")),
                    Boolean.parseBoolean(values.getProperty("paused")),
                    Double.parseDouble(values.getProperty("speed")));
        } catch (IOException | IllegalArgumentException exception) {
            throw new IllegalStateException("Could not load world calendar state from " + path, exception);
        }
    }

    @Override public void save(WorldClockState state) {
        Properties values = new Properties();
        values.setProperty("base-virtual-millis", Double.toString(state.baseVirtualMillis()));
        values.setProperty("anchor-real-millis", Long.toString(state.anchorRealMillis()));
        values.setProperty("paused", Boolean.toString(state.paused()));
        values.setProperty("speed", Double.toString(state.speed()));
        try {
            Files.createDirectories(path.getParent());
            Path temporary = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");
            try (OutputStream output = Files.newOutputStream(temporary)) { values.store(output, "TomBlock global world calendar"); }
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save world calendar state to " + path, exception);
        }
    }
}
