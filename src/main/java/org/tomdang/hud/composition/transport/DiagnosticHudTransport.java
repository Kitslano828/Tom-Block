package org.tomdang.hud.composition.transport;

import org.tomdang.hud.composition.*;
import org.tomdang.hud.composition.draw.PositionedHudCommand;
import java.util.*;
import java.util.function.Consumer;

/** Human-readable frame sink for tests and future diagnostic commands. */
public final class DiagnosticHudTransport implements HudTransport {
	private final Consumer<String> sink;
	public DiagnosticHudTransport(Consumer<String> sink) { this.sink = Objects.requireNonNull(sink); }
	@Override public Set<HudRegion> regions() { return EnumSet.allOf(HudRegion.class); }
	@Override public void apply(HudFrame frame) { sink.accept(describe(frame)); }
	@Override public void clear(UUID playerId) { sink.accept("CLEAR " + playerId); }

	public String describe(HudFrame frame) {
		StringBuilder result = new StringBuilder("HUD ").append(frame.playerId()).append(" revision=")
				.append(frame.revision()).append(" viewport=").append(frame.viewport().width()).append('x')
				.append(frame.viewport().height()).append(" scale=").append(frame.viewport().guiScale());
		for (HudRegion region : HudRegion.values()) {
			List<HudElementSnapshot> elements = frame.region(region);
			if (elements.isEmpty()) continue;
			result.append('\n').append(region);
			for (HudElementSnapshot element : elements) {
				result.append("\n  ").append(element.id()).append(" ").append(element.mode())
						.append(" @ ").append(element.bounds());
				for (PositionedHudCommand command : frame.commands().stream()
						.filter(value -> value.owner().equals(element.id())).toList())
					result.append("\n    ").append(command.command().getClass().getSimpleName())
							.append(" @ ").append(command.bounds());
			}
		}
		return result.toString();
	}
}
