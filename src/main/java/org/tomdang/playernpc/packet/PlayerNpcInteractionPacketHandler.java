package org.tomdang.playernpc.packet;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface PlayerNpcInteractionPacketHandler {

	public void handleInteraction(Player player, int entityID);

}
