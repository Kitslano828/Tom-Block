package org.tomdang.collection;

import org.tomdang.player.counter.PlayerCounterService;

import java.util.Collection;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.skill.SkillProgressPresenter;
import org.tomdang.player.skill.SkillProgressionService;
import org.tomdang.player.skill.SkillType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.tomdang.gameplay.event.GameplayEventBus;
import org.tomdang.gameplay.event.type.ItemCollected;

public final class CollectionService {
	private final CollectionRegistry registry;
	private final PlayerCounterService counters;
	private final PlayerProfileService profiles;
	private final SkillProgressPresenter presenter;
	private final SkillProgressionService progression = new SkillProgressionService();
	private final GameplayEventBus gameplayEvents;
	public CollectionService(CollectionRegistry registry, PlayerCounterService counters,
			PlayerProfileService profiles, SkillProgressPresenter presenter) {
		this(registry, counters, profiles, presenter, null);
	}
	public CollectionService(CollectionRegistry registry, PlayerCounterService counters,
			PlayerProfileService profiles, SkillProgressPresenter presenter, GameplayEventBus gameplayEvents) {
		this.registry = registry; this.counters = counters; this.profiles = profiles; this.presenter = presenter;
		this.gameplayEvents = gameplayEvents;
	}
	public Collection<CollectionDefinition> definitions() { return registry.all(); }
	public long amount(UUID playerId, CollectionDefinition definition) { return counters.get(playerId, definition.counterKey()); }
	public long increment(Player player, String collectionId, long delta) {
		CollectionDefinition definition = registry.require(collectionId);
		long total = counters.increment(player.getUniqueId(), definition.counterKey(), delta);
		long previous = total - delta;
		for (CollectionMilestone milestone : definition.milestones()) if (previous < milestone.amount() && total >= milestone.amount()) {
			player.sendMessage(Component.text("Collection milestone: " + definition.displayName() + " " + milestone.amount(), NamedTextColor.GOLD));
			var profile = profiles.getPlayerProfileFromMap(player.getUniqueId());
			if (profile != null && milestone.skillXpReward() > 0) presenter.showAward(player,
					progression.awardXp(profile, SkillType.FORAGING, milestone.skillXpReward()));
		}
		if (gameplayEvents != null && delta > 0) gameplayEvents.publish(
				new ItemCollected(player.getUniqueId(), collectionId, delta));
		return total;
	}
}
