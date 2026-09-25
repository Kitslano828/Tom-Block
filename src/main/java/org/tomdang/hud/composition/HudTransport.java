package org.tomdang.hud.composition;

import java.util.UUID;
import java.util.Set;

/** Reconciles frames onto one Minecraft carrier. Implementations own all packets/API objects they create. */
public interface HudTransport extends AutoCloseable {
	/** Logical regions exclusively owned by this transport. */
	Set<HudRegion> regions();
	void apply(HudFrame frame);
	void clear(UUID playerId);
	@Override default void close() {}
}
