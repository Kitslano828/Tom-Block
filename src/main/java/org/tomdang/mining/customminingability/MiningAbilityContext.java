package org.tomdang.mining.customminingability;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.tomdang.mining.miningblock.MiningBlock;
import org.tomdang.mining.miningtool.MiningTool;
import org.tomdang.player.PlayerProfile;

public class MiningAbilityContext {

	@Getter
	private final Player player;
	@Getter
	private final PlayerProfile playerProfile;
	@Getter
	private final Block block;
	@Getter
	private final MiningBlock blockDefinition;
	@Getter
	private final MiningTool miningTool;

	public MiningAbilityContext(Player player, PlayerProfile playerProfile, Block block, MiningBlock blockDefinition, MiningTool miningTool) {
		this.player = player;
		this.playerProfile = playerProfile;
		this.block = block;
		this.blockDefinition = blockDefinition;
		this.miningTool = miningTool;
	}
}
