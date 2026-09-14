package org.tomdang.actorframework.nameplate.nms;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.nameplate.ActorNameplateLine;
import org.tomdang.actorframework.nameplate.ActorNameplateLineRole;
import org.tomdang.actorframework.nameplate.layout.ActorNameplateLinePlacement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class NmsActorNameplateLineFactoryTest {

	private NmsActorNameplateLineFactory factory;
	private World world;
	private ActorNameplateLinePlacement placement;

	@BeforeEach
	void setUp() {
		factory = new NmsActorNameplateLineFactory();
		world = mock(World.class);
		ActorNameplateLine line = new ActorNameplateLine(
				ActorNameplateLineRole.NAME,
				Component.text("Blacksmith"),
				true
		);
		placement = new ActorNameplateLinePlacement(line, 2.3);
	}

	@Test
	void rejectsNullActorLocation() {
		assertThrows(IllegalArgumentException.class, () -> factory.create(null, placement));
	}

	@Test
	void rejectsLocationWithoutWorld() {
		Location location = new Location(null, 1.0, 2.0, 3.0);

		assertThrows(IllegalArgumentException.class, () -> factory.create(location, placement));
	}

	@Test
	void rejectsNullPlacement() {
		Location location = new Location(world, 1.0, 2.0, 3.0);

		assertThrows(IllegalArgumentException.class, () -> factory.create(location, null));
	}

	@Test
	void rejectsNonFiniteBaseCoordinates() {
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () ->
						factory.create(new Location(world, Double.NaN, 2.0, 3.0), placement)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						factory.create(new Location(world, 1.0, Double.POSITIVE_INFINITY, 3.0), placement)),
				() -> assertThrows(IllegalArgumentException.class, () ->
						factory.create(new Location(world, 1.0, 2.0, Double.NEGATIVE_INFINITY), placement))
		);
	}

	@Test
	void rejectsOverflowedFinalYCoordinate() {
		Location location = new Location(world, 1.0, Double.MAX_VALUE, 3.0);
		ActorNameplateLinePlacement enormousPlacement =
				new ActorNameplateLinePlacement(placement.line(), Double.MAX_VALUE);

		assertThrows(IllegalArgumentException.class, () ->
				factory.create(location, enormousPlacement)
		);
	}
}
