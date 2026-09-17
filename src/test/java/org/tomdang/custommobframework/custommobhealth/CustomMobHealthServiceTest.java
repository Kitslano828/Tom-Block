package org.tomdang.custommobframework.custommobhealth;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.CustomMobResolver;
import org.tomdang.custommobframework.custommobcontext.CustomMobContextRegistry;
import org.tomdang.combat.eligibility.AttackEligibilityRule;
import org.tomdang.combat.eligibility.AttackCapability;
import org.tomdang.combat.eligibility.AttackDelivery;
import org.tomdang.combat.eligibility.AttackSource;
import java.util.Set;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

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

	@Test
	void jellyfishRejectsOrdinaryAttacksButAcceptsNetCapability() {
		LivingEntity entity = mobEntity(50.0);
		CustomMob mob = resolver.getCustomMob(entity);
		when(mob.getAttackEligibilityRule()).thenReturn(
				new AttackEligibilityRule(Set.of(AttackCapability.JELLYFISH_HUNTING)));
		AttackSource ordinary = new AttackSource(AttackDelivery.BASIC_ATTACK, Set.of());
		AttackSource net = new AttackSource(AttackDelivery.BASIC_ATTACK, Set.of(AttackCapability.JELLYFISH_HUNTING));

		assertFalse(service.damageMob(null, entity, 10.0, ordinary));
		assertTrue(service.damageMob(null, entity, 10.0, net));
		assertEquals(40.0, contexts.getCustomMobContext(entity.getUniqueId()).getCurrentHealth());
	}

	@Test
	void feedbackOnlyReportsAcceptedDamageAndRejectedAttackSeparately() {
		LivingEntity entity = mobEntity(50.0);
		CustomMob mob = resolver.getCustomMob(entity);
		when(mob.getAttackEligibilityRule()).thenReturn(
				new AttackEligibilityRule(Set.of(AttackCapability.JELLYFISH_HUNTING)));
		CustomMobHitFeedback feedback = mock(CustomMobHitFeedback.class);
		CustomMobHealthService withFeedback = new CustomMobHealthService(contexts, resolver, healthKey, feedback);
		org.bukkit.entity.Player attacker = mock(org.bukkit.entity.Player.class);
		AttackSource ordinary = new AttackSource(AttackDelivery.BASIC_ATTACK, Set.of());
		AttackSource net = new AttackSource(AttackDelivery.BASIC_ATTACK, Set.of(AttackCapability.JELLYFISH_HUNTING));

		assertFalse(withFeedback.damageMob(attacker, entity, 10.0, ordinary));
		verify(feedback).onRejectedHit(attacker, entity);
		verify(feedback, never()).onAcceptedHit(entity, 50.0, 50.0);

		assertTrue(withFeedback.damageMob(attacker, entity, 10.0, net));
		verify(feedback).onAcceptedHit(entity, 40.0, 100.0);
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
		when(mob.getAttackEligibilityRule()).thenReturn(new AttackEligibilityRule(Set.of()));
		return entity;
	}
}
