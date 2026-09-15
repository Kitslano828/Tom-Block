package org.tomdang.customabilityframework;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.tomdang.customabilityframework.abilitycooldown.AbilityCooldownService;
import org.tomdang.customabilityframework.customability.AbilityExecutionContext;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customabilityframework.source.AbilitySource;
import org.tomdang.customabilityframework.source.AbilitySourceProvider;
import org.tomdang.customabilityframework.source.AbilitySourceType;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.player.playerresource.PlayerResourceService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomAbilityServiceTest {

	@Test
	void nullDependenciesAndTriggerArgumentsAreRejected() {
		AbilitySourceProvider provider = mock(AbilitySourceProvider.class);
		PlayerResourceService resources = mock(PlayerResourceService.class);
		AbilityCooldownService cooldowns = mock(AbilityCooldownService.class);
		CustomAbilityService service = new CustomAbilityService(provider, resources, cooldowns);

		assertThrows(IllegalArgumentException.class, () -> new CustomAbilityService(null, resources, cooldowns));
		assertThrows(IllegalArgumentException.class, () -> new CustomAbilityService(provider, null, cooldowns));
		assertThrows(IllegalArgumentException.class, () -> new CustomAbilityService(provider, resources, null));
		assertThrows(IllegalArgumentException.class, () -> service.triggerAbility(null, AbilityTrigger.RIGHT_CLICK));
		assertThrows(IllegalArgumentException.class, () -> service.triggerAbility(mock(Player.class), null));
	}

	@Test
	void abilitiesWithDifferentTriggersAreIgnored() {
		Fixture fixture = fixture(AbilityTrigger.LEFT_CLICK);

		fixture.service().triggerAbility(fixture.player(), AbilityTrigger.RIGHT_CLICK);

		verify(fixture.cooldowns(), never()).isAbilityOnCooldown(fixture.player(), fixture.source().sourceId());
		verify(fixture.resources(), never()).spendEnergy(fixture.player(), fixture.ability().getEnergyCost());
		verify(fixture.ability(), never()).execute(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void abilityOnCooldownDoesNotSpendEnergyOrExecute() {
		Fixture fixture = fixture(AbilityTrigger.RIGHT_CLICK);
		when(fixture.cooldowns().isAbilityOnCooldown(fixture.player(), fixture.source().sourceId())).thenReturn(true);

		fixture.service().triggerAbility(fixture.player(), AbilityTrigger.RIGHT_CLICK);

		verify(fixture.player()).sendMessage("Test Ability is on cooldown!");
		verify(fixture.resources(), never()).spendEnergy(fixture.player(), fixture.ability().getEnergyCost());
		verify(fixture.ability(), never()).execute(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void rejectedActivationDoesNotCheckCooldownSpendEnergyOrExecute() {
		Fixture fixture = fixture(AbilityTrigger.RIGHT_CLICK);
		when(fixture.ability().canActivate(org.mockito.ArgumentMatchers.any())).thenReturn(false);

		fixture.service().triggerAbility(fixture.player(), AbilityTrigger.RIGHT_CLICK);

		verify(fixture.cooldowns(), never()).isAbilityOnCooldown(fixture.player(), fixture.source().sourceId());
		verify(fixture.resources(), never()).spendEnergy(fixture.player(), fixture.ability().getEnergyCost());
		verify(fixture.ability(), never()).execute(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void insufficientEnergyDoesNotStartCooldownOrExecute() {
		Fixture fixture = fixture(AbilityTrigger.RIGHT_CLICK);
		when(fixture.resources().spendEnergy(fixture.player(), 25)).thenReturn(false);

		fixture.service().triggerAbility(fixture.player(), AbilityTrigger.RIGHT_CLICK);

		verify(fixture.player()).sendMessage("You don't have enough energy to use this ability!");
		verify(fixture.cooldowns(), never()).startAbilityCooldown(
				fixture.player(), fixture.source().sourceId(), fixture.ability().getCooldownInTicks());
		verify(fixture.ability(), never()).execute(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void successfulAbilityUsesItsSourceForContextAndCooldownIdentity() {
		Fixture fixture = fixture(AbilityTrigger.RIGHT_CLICK);
		when(fixture.resources().spendEnergy(fixture.player(), 25)).thenReturn(true);

		fixture.service().triggerAbility(fixture.player(), AbilityTrigger.RIGHT_CLICK);

		verify(fixture.cooldowns()).startAbilityCooldown(fixture.player(), fixture.source().sourceId(), 40);
		ArgumentCaptor<AbilityExecutionContext> contextCaptor = ArgumentCaptor.forClass(AbilityExecutionContext.class);
		verify(fixture.ability()).execute(contextCaptor.capture());
		assertSame(fixture.player(), contextCaptor.getValue().getPlayer());
		assertSame(fixture.sourceItem(), contextCaptor.getValue().getCastingItem());
	}

	private Fixture fixture(AbilityTrigger trigger) {
		Player player = mock(Player.class);
		CustomItem sourceItem = mock(CustomItem.class);
		CustomAbility ability = mock(CustomAbility.class);
		when(ability.getAbilityTrigger()).thenReturn(trigger);
		when(ability.getAbilityName()).thenReturn("Test Ability");
		when(ability.getEnergyCost()).thenReturn(25.0);
		when(ability.getCooldownInTicks()).thenReturn(40L);
		when(ability.canActivate(org.mockito.ArgumentMatchers.any())).thenReturn(true);
		AbilitySource source = new AbilitySource(
				ability,
				sourceItem,
				"equipment:main-hand:TEST_ITEM:TEST_ABILITY",
				AbilitySourceType.MAIN_HAND
		);
		AbilitySourceProvider provider = mock(AbilitySourceProvider.class);
		when(provider.getAbilitySources(player)).thenReturn(List.of(source));
		PlayerResourceService resources = mock(PlayerResourceService.class);
		AbilityCooldownService cooldowns = mock(AbilityCooldownService.class);
		return new Fixture(
				player,
				sourceItem,
				ability,
				source,
				resources,
				cooldowns,
				new CustomAbilityService(provider, resources, cooldowns)
		);
	}

	private record Fixture(Player player, CustomItem sourceItem, CustomAbility ability,
	                       AbilitySource source, PlayerResourceService resources,
	                       AbilityCooldownService cooldowns, CustomAbilityService service) {
	}
}
