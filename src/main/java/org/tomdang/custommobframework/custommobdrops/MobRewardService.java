package org.tomdang.custommobframework.custommobdrops;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.tomdang.combat.combatlevel.CombatLevel;
import org.tomdang.customitemframework.CustomItemCreator;
import org.tomdang.customitemframework.CustomItemStackFactory;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.CustomMobResolver;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playeractionbar.PlayerActionBarService;

import java.util.Objects;

public class MobRewardService {

	private final PlayerProfileService playerProfileService;
	private final CustomItemStackFactory customItemStackFactory;
	private final CustomMobResolver customMobResolver;
	private final CombatLevel combatLevel;
	private final PlayerActionBarService playerActionBarService;

	public MobRewardService(PlayerProfileService playerProfileService, CustomItemStackFactory customItemStackFactory,
							CustomMobResolver customMobResolver, CombatLevel combatLevel, PlayerActionBarService playerActionBarService
	) {
		this.playerProfileService = playerProfileService;
		this.customItemStackFactory = customItemStackFactory;
		this.customMobResolver = customMobResolver;
		this.combatLevel = combatLevel;
		this.playerActionBarService = playerActionBarService;
	}

	public void givePlayerMobDrops(EntityDeathEvent event) {
		CustomMob customMob = customMobResolver.getCustomMob(event.getEntity());
		if (customMob != null) {
			event.getDrops().clear();
			Player player = event.getEntity().getKiller();
			if (player != null) {
				PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
				player.sendMessage("You killed the " + customMob.getName() + "!");
				for (CustomMobDrop customMobDrop : customMob.getCustomMobDrops()) {
					ItemStack drop = customItemStackFactory.createCustomItemStack(customMobDrop.getItem());
					if (customMobDrop.rollForDrop()) {
						if(player.getInventory().firstEmpty() == -1) {
							player.sendMessage("§9YOUR INVENTORY IS FULL!");
							player.dropItem(customMobDrop.getMobDrops(drop, customMobDrop.getAmount()));
						} else {
							player.sendMessage("YOU DROPPED " + customMobDrop.getItem().getDisplayName() + " x" + customMobDrop.getAmount());
							drop.setAmount(customMobDrop.getAmount());
							player.getInventory().addItem(drop);
						}
					}
					}
					givePlayerCombatXP(event, playerProfile, customMob);
				}
			}
		}

		public void givePlayerCombatXP(EntityDeathEvent event, PlayerProfile player, CustomMob customMob) {
			combatLevel.updatePlayerCombatXP(customMob.getXpAmount(), player);
			Component text = Component.text(player.getCombatXP() + " / " + combatLevel.getNextCombatLevel(player) + " Combat XP").color(NamedTextColor.DARK_AQUA)	;
			playerActionBarService.showTemporaryMessage(Objects.requireNonNull(event.getEntity().getKiller()), text, 40);
			combatLevel.playerCombatLevelUp(event, player, playerActionBarService);
		}
}


