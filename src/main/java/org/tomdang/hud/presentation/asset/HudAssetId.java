package org.tomdang.hud.presentation.asset;

public record HudAssetId(String namespace, String value) implements Comparable<HudAssetId> {
	public HudAssetId {
		if (!valid(namespace) || !valid(value)) throw new IllegalArgumentException("Invalid HUD asset id");
	}
	public static HudAssetId of(String namespace, String value) { return new HudAssetId(namespace, value); }
	private static boolean valid(String value) { return value != null && value.matches("[a-z0-9._/-]+"); }
	@Override public String toString() { return namespace + ":" + value; }
	@Override public int compareTo(HudAssetId other) { return toString().compareTo(other.toString()); }
}
