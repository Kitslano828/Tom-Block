package org.tomdang.mining;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.tomdang.customabilityframework.activeability.ActiveAbilityService;
import org.tomdang.customabilityframework.customability.AbilityExecutionContext;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.mining.customminingability.AbilityMiningHandler;
import org.tomdang.mining.customminingability.MiningAbilityContext;
import org.tomdang.mining.customminingability.MiningBlockReactiveAbility;
import org.tomdang.mining.miningblock.MiningBlock;
import org.tomdang.mining.miningblock.MiningBlockRegistry;
import org.tomdang.mining.miningdrops.MiningDrop;
import org.tomdang.mining.mininglevel.MiningLevel;
import org.tomdang.mining.miningstats.MiningFortune;
import org.tomdang.mining.miningtool.MiningTool;
import org.tomdang.mining.miningtool.MiningToolResolver;
import org.tomdang.mining.regeneratingblock.RegeneratingBlock;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.tomdang.player.playeractionbar.PlayerActionBarService;
import org.tomdang.player.playerresource.PlayerStatsService;

import java.util.*;

public class MiningService implements AbilityMiningHandler {

	private final MiningFortune miningFortune;
	private final PlayerProfileService playerProfileService;
	private final MiningBlockRegistry miningBlockRegistry;
	private final MiningLevel miningLevel;
	private final MiningToolResolver miningToolResolver;
	private final MiningRegenerationService miningRegenerationService;
	private final PlayerActionBarService playerActionBarService;
	private final PlayerStatsService playerStatsService;
	private final ActiveAbilityService activeAbilityService;


	public MiningService(PlayerProfileService playerProfileService, MiningBlockRegistry miningBlockRegistry,
						 MiningLevel miningLevel, MiningToolResolver miningToolResolver, MiningFortune miningFortune,
						MiningRegenerationService miningRegenerationService, PlayerActionBarService playerActionBarService,
						 PlayerStatsService playerStatsService, ActiveAbilityService activeAbilityService) {
		this.playerProfileService = playerProfileService;
		this.miningBlockRegistry = miningBlockRegistry;
		this.miningLevel = miningLevel;
		this.miningToolResolver = miningToolResolver;
		this.miningFortune = miningFortune;
		this.miningRegenerationService = miningRegenerationService;
		this.playerActionBarService = playerActionBarService;
		this.playerStatsService = playerStatsService;
		this.activeAbilityService = activeAbilityService;
	}

	public void blockBreak(BlockBreakEvent event, UUID uuid) {
		Material block = event.getBlock().getType();
		if (playerProfileService.playerAlreadyExist(uuid)) {
			PlayerProfile player = playerProfileService.getPlayerProfileFromMap(uuid);
			if (miningBlockRegistry.blockInRegistry(block)) {
				MiningBlock miningBlock = miningBlockRegistry.getMiningBlock(block);
				miningBlock(event, miningBlock, player);
			} else {
				Component text = Component.text("THIS IS NOT A VALID MINING BLOCK!").decoration(TextDecoration.BOLD, true).color(TextColor.color(Color.RED.asARGB()));
				playerActionBarService.showTemporaryMessage(event.getPlayer(), text, 40);
			}
		} else {
			System.out.println("Player Does Not Exist");
		}
	}

	public void miningBlock(BlockBreakEvent event, MiningBlock block, PlayerProfile playerProfile) {
		ItemStack heldItem = event.getPlayer().getInventory().getItemInMainHand();
		MiningTool miningTool = miningToolResolver.getMiningTool(heldItem);
		if (miningTool == null) {
			if (event.getPlayer().getGameMode() != GameMode.CREATIVE) { // to be able to actually break the block
				event.setCancelled(true);
				event.getPlayer().sendMessage("§4YOU ARE NOT HOLDING A MINING TOOL");
			}
		} else if (checkBreakingPower(miningTool, block)) {
			event.setCancelled(true);
			event.setDropItems(false);
			notifyActiveMiningAbilities(event.getPlayer(),playerProfile,event.getBlock(),block,miningTool);
			processMinedBlock(event.getPlayer(), playerProfile, event.getBlock(), block, miningTool);
		} else {
			event.setCancelled(true);
			event.getPlayer().sendMessage("§4YOU DON'T HAVE THE BREAKING POWER TO BREAK THIS BLOCK!");
		}
	}

	public void scheduleBlockRegeneration(Block block, MiningBlock miningBlock) {
		Material materialBlock = block.getType();
		RegeneratingBlock regeneratingBlock = new RegeneratingBlock(materialBlock, miningBlock.getRegenerationTime());
		miningRegenerationService.scheduleRegeneration(regeneratingBlock, block);
	}

