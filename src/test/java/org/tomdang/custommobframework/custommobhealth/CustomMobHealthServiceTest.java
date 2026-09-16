package org.tomdang.custommobframework.custommobhealth;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.CustomMobResolver;
import org.tomdang.custommobframework.custommobcontext.CustomMobContextRegistry;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomMobHealthServiceTest {
	private final NamespacedKey healthKey = NamespacedKey.minecraft("mob_current_health");
	private final CustomMobContextRegistry contexts = new CustomMobContextRegistry();
	private final CustomMobResolver resolver = mock(CustomMobResolver.class);
	private final CustomMobHealthService service = new CustomMobHealthService(contexts, resolver, healthKey);

	@Test
	void restoresSavedHealthAfterRestartThenPersistsDamage() {
		LivingEntity entity = mobEntity(65.0);

		service.damageMob(null, entity, 10.0);

		assertEquals(55.0, contexts.getCustomMobContext(entity.getUniqueId()).getCurrentHealth());
		verify(entity.getPersistentDataContainer()).set(healthKey, PersistentDataType.DOUBLE, 55.0);
	}

	@Test
	void olderMobWithoutSavedHealthUsesBukkitHealth() {
		LivingEntity entity = mobEntity(null);
		when(entity.getHealth()).thenReturn(80.0);

		service.damageMob(null, entity, 10.0);

		assertEquals(70.0, contexts.getCustomMobContext(entity.getUniqueId()).getCurrentHealth());
		verify(entity.getPersistentDataContainer()).set(healthKey, PersistentDataType.DOUBLE, 70.0);
	}

	private LivingEntity mobEntity(Double savedHealth) {
		LivingEntity entity = mock(LivingEntity.class);
		PersistentDataContainer data = mock(PersistentDataContainer.class);
		CustomMob mob = mock(CustomMob.class);
		when(entity.getUniqueId()).thenReturn(UUID.randomUUID());
		when(entity.getPersistentDataContainer()).thenReturn(data);
		when(data.get(healthKey, PersistentDataType.DOUBLE)).thenReturn(savedHealth);
		when(resolver.getCustomMob(entity)).thenReturn(mob);
		when(mob.getMaxHealth()).thenReturn(100.0);
		when(mob.getName()).thenReturn("Training Zombie");
		return entity;
	}
}
