package org.tomdang.island.preset;

public record IslandInteractionPolicy(
		IslandAccessPolicy placement,
		IslandAccessPolicy playerPlacedBreaking,
		IslandAccessPolicy terrainBreaking,
		IslandAccessPolicy registeredResources,
		boolean explosions,
		boolean pistons,
		boolean fluidModification) { }
