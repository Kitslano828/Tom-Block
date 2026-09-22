package org.tomdang.mining;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.tomdang.mining.miningblock.MiningBlock;
import org.tomdang.mining.miningblock.MiningBlockRegistry;
import org.tomdang.mining.miningtool.MiningTool;
import org.tomdang.mining.miningtool.MiningToolResolver;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playerresource.PlayerStatsService;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MiningProgressServiceTest {
	@Test
	void interceptsAllRegisteredBlocksButNotOrdinaryBlocksOrCreative() {
		MiningBlockRegistry blocks = blocks();
		try (Fixture fixture = new Fixture(blocks)) {
			Player player = mock(Player.class);
			when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
			assertTrue(fixture.service.interceptBreak(player, block(Material.STONE)));
			assertTrue(fixture.service.interceptBreak(player, block(Material.COAL_ORE)));
			assertTrue(fixture.service.interceptBreak(player, block(Material.IRON_ORE)));
			assertTrue(fixture.service.interceptBreak(player, block(Material.DIAMOND_ORE)));
			assertTrue(fixture.service.interceptBreak(player, block(Material.IRON_BLOCK)));
			assertFalse(fixture.service.interceptBreak(player, block(Material.DIRT)));
			when(player.getGameMode()).thenReturn(GameMode.CREATIVE);
			assertFalse(fixture.service.interceptBreak(player, block(Material.IRON_ORE)));
		}
	}

	@Test
	void startsAnOreAttemptOnlyWithSufficientBreakingPower() {
		try (Fixture fixture = new Fixture(blocks())) {
			Player player = mock(Player.class);
			PlayerInventory inventory = mock(PlayerInventory.class);
			PlayerProfile profile = mock(PlayerProfile.class);
			MiningTool tool = mock(MiningTool.class);
			ItemStack held = mock(ItemStack.class);
			UUID playerId = UUID.randomUUID();
			when(player.getUniqueId()).thenReturn(playerId);
			when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
			when(player.getInventory()).thenReturn(inventory);
			when(inventory.getItemInMainHand()).thenReturn(held);
			when(held.clone()).thenReturn(held);
			when(fixture.profiles.getPlayerProfileFromMap(playerId)).thenReturn(profile);
			when(fixture.tools.getMiningTool(held)).thenReturn(tool);
			when(fixture.stats.getTotalMiningSpeed(player)).thenReturn(45.0);

			when(tool.getBreakingPower()).thenReturn(1);
			fixture.service.start(player, block(Material.DIAMOND_ORE));
			verify(fixture.stats, never()).getTotalMiningSpeed(player);

			when(tool.getBreakingPower()).thenReturn(2);
			fixture.service.start(player, block(Material.DIAMOND_ORE));
			verify(fixture.stats).getTotalMiningSpeed(player);
		}
	}

	@Test
	void completedBreakDoesNotClearCracksBeforeReplacingTheBlock() {
		try (Fixture fixture = new Fixture(blocks())) {
			UUID playerId = UUID.randomUUID();
			Player player = mock(Player.class);
			PlayerInventory inventory = mock(PlayerInventory.class);
			ItemStack held = mock(ItemStack.class);
			MiningTool tool = mock(MiningTool.class);
			World world = mock(World.class);
			Block block = mock(Block.class);
			Location location = new Location(world, 0, 64, 0);
			AtomicReference<Material> material = new AtomicReference<>(Material.STONE);
			when(block.getType()).thenAnswer(ignored -> material.get());
			when(block.getWorld()).thenReturn(world);
			when(block.getLocation()).thenAnswer(ignored -> location.clone());
			when(player.getUniqueId()).thenReturn(playerId);
			when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
			when(player.isOnline()).thenReturn(true);
			when(player.getWorld()).thenReturn(world);
			when(player.getLocation()).thenReturn(new Location(world, 0.5, 64, 0.5));
			when(player.getTargetBlockExact(6)).thenReturn(block);
			when(player.getInventory()).thenReturn(inventory);
			when(inventory.getItemInMainHand()).thenReturn(held);
			when(held.clone()).thenReturn(held);
			when(held.isSimilar(held)).thenReturn(true);
			when(fixture.profiles.getPlayerProfileFromMap(playerId)).thenReturn(mock(PlayerProfile.class));
			when(fixture.tools.getMiningTool(held)).thenReturn(tool);
			when(fixture.stats.getTotalMiningSpeed(player)).thenReturn(300.0); // Stone completes in one tick.
			when(player.breakBlock(block)).thenAnswer(ignored -> {
				material.set(Material.BEDROCK);
				return true;
			});
			fixture.bukkit.when(() -> Bukkit.getPlayer(playerId)).thenReturn(player);

			fixture.service.start(player, block);
			fixture.tick.run();

			verify(player).breakBlock(block);
			verify(player, never()).sendBlockDamage(any(Location.class), eq(0f));
		}
	}

	private static MiningBlockRegistry blocks() {
		MiningBlockRegistry blocks = new MiningBlockRegistry();
		blocks.addBlockToRegistry(Material.STONE, new MiningBlock(10, 0, Material.STONE, 1, 5));
		blocks.addBlockToRegistry(Material.COAL_ORE, new MiningBlock(12, 1, Material.COAL_ORE, 3, 5));
		blocks.addBlockToRegistry(Material.IRON_ORE, new MiningBlock(20, 1, Material.IRON_ORE, 5, 7));
		blocks.addBlockToRegistry(Material.DIAMOND_ORE, new MiningBlock(35, 2, Material.DIAMOND_ORE, 8, 10));
		blocks.addBlockToRegistry(Material.IRON_BLOCK, new MiningBlock(60, 3, Material.IRON_BLOCK, 12, 15));
		return blocks;
	}

	private static Block block(Material material) {
		Block block = mock(Block.class);
		when(block.getType()).thenReturn(material);
		return block;
	}

	private static final class Fixture implements AutoCloseable {
		private final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
		private final MiningToolResolver tools = mock(MiningToolResolver.class);
		private final PlayerProfileService profiles = mock(PlayerProfileService.class);
		private final PlayerStatsService stats = mock(PlayerStatsService.class);
		private final MiningProgressService service;
		private Runnable tick;

		private Fixture(MiningBlockRegistry blocks) {
			BukkitScheduler scheduler = mock(BukkitScheduler.class);
			when(scheduler.runTaskTimer(any(Plugin.class), any(Runnable.class), eq(1L), eq(1L)))
					.thenAnswer(invocation -> {
						tick = invocation.getArgument(1);
						return mock(BukkitTask.class);
					});
			bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
			service = new MiningProgressService(mock(Plugin.class), blocks, tools, profiles, stats);
		}

		@Override
		public void close() {
			bukkit.close();
		}
	}
}
