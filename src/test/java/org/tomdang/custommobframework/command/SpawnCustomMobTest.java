package org.tomdang.custommobframework.command;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.custommobframework.CustomMob;
import org.tomdang.custommobframework.CustomMobRegistry;

import static org.mockito.Mockito.*;

class SpawnCustomMobTest {
	private final CustomMobRegistry registry = mock(CustomMobRegistry.class);
	private final SpawnCustomMob command = new SpawnCustomMob(registry);
	private final Player player = mock(Player.class);
	private final Command bukkitCommand = mock(Command.class);

	@Test
	void passesSelectedColorToSpawner() {
		CustomMob jellyfish = mock(CustomMob.class);
		Location location = mock(Location.class);
		when(player.getLocation()).thenReturn(location);
		when(registry.getCustomMob("JELLYFISH")).thenReturn(jellyfish);
		when(registry.getCustomMobAsMob(jellyfish, location, "GREEN")).thenReturn(mock(Entity.class));

		command.onCommand(player, bukkitCommand, "spawncustom", new String[]{"Jellyfish", "green"});

		verify(registry).getCustomMobAsMob(jellyfish, location, "GREEN");
	}

	@Test
	void rejectsUnsupportedColorWithoutSpawning() {
		when(registry.getCustomMob("JELLYFISH")).thenReturn(mock(CustomMob.class));

		command.onCommand(player, bukkitCommand, "spawncustom", new String[]{"Jellyfish", "purple"});

		verify(registry, never()).getCustomMobAsMob(any(), any(), any());
	}
}
