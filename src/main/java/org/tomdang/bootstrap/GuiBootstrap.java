package org.tomdang.bootstrap;

import org.bukkit.plugin.Plugin;
import org.tomdang.guiframework.GuiListener;
import org.tomdang.guiframework.GuiRegistry;
import org.tomdang.guiframework.GuiService;
import org.tomdang.guiframework.GuiSessionRegistry;

/** Owns the shared GUI runtime. Feature bootstraps register screens through the registry. */
public final class GuiBootstrap implements AutoCloseable {
    private final GuiRegistry registry = new GuiRegistry();
    private final GuiService service;

    public GuiBootstrap(Plugin plugin) {
        if (plugin == null) throw new IllegalArgumentException("Plugin is required");
        service = new GuiService(plugin, registry, new GuiSessionRegistry());
        plugin.getServer().getPluginManager().registerEvents(new GuiListener(service), plugin);
    }

    public GuiRegistry registry() { return registry; }
    public GuiService service() { return service; }

    @Override
    public void close() { service.close(); }
}
