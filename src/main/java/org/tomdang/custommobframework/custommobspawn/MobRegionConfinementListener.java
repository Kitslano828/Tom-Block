package org.tomdang.custommobframework.custommobspawn;

import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.tomdang.region.bukkit.BukkitBlockPositionAdapter;
import org.tomdang.region.position.BlockPosition;

/** Blocks AI movement across an exact block-level region boundary. */
public final class MobRegionConfinementListener implements Listener {
	private final NamespacedKey mobKey;
	private final NamespacedKey populationKey;
	private final MobRegionConfinementPolicy policy;
	private final BukkitBlockPositionAdapter positions = new BukkitBlockPositionAdapter();

	public MobRegionConfinementListener(NamespacedKey mobKey, NamespacedKey populationKey,
	                                    MobRegionConfinementPolicy policy) {
		if (mobKey == null || populationKey == null || policy == null) {
			throw new IllegalArgumentException("mobKey, populationKey, and policy are required");
		}
		this.mobKey = mobKey;
		this.populationKey = populationKey;
		this.policy = policy;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onMove(EntityMoveEvent event) {
		if (!event.hasChangedBlock()) return;
		var data = event.getEntity().getPersistentDataContainer();
		String mobId = data.get(mobKey, PersistentDataType.STRING);
		if (mobId == null) return;
		String populationId = data.get(populationKey, PersistentDataType.STRING);
		if (!policy.constrained(mobId, populationId)) return;
		BlockPosition from = positions.fromLocation(event.getFrom());
		BlockPosition to = positions.fromLocation(event.getTo());
		if (policy.contains(mobId, populationId, to)) return;
		if (!policy.contains(mobId, populationId, from)) return; // Legacy mobs can move back inside.
		event.setCancelled(true);
		if (event.getEntity() instanceof Mob mob && mob.getTarget() != null
				&& !policy.contains(mobId, populationId, positions.fromLocation(mob.getTarget().getLocation()))) {
			mob.setTarget(null);
		}
	}
}
