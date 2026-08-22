package org.tomdang.crafting.interaction;

import org.bukkit.entity.Player;
import org.tomdang.actorframework.interaction.ActorInteraction;
import org.tomdang.actorframework.interaction.ActorInteractionContext;
import org.tomdang.crafting.gui.ForgeMenu;

public class OpenForgeInteraction implements ActorInteraction {
	@Override
	public void interact(ActorInteractionContext context) {
		if (context == null) throw new IllegalArgumentException("context cannot be null");
		Player player = context.player();
		ForgeMenu menu = new ForgeMenu();
		menu.open(player);
	}
}
