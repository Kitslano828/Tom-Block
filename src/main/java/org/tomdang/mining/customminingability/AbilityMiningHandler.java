package org.tomdang.mining.customminingability;

import org.bukkit.block.Block;

public interface AbilityMiningHandler {

	//Process one additional world block selected by a mining ability.
	void applyMiningAbility(MiningAbilityContext context, Block additionalBlock);

}
