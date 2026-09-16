package org.tomdang.region.command;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.mockito.ArgumentCaptor;
import org.tomdang.region.bukkit.BukkitBlockPositionAdapter;
import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.override.RegionOverrides;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.resolution.RegionResolver;
import org.tomdang.region.shape.CuboidRegionShape;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegionInspectCommandTest {
	@Test
	void reportsDirectInheritedAndPrimaryRegions() {
		RegionRegistry registry = new RegionRegistry();
		registry.registerAll(List.of(
				definition("LIBRARY", "VILLAGE", 20),
				definition("VILLAGE", null, 10)
		));
		RegionInspectCommand command = new RegionInspectCommand(
				new RegionResolver(registry), new BukkitBlockPositionAdapter());
		Player player = playerAt(5);

		command.onCommand(player, mock(Command.class), "region", new String[]{"inspect"});

		String output = sentMessages(player);
		assertTrue(output.contains("Primary:"));
		assertTrue(output.contains("LIBRARY"));
		assertTrue(output.contains("Direct:"));
		assertTrue(output.contains("Inherited:"));
		assertTrue(output.contains("VILLAGE"));
	}

	@Test
	void reportsNoRegionAndRejectsConsoleOrInvalidUsage() {
		RegionInspectCommand command = new RegionInspectCommand(
				new RegionResolver(new RegionRegistry()), new BukkitBlockPositionAdapter());
		Player player = playerAt(50);
		Command bukkitCommand = mock(Command.class);

		command.onCommand(player, bukkitCommand, "region", new String[]{"inspect"});
		assertTrue(sentMessages(player).contains("No configured region"));

		CommandSender console = mock(CommandSender.class);
		command.onCommand(console, bukkitCommand, "region", new String[]{"inspect"});
		verify(console).sendMessage(Component.text("This command can only inspect a player's current block."));
	}

	@Test
	void providesInspectTabCompletion() {
		RegionInspectCommand command = new RegionInspectCommand(
				new RegionResolver(new RegionRegistry()), new BukkitBlockPositionAdapter());
		assertTrue(command.onTabComplete(mock(CommandSender.class), mock(Command.class), "region",
				new String[]{"in"}).contains("inspect"));
	}

	private RegionDefinition definition(String id, String parentId, int priority) {
		BlockPosition minimum = new BlockPosition("world", 0, 0, 0);
		BlockPosition maximum = new BlockPosition("world", 10, 100, 10);
		return new RegionDefinition(id, Optional.ofNullable(parentId), priority, Set.of("TEST"),
				new CuboidRegionShape(minimum, maximum), RegionOverrides.empty());
	}

	private Player playerAt(int x) {
		World world = mock(World.class);
		when(world.getName()).thenReturn("world");
		Player player = mock(Player.class);
		when(player.getLocation()).thenReturn(new Location(world, x, 64, 0));
		return player;
	}

	private String sentMessages(Player player) {
		ArgumentCaptor<Component> messages = ArgumentCaptor.forClass(Component.class);
		verify(player, atLeastOnce()).sendMessage(messages.capture());
		return String.join("\n", messages.getAllValues().stream()
				.map(PlainTextComponentSerializer.plainText()::serialize)
				.toList());
	}
}
