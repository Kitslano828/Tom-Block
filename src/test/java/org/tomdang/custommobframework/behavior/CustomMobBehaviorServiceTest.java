package org.tomdang.custommobframework.behavior;

import org.bukkit.NamespacedKey;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;
import org.tomdang.TomBlock;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.CustomMobRegistry;
import org.tomdang.custommobframework.custommobspawn.MobRegionConfinementPolicy;
import org.tomdang.custommobframework.configuration.MobPopulationRule;
import org.tomdang.custommobframework.configuration.MobSpawnPlacement;
import org.tomdang.region.position.BlockPosition;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CustomMobBehaviorServiceTest {
	private final NamespacedKey mobKey = NamespacedKey.minecraft("custom_mob");
	private final CustomMobRegistry registry = mock(CustomMobRegistry.class);
	private final CustomMobBehaviorService service = new CustomMobBehaviorService(
			mock(TomBlock.class), registry, mock(MobRegionConfinementPolicy.class),
			mobKey, NamespacedKey.minecraft("population"), List.of());

	@Test
	void vanillaBehaviorLeavesCarrierUntouched() {
		Mob carrier = mock(Mob.class);
		stubMob(carrier, "ZOMBIE", MobBehaviorType.VANILLA);
		service.track(carrier);
		verify(carrier, never()).setAI(false);
		verify(carrier, never()).setGravity(false);
	}

	@Test
	void floatingBehaviorDisablesVanillaAiAndItemPickup() {
		Mob carrier = mock(Mob.class);
		stubMob(carrier, "JELLYFISH", MobBehaviorType.FLOATING_WANDER);
		service.track(carrier);
		verify(carrier).setAI(false);
		verify(carrier).setGravity(false);
		verify(carrier).setCanPickupItems(false);
	}

	@Test
	void blocksPlayerItemInteractionOnlyForCustomFloater() {
		Entity carrier = mock(Entity.class);
		stubMob(carrier, "JELLYFISH", MobBehaviorType.FLOATING_WANDER);
		PlayerInteractEntityEvent event = mock(PlayerInteractEntityEvent.class);
		when(event.getRightClicked()).thenReturn(carrier);
		service.onInteract(event);
		verify(event).setCancelled(true);
	}

	@Test
	void floatingStepRespectsHeightAndRegionBoundary() {
		MobRegionConfinementPolicy confinement = mock(MobRegionConfinementPolicy.class);
		MobPopulationRule rule = new MobPopulationRule("JELLYFISH", "AREA", 30, 40,
				48, 80, 200, 12, 40, MobSpawnPlacement.AIR, 65, 135, 6);
		CustomMobBehaviorService bounded = new CustomMobBehaviorService(mock(TomBlock.class), registry,
				confinement, mobKey, NamespacedKey.minecraft("population"), List.of(rule));
		World world = mock(World.class);
		when(world.getName()).thenReturn("world");
		when(world.isChunkLoaded(anyInt(), anyInt())).thenReturn(true);
		when(world.getMinHeight()).thenReturn(-64);
		when(world.getMaxHeight()).thenReturn(320);
		Block clear = mock(Block.class);
		when(clear.isPassable()).thenReturn(true);
		when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenReturn(clear);
		Mob carrier = mock(Mob.class);
		PersistentDataContainer data = mock(PersistentDataContainer.class);
		when(carrier.getPersistentDataContainer()).thenReturn(data);
		when(data.get(mobKey, PersistentDataType.STRING)).thenReturn("JELLYFISH");
		Location inside = new Location(world, 1200, 80, -150);
		when(confinement.contains(eq("JELLYFISH"), isNull(), any(BlockPosition.class))).thenReturn(true);
		assertTrue(bounded.canMove(carrier, inside));
		assertFalse(bounded.canMove(carrier, new Location(world, 1200, 136, -150)));
		when(confinement.contains(eq("JELLYFISH"), isNull(), any(BlockPosition.class))).thenReturn(false);
		assertFalse(bounded.canMove(carrier, inside));
	}

	private void stubMob(Entity carrier, String id, MobBehaviorType behavior) {
		PersistentDataContainer data = mock(PersistentDataContainer.class);
		CustomMob customMob = mock(CustomMob.class);
		when(carrier.getPersistentDataContainer()).thenReturn(data);
		when(data.get(mobKey, PersistentDataType.STRING)).thenReturn(id);
		when(registry.getCustomMob(id)).thenReturn(customMob);
		when(customMob.getBehavior()).thenReturn(behavior);
		when(carrier.isValid()).thenReturn(true);
	}
}
