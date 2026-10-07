package org.tomdang.hud.composition.layout;

import org.tomdang.hud.composition.*;
import org.tomdang.hud.composition.draw.*;

import java.util.ArrayList;
import java.util.List;

/** Measures layout trees, resolves anchors, and emits deterministic absolute draw commands. */
public final class HudLayoutEngine {
	private final HudTextMetrics textMetrics;
	public HudLayoutEngine(HudTextMetrics textMetrics) { this.textMetrics = java.util.Objects.requireNonNull(textMetrics); }

	public HudSize measure(HudNode node) {
		return switch (node) {
			case HudPrimitive primitive -> measure(primitive.command());
			case HudPadding padding -> {
				HudSize child = measure(padding.child());
				yield new HudSize(child.width() + padding.insets().horizontal(), child.height() + padding.insets().vertical());
			}
			case HudTranslate translated -> measure(translated.child());
			case HudClip clip -> new HudSize(clip.width(), clip.height());
			case HudOverlay overlay -> overlay.children().stream().map(this::measure)
					.reduce(HudSize.ZERO, (a, b) -> new HudSize(Math.max(a.width(), b.width()), Math.max(a.height(), b.height())));
			case HudStack stack -> measureStack(stack);
		};
	}

	public List<PositionedHudCommand> layout(HudElementSnapshot snapshot, HudPoint origin, int zIndex) {
		List<PositionedHudCommand> result = new ArrayList<>();
		place(snapshot.content().root(), origin.x(), origin.y(), null, snapshot, zIndex, result);
		return List.copyOf(result);
	}

	public HudPoint anchor(HudViewport viewport, HudRegionLayout region, HudSize content) {
		int left = viewport.safeArea().left();
		int right = viewport.width() - viewport.safeArea().right();
		int top = viewport.safeArea().top();
		int bottom = viewport.height() - viewport.safeArea().bottom();
		int x = switch (region.anchor()) {
			case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> left;
			case TOP_CENTER, CENTER, BOTTOM_CENTER -> (left + right - content.width()) / 2;
			case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> right - content.width();
		};
		int y = switch (region.anchor()) {
			case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> top;
			case CENTER_LEFT, CENTER, CENTER_RIGHT -> (top + bottom - content.height()) / 2;
			case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> bottom - content.height();
		};
		return new HudPoint(x + region.offsetX(), y + region.offsetY());
	}

	private HudSize measure(HudDrawCommand command) {
		return switch (command) {
			case HudTextCommand text -> textMetrics.measure(text);
			case HudImageCommand image -> new HudSize(image.width(), image.height());
			case HudProgressBarCommand bar -> new HudSize(bar.width(), bar.height());
			case HudPanelCommand panel -> new HudSize(panel.width(), panel.height());
			case HudComponentCommand component -> new HudSize(component.width(), component.height());
		};
	}

	private HudSize measureStack(HudStack stack) {
		List<HudSize> sizes = stack.children().stream().map(this::measure).toList();
		int gaps = Math.max(0, sizes.size() - 1) * stack.gap();
		if (stack.axis() == HudAxis.VERTICAL)
			return new HudSize(sizes.stream().mapToInt(HudSize::width).max().orElse(0),
					sizes.stream().mapToInt(HudSize::height).sum() + gaps);
		return new HudSize(sizes.stream().mapToInt(HudSize::width).sum() + gaps,
				sizes.stream().mapToInt(HudSize::height).max().orElse(0));
	}

	private void place(HudNode node, int x, int y, HudRect clip, HudElementSnapshot owner, int z,
	                   List<PositionedHudCommand> output) {
		switch (node) {
			case HudPrimitive primitive -> {
				HudSize size = measure(primitive);
				output.add(new PositionedHudCommand(owner.id(), owner.region(), z,
						new HudRect(x, y, size.width(), size.height()), clip, primitive.command()));
			}
			case HudPadding padding -> place(padding.child(), x + padding.insets().left(),
					y + padding.insets().top(), clip, owner, z, output);
			case HudTranslate translated -> place(translated.child(), x + translated.x(),
					y + translated.y(), clip, owner, z, output);
			case HudClip clipping -> {
				HudRect ownClip = new HudRect(x, y, clipping.width(), clipping.height());
				place(clipping.child(), x, y, intersect(clip, ownClip), owner, z, output);
			}
			case HudOverlay overlay -> {
				HudSize parent = measure(overlay);
				for (HudNode child : overlay.children()) {
					HudSize size = measure(child);
					place(child, x + align(parent.width(), size.width(), overlay.horizontal()),
							y + align(parent.height(), size.height(), overlay.vertical()), clip, owner, z++, output);
				}
			}
			case HudStack stack -> placeStack(stack, x, y, clip, owner, z, output);
		}
	}

	private void placeStack(HudStack stack, int x, int y, HudRect clip, HudElementSnapshot owner, int z,
	                        List<PositionedHudCommand> output) {
		HudSize parent = measure(stack);
		int cursor = 0;
		for (HudNode child : stack.children()) {
			HudSize size = measure(child);
			int childX = stack.axis() == HudAxis.VERTICAL ? x + align(parent.width(), size.width(), stack.alignment()) : x + cursor;
			int childY = stack.axis() == HudAxis.HORIZONTAL ? y + align(parent.height(), size.height(), stack.alignment()) : y + cursor;
			place(child, childX, childY, clip, owner, z, output);
			cursor += (stack.axis() == HudAxis.VERTICAL ? size.height() : size.width()) + stack.gap();
		}
	}

	private int align(int parent, int child, HudAlignment alignment) {
		return switch (alignment) { case START -> 0; case CENTER -> (parent - child) / 2; case END -> parent - child; };
	}
	private HudRect intersect(HudRect first, HudRect second) {
		if (first == null) return second;
		int x = Math.max(first.x(), second.x()), y = Math.max(first.y(), second.y());
		return new HudRect(x, y, Math.max(0, Math.min(first.right(), second.right()) - x),
				Math.max(0, Math.min(first.bottom(), second.bottom()) - y));
	}
}
