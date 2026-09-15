package org.tomdang.customitemframework.refresh;

public record PlayerInventoryItemRefreshResult(int inspected, int updated, int skipped, int failed) {

	public PlayerInventoryItemRefreshResult {
		if (inspected < 0) throw new IllegalArgumentException("inspected cannot be negative");
		if (updated < 0) throw new IllegalArgumentException("updated cannot be negative");
		if (skipped < 0) throw new IllegalArgumentException("skipped cannot be negative");
		if (failed < 0) throw new IllegalArgumentException("failed cannot be negative");
		if (updated + skipped + failed != inspected) {
			throw new IllegalArgumentException("updated, skipped, and failed must account for every inspected slot");
		}
	}
}
