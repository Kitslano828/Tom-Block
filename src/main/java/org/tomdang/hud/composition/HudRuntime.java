package org.tomdang.hud.composition;

import java.util.List;
import java.util.EnumSet;
import java.util.UUID;

/** Single mutation boundary for HUD features. */
public final class HudRuntime implements AutoCloseable {
	private final PlayerHudSessionRegistry sessions;
	private final HudCompositor compositor;
	private final List<HudTransport> transports;
	private final HudViewportProvider viewports;

	public HudRuntime(PlayerHudSessionRegistry sessions, HudCompositor compositor, List<HudTransport> transports) {
		this(sessions, compositor, transports, ignored -> HudViewport.DEFAULT);
	}
	public HudRuntime(PlayerHudSessionRegistry sessions, HudCompositor compositor, List<HudTransport> transports,
	                 HudViewportProvider viewports) {
		this.sessions = java.util.Objects.requireNonNull(sessions);
		this.compositor = java.util.Objects.requireNonNull(compositor);
		this.viewports = java.util.Objects.requireNonNull(viewports);
		if (transports == null || transports.stream().anyMatch(java.util.Objects::isNull))
			throw new IllegalArgumentException("HUD transports cannot be null");
		this.transports = List.copyOf(transports);
		EnumSet<HudRegion> claimed = EnumSet.noneOf(HudRegion.class);
		for (HudTransport transport : this.transports) for (HudRegion region : transport.regions()) {
			if (!claimed.add(region)) throw new IllegalArgumentException("Multiple HUD transports own " + region);
		}
	}

	public void open(UUID playerId) { sessions.open(playerId); }
	public void show(UUID playerId, HudElement element, long tick) {
		PlayerHudSession session = sessions.open(playerId);
		session.put(element);
		apply(session, tick);
	}
	public void hide(UUID playerId, HudElementId id, long tick) {
		sessions.find(playerId).ifPresent(session -> { session.remove(id); apply(session, tick); });
	}
	public void refresh(UUID playerId, long tick) {
		sessions.find(playerId).ifPresent(session -> apply(session, tick));
	}
	public void close(UUID playerId) {
		transports.forEach(transport -> transport.clear(playerId));
		sessions.close(playerId);
	}
	private void apply(PlayerHudSession session, long tick) {
		HudViewport viewport = java.util.Objects.requireNonNull(viewports.viewport(session.playerId()), "HUD viewport provider returned null");
		HudFrame frame = compositor.compose(session, tick, viewport);
		transports.forEach(transport -> transport.apply(frame));
	}
	@Override public void close() {
		for (PlayerHudSession session : sessions.all()) close(session.playerId());
		for (HudTransport transport : transports) {
			try { transport.close(); }
			catch (Exception exception) { throw new IllegalStateException("Could not close HUD transport", exception); }
		}
	}
}
