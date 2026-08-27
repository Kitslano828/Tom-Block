package org.tomdang.actorframework.interaction;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.movement.ActorLookService;

import static org.mockito.Mockito.*;

public class LookAtPlayerInteractionTest {

	@Test
	void looksAtPlayerBeforeDelegating() {
		ActorLookService actorLookService = mock(ActorLookService.class);
		ActorInteraction delegate = mock(ActorInteraction.class);
		Player player = mock(Player.class);
		ActorInstance instance = mock(ActorInstance.class);
		Location targetLocation = mock(Location.class);

		ActorInteractionContext context = new ActorInteractionContext(player, instance);

		when(player.getLocation()).thenReturn(targetLocation);

		LookAtPlayerInteraction interaction = new LookAtPlayerInteraction(actorLookService, delegate);

		interaction.interact(context);

		InOrder order = inOrder(actorLookService, delegate);
		order.verify(actorLookService).lookAt(instance, targetLocation);
		order.verify(delegate).interact(context);

	}

}
