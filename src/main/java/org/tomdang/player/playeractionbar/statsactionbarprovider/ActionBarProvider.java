package org.tomdang.player.playeractionbar.statsactionbarprovider;

import net.kyori.adventure.text.Component;
import org.tomdang.player.playeractionbar.PlayerActionBarContext;

public interface ActionBarProvider {

	boolean shouldDisplay(PlayerActionBarContext context);

	Component render(PlayerActionBarContext context);

	int getPriority();
}