	// Drops before Mining Fortune
	public void givePlayerDrops(Player player, MiningBlock block, MiningTool miningTool) {
		for (MiningDrop miningDrop : block.getBlockDrops()) {
			if (miningDrop.rollForDrop()) {
				if (player.getInventory().firstEmpty() == -1) {
						player.sendMessage("YOUR INVENTORY IS FULL!");
					player	.dropItem(miningDrop.getItemDrops(miningDrop.getItem(), miningDrop.getAmount()));
					if (miningDrop.isAffectedByFortune()) {
						handleMiningFortune(player, miningDrop, miningTool);
					} else {
						player.sendMessage("§b§l★YOU DROPPED A RARE ITEM! " + miningDrop.getItem().getI18NDisplayName() );
					}
				} else {
					player.getInventory().addItem(miningDrop.getItemDrops(miningDrop.getItem(), miningDrop.getAmount()));
					if (miningDrop.isAffectedByFortune()) {
						handleMiningFortune(player,miningDrop, miningTool);
					} else {
						player.sendMessage("§b§l★YOU DROPPED A RARE ITEM! " + miningDrop.getItem().getI18NDisplayName() );
					}
				}
			}
		}
	}

	public void handleMiningFortune(Player player, MiningDrop drop, MiningTool miningTool) {
		double totalFortune = playerStatsService.getTotalMiningFortune(player, miningTool);
		int guaranteedDrops = miningFortune.guaranteedDrops(totalFortune);

		// 1. Process guaranteed fortune drops
		if (guaranteedDrops > 0) {
			ItemStack guaranteedItem = drop.getItemDropsWithFortune(drop.getItem(), drop.getAmount(), guaranteedDrops);
			giveOrDropItem(player, guaranteedItem);

			player.sendMessage("§a§lYOU DROPPED " + guaranteedDrops + " ADDITIONAL DROPS FROM YOUR " + totalFortune + " TOTAL FORTUNE!");
		}

		// 2. Process chance-based fortune proc drop
		if (miningFortune.checkForMiningFortuneProc(totalFortune)) {
			ItemStack procItem = drop.getItemDrops(drop.getItem(), drop.getAmount());
			giveOrDropItem(player, procItem);

			// only shows to low level miners
			PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
			if (playerProfile.getMiningLVL() < 5) {
				player.sendMessage("");
				player.sendMessage("§a§lYOU DROPPED AN ADDITIONAL DROP FROM YOUR " + String.format("%.0f", totalFortune % 100) + "% FORTUNE CHANCE!");
			}
		}
	}

	/**
	 * Helper method to safely give item to player or drop it on the ground if full
	 */
	private void giveOrDropItem(Player player, ItemStack item) {
		// firstEmpty() returns -1 if the main inventory has no empty slots
		if (player.getInventory().firstEmpty() == -1) {
			player.dropItem(item);
		} else {
			player.getInventory().addItem(item);
		}
	}

	private boolean checkBreakingPower(MiningTool miningTool, MiningBlock block) {
		return miningTool.getBreakingPower() >= block.getBreakingPower();
	}

	private void updateMiningXP(PlayerProfile playerProfile, Player player, MiningBlock block) {
		miningLevel.updatePlayerMiningXP(block.getXp(), playerProfile);
		Component text = Component.text(playerProfile.getMiningXP() + " / " + miningLevel.getNextMiningLevel(playerProfile) + " Mining XP")
				.color(NamedTextColor.DARK_AQUA);
		playerActionBarService.showTemporaryMessage(player, text, 40);
		miningLevel.playerMiningLevelUp(player, playerProfile);
	}

	private void processMinedBlock(Player player, PlayerProfile playerProfile, Block worldBlock, MiningBlock blockDefinition, MiningTool miningTool) {
		scheduleBlockRegeneration(worldBlock, blockDefinition);
		updateMiningXP(playerProfile, player, blockDefinition);
		givePlayerDrops(player, blockDefinition, miningTool);
	}

	private void notifyActiveMiningAbilities(Player player, PlayerProfile playerProfile, Block block, MiningBlock blockDefinition, MiningTool miningTool) {
		MiningAbilityContext context = new MiningAbilityContext(player, playerProfile, block, blockDefinition, miningTool);

		for (CustomAbility ability : miningTool.getCustomAbilities()) {
			if (ability instanceof MiningBlockReactiveAbility) {
				if (activeAbilityService.isAbilityActive(player, ability.getAbilityID())) {
					((MiningBlockReactiveAbility) ability).onBlockMined(context, this);
				}
			}
		}
	}

	@Override
	public void applyMiningAbility(MiningAbilityContext context, Block additionalBlock) {
		Material additionBlockMaterial = additionalBlock.getType();

		if (!miningBlockRegistry.blockInRegistry(additionBlockMaterial)) {
			return;
		}

		MiningBlock miningBlock = miningBlockRegistry.getMiningBlock(additionBlockMaterial);
		MiningTool miningTool = context.getMiningTool();
		if (!checkBreakingPower(miningTool, miningBlock)) {
			return;
		}

		processMinedBlock(context.getPlayer(), context.getPlayerProfile(), additionalBlock, miningBlock, miningTool);

	}
}
