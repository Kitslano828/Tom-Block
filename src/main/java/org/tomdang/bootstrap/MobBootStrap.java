package org.tomdang.bootstrap;

import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.tomdang.TomBlock;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.custommobframework.CustomMobRegistry;
import org.tomdang.custommobframework.CustomMobResolver;
import org.tomdang.custommobframework.CustomMobSpawner;
import org.tomdang.custommobframework.custommobcontext.CustomMobContextRegistry;
import org.tomdang.custommobframework.custommobhealth.CustomMobHealthService;
import org.tomdang.custommobframework.custommobspawn.CustomMobRespawnService;
import org.tomdang.custommobframework.custommobspawn.CustomMobSpawnPoint;
import org.tomdang.custommobframework.custommobspawn.CustomMobSpawnPointRegistry;
import org.tomdang.custommobframework.custommobspawn.CustomMobSpawnPointResolver;
import org.tomdang.custommobframework.custommobspawn.MobSpawnRegionPolicy;
import org.tomdang.custommobframework.custommobspawn.MobPopulationService;
import org.tomdang.custommobframework.custommobspawn.MobRegionConfinementPolicy;
import org.tomdang.custommobframework.configuration.CustomMobConfigurationLoader;
import org.tomdang.custommobframework.configuration.CustomMobDefinition;
import org.tomdang.custommobframework.configuration.CustomMobDefinitionRegistrar;
import org.tomdang.custommobframework.presentation.JellyfishPresentationService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.resolution.RegionResolver;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.bukkit.BukkitBlockPositionAdapter;
import org.tomdang.region.definition.RegionDefinition;

public class MobBootStrap {

	@Getter
	private final CustomMobRegistry customMobRegistry;
	@Getter
	private final CustomMobResolver customMobResolver;
	@Getter
	private final CustomMobHealthService customMobHealthService;
	@Getter
	private final CustomMobRespawnService customMobRespawnService;
	@Getter
	private final CustomMobContextRegistry customMobContextRegistry;
	private final CustomMobSpawnPointRegistry customMobSpawnPointRegistry;
	private final MobPopulationService mobPopulationService;
	private final JellyfishPresentationService jellyfishPresentationService;
	@Getter
	private final NamespacedKey mobVisualVariantKey;
	@Getter
	private final MobRegionConfinementPolicy mobRegionConfinementPolicy;

	public MobBootStrap(TomBlock instance, NamespacedKey customMobKey, NamespacedKey spawnPointIDKey,
	                    NamespacedKey currentHealthKey,
	                    NamespacedKey populationRuleKey,
						CustomItemRegistry customItemRegistry, RegionRegistry regionRegistry, RegionResolver regionResolver
	) {
		customMobContextRegistry = new CustomMobContextRegistry();
		List<CustomMobDefinition> definitions;
		try (InputStream stream = instance.getResource("mobs/mobs.yml")) {
			if (stream == null) throw new IllegalStateException("TomBlock.jar does not contain mobs.yml");
			definitions = new CustomMobConfigurationLoader().loadDefinitions(
					new InputStreamReader(stream, StandardCharsets.UTF_8));
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled mobs.yml resource", exception);
		}
		MobSpawnRegionPolicy spawnRegionPolicy = new MobSpawnRegionPolicy(regionResolver, regionRegistry, definitions);
		List<org.tomdang.custommobframework.configuration.MobPopulationRule> populationRules = definitions.stream()
				.map(CustomMobDefinition::population).filter(java.util.Objects::nonNull).toList();
		for (var rule : populationRules) {
			regionRegistry.require(rule.regionId());
			CustomMobDefinition definition = definitions.stream()
					.filter(value -> value.id().equals(rule.mobId())).findFirst().orElseThrow();
			if (!allowedByMob(definition, rule.regionId(), regionRegistry)) {
				// Population and manual spawn permissions must not contradict each other.
				throw new IllegalArgumentException("Population region is not allowed for mob " + rule.mobId());
			}
		}
		mobRegionConfinementPolicy = new MobRegionConfinementPolicy(spawnRegionPolicy, regionResolver, populationRules);
		mobVisualVariantKey = new NamespacedKey(instance, "mob_visual_variant");
		CustomMobSpawner customMobSpawner = new CustomMobSpawner(
				customMobKey, spawnPointIDKey, currentHealthKey, mobVisualVariantKey,
				customMobContextRegistry, spawnRegionPolicy);
		customMobRegistry = new CustomMobRegistry(customMobSpawner);
		mobPopulationService = new MobPopulationService(instance, customMobRegistry, customMobSpawner,
				customMobContextRegistry, regionResolver, customMobKey, populationRuleKey, populationRules);
		new CustomMobDefinitionRegistrar(customMobRegistry, customItemRegistry).registerDefinitions(definitions);
		jellyfishPresentationService = new JellyfishPresentationService(
				instance, customMobKey, currentHealthKey, mobVisualVariantKey, customMobRegistry);
		customMobSpawner.addSpawnObserver(jellyfishPresentationService::trackIfJellyfish);
		customMobResolver = new CustomMobResolver(customMobKey, customMobRegistry);
		customMobHealthService = new CustomMobHealthService(customMobContextRegistry,
				customMobResolver, currentHealthKey, jellyfishPresentationService);
		customMobSpawnPointRegistry = new CustomMobSpawnPointRegistry();
		CustomMobSpawnPointResolver customMobSpawnPointResolver = new CustomMobSpawnPointResolver(spawnPointIDKey, customMobSpawnPointRegistry);
		customMobRespawnService = new CustomMobRespawnService(
				instance,
				customMobRegistry,
				customMobSpawner,
				customMobSpawnPointResolver,
				spawnPointIDKey,
				customMobContextRegistry
		);
	}

	public void reconcileSpawnPoints() {
		for (CustomMobSpawnPoint spawnPoint : customMobSpawnPointRegistry.getAllSpawnPoints()) {
			customMobRespawnService.reconcileSpawnPoint(spawnPoint);
		}

	}

	private boolean allowedByMob(CustomMobDefinition definition, String regionId, RegionRegistry registry) {
		if (definition.allowedSpawnRegions().isEmpty()) return true;
		RegionDefinition region = registry.require(regionId);
		while (region != null) {
			if (definition.allowedSpawnRegions().contains(region.id())) return true;
			region = region.parentId().map(registry::require).orElse(null);
		}
		return false;
	}

	public void startPopulations() {
		mobPopulationService.start();
	}

	public void startPresentations() {
		jellyfishPresentationService.start();
	}

	public void stopPresentations() {
		jellyfishPresentationService.stop();
	}

	public void stopPopulations() {
		mobPopulationService.stop();
	}

	public void reconcileSpawnPointsAt(BlockPosition position) {
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		BukkitBlockPositionAdapter positions = new BukkitBlockPositionAdapter();
		for (CustomMobSpawnPoint spawnPoint : customMobSpawnPointRegistry.getAllSpawnPoints()) {
			if (positions.fromLocation(spawnPoint.getLocation()).equals(position)) {
				customMobRespawnService.reconcileSpawnPoint(spawnPoint);
			}
		}
	}

}
