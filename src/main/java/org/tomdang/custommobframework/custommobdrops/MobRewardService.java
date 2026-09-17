package org.tomdang.custommobframework.custommobdrops;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.tomdang.player.skill.SkillProgressionService;
import org.tomdang.player.skill.SkillProgressPresenter;
import org.tomdang.player.skill.SkillType;
import org.tomdang.customitemframework.CustomItemCreator;
import org.tomdang.customitemframework.CustomItemStackFactory;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.CustomMobResolver;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;


public class MobRewardService {

	private final PlayerProfileService playerProfileService;
	private final CustomItemStackFactory customItemStackFactory;
	private final CustomMobResolver customMobResolver;
	private final SkillProgressionService skillProgression;
	private final SkillProgressPresenter skillPresenter;

	public MobRewardService(PlayerProfileService playerProfileService, CustomItemStackFactory customItemStackFactory,
							CustomMobResolver customMobResolver, SkillProgressionService skillProgression, SkillProgressPresenter skillPresenter
	) {
		this.playerProfileService = playerProfileService;
		this.customItemStackFactory = customItemStackFactory;
		this.customMobResolver = customMobResolver;
		this.skillProgression = skillProgression;
		this.skillPresenter = skillPresenter;
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
			Player killer = event.getEntity().getKiller();
			if (killer == null) return;
			skillPresenter.showAward(killer, skillProgression.awardXp(player, SkillType.COMBAT, customMob.getXpAmount()));
		}
}


