package org.tomdang.hud.composition;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/** Data-driven capacity policy. A capacity of zero suppresses a region on that layout profile. */
public final class HudLayoutPolicy {
	private final Map<HudRegion, HudRegionLayout> regions;
	private final Map<HudElementId, HudElementLayout> elements;

	public HudLayoutPolicy(Map<HudRegion, HudRegionLayout> regions) {
		this(regions, Map.of());
	}

	public HudLayoutPolicy(Map<HudRegion, HudRegionLayout> regions, Map<HudElementId, HudElementLayout> elements) {
		if (regions == null) throw new IllegalArgumentException("HUD layouts cannot be null");
		if (elements == null) throw new IllegalArgumentException("HUD element layouts cannot be null");
		EnumMap<HudRegion, HudRegionLayout> copy = new EnumMap<>(HudRegion.class);
		for (HudRegion region : HudRegion.values()) {
			copy.put(region, regions.getOrDefault(region, defaults(region)));
		}
		this.regions = Map.copyOf(copy);
		this.elements = Map.copyOf(elements);
	}

	public HudRegionLayout region(HudRegion region) { return regions.get(region); }
	public int capacity(HudRegion region) { return region(region).capacity(); }
	public Map<HudRegion, HudRegionLayout> regions() { return regions; }
	public Optional<HudElementLayout> element(HudElementId id) { return Optional.ofNullable(elements.get(id)); }
	public Map<HudElementId, HudElementLayout> elements() { return elements; }

	private static HudRegionLayout defaults(HudRegion region) {
		return switch (region) {
			case STATUS -> new HudRegionLayout(HudAnchor.BOTTOM_CENTER, 0, -8, 280, 3,
					org.tomdang.hud.composition.layout.HudAxis.VERTICAL, 2,
					org.tomdang.hud.composition.layout.HudAlignment.CENTER);
			case DIALOGUE -> new HudRegionLayout(HudAnchor.BOTTOM_CENTER, 0, -32, 260, 1,
					org.tomdang.hud.composition.layout.HudAxis.VERTICAL, 0,
					org.tomdang.hud.composition.layout.HudAlignment.CENTER);
			case QUEST_TRACKER -> new HudRegionLayout(HudAnchor.TOP_RIGHT, -8, 8, 180, 2,
					org.tomdang.hud.composition.layout.HudAxis.VERTICAL, 4,
					org.tomdang.hud.composition.layout.HudAlignment.END);
			case NOTIFICATION -> new HudRegionLayout(HudAnchor.TOP_CENTER, 0, 8, 240, 3,
					org.tomdang.hud.composition.layout.HudAxis.VERTICAL, 2,
					org.tomdang.hud.composition.layout.HudAlignment.CENTER);
			case BOSS -> new HudRegionLayout(HudAnchor.TOP_CENTER, 0, 28, 260, 2,
					org.tomdang.hud.composition.layout.HudAxis.VERTICAL, 3,
					org.tomdang.hud.composition.layout.HudAlignment.CENTER);
			case WORLD_CONTEXT -> new HudRegionLayout(HudAnchor.CENTER, 0, 36, 220, 1,
					org.tomdang.hud.composition.layout.HudAxis.VERTICAL, 0,
					org.tomdang.hud.composition.layout.HudAlignment.CENTER);
			case MAP -> new HudRegionLayout(HudAnchor.TOP_LEFT, 8, 8, 160, 1,
					org.tomdang.hud.composition.layout.HudAxis.VERTICAL, 0,
					org.tomdang.hud.composition.layout.HudAlignment.START);
			case DEBUG -> new HudRegionLayout(HudAnchor.TOP_RIGHT, -8, 8, 200, 1,
					org.tomdang.hud.composition.layout.HudAxis.VERTICAL, 0,
					org.tomdang.hud.composition.layout.HudAlignment.END);
		};
	}
}
