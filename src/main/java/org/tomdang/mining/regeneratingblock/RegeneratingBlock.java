package org.tomdang.mining.regeneratingblock;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Location;

public class RegeneratingBlock {
	@Getter
	private Material originalMaterial;
	@Getter
	private long respawnDelayInTicks;
	@Getter
	private Material replacementMaterial;

	public RegeneratingBlock(Material originalMaterial, long respawnDelayInTicks) {
		this.originalMaterial = originalMaterial;
		this.respawnDelayInTicks = respawnDelayInTicks;
		this.replacementMaterial = Material.BEDROCK;
	}
}
