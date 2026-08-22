package org.tomdang.mining.customminingability.miningspreadability;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.tomdang.TomBlock;
import org.tomdang.customabilityframework.AbilityTrigger;
import org.tomdang.customabilityframework.customability.AbilityExecutionContext;
import org.tomdang.mining.customminingability.AbilityMiningHandler;
import org.tomdang.mining.customminingability.CustomMiningAbility;
import org.tomdang.customabilityframework.activeability.ActiveAbilityService;
import org.tomdang.mining.customminingability.MiningAbilityContext;
import org.tomdang.mining.customminingability.MiningBlockReactiveAbility;

public class MiningSpreadAbility extends CustomMiningAbility implements MiningBlockReactiveAbility {

	private final ActiveAbilityService activeAbilityService;

	public MiningSpreadAbility(String abilityID, String abilityName, double energyCost, TomBlock instance,
							   AbilityTrigger abilityTrigger, long cooldownInTicks, Component abilityDescription,
							   ActiveAbilityService activeAbilityService
	) {
		super(abilityID, abilityName, energyCost, instance, abilityTrigger, cooldownInTicks, abilityDescription);
		this.activeAbilityService = activeAbilityService;
	}

	@Override
	public void execute(AbilityExecutionContext context) {
		Player player = context.getPlayer();

		activeAbilityService.activateAbility(player, this.getAbilityID(), 200);
		Component activationMessage = Component.text("[MINING SPREAD] Activated Mining Spread for 10 seconds", NamedTextColor.GOLD);
		player.sendMessage(activationMessage);

	}

	@Override
	public void onBlockMined(MiningAbilityContext context, AbilityMiningHandler handler) {
		Player player = context.getPlayer();

		Block block = context.getBlock();
		Material miningBlock = block.getType();

		// Define the 6 faces to inspect
		BlockFace[] faces = {
				BlockFace.UP,
				BlockFace.DOWN,
				BlockFace.NORTH,
				BlockFace.SOUTH,
				BlockFace.EAST,
				BlockFace.WEST
		};

		int localCounter = 0;

		for (BlockFace face : faces) {
			if (localCounter == 2) {
				break;
			}

			// Get the block touching that specific face
			Block relativeBlock = block.getRelative(face);
			Material relativeBlockMaterial = block.getRelative(face).getType();

			if (miningBlock.equals(relativeBlockMaterial)) {
				localCounter++;
				handler.applyMiningAbility(context, relativeBlock);
			}
		}

		player.sendMessage("Mining Spread reacted to a mined block!");

	}
}