package org.tomdang.playernpc.nms;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.tomdang.playernpc.packet.PlayerNpcInteractionPacketHandler;

public class NmsPlayerNpcInteractionInterceptor {

	private final Plugin plugin;
	private final PlayerNpcInteractionPacketHandler playerNpcInteractionPacketHandler;
	private static final String HANDLER_NAME = "tomblock_player_npc_interaction";

	public NmsPlayerNpcInteractionInterceptor(Plugin plugin, PlayerNpcInteractionPacketHandler playerNpcInteractionPacketHandler) {
		if (plugin == null) throw new IllegalArgumentException("Plugin cannot be null");
		if (playerNpcInteractionPacketHandler == null) throw new IllegalArgumentException("playerNpcInteractionPacketHandler cannot be null");

		this.plugin = plugin;
		this.playerNpcInteractionPacketHandler = playerNpcInteractionPacketHandler;

	}

	public void install(Player player) {
		Connection connection = resolveConnection(player);

		Channel channel = connection.channel;
		if (channel == null) throw new IllegalStateException("channel cannot be null");

		channel.eventLoop().execute(() -> {
			if (!channel.isActive()) return;
			ChannelPipeline pipeline = channel.pipeline();
			if (pipeline.get(HANDLER_NAME) != null) return;

			ChannelHandlerContext connectionContext = pipeline.context(connection);

			if (connectionContext == null) return;

			pipeline.addBefore(
					connectionContext.name(),
					HANDLER_NAME,
					new InteractionChannelHandler(player)
			);
		});
	}

	public void remove(Player player) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");

		CraftPlayer craftPlayer = (CraftPlayer) player;
		ServerPlayer nmsServerPlayer = craftPlayer.getHandle();
		ServerGamePacketListenerImpl gamePacketListener = nmsServerPlayer.connection;
		if (gamePacketListener == null) return;

		Connection connection = gamePacketListener.connection;
		if (connection == null) return;


		Channel channel = connection.channel;
		if (channel == null) return;
		channel.eventLoop().execute(() -> {
			ChannelPipeline pipeline = channel.pipeline();
			if (pipeline.get(HANDLER_NAME) == null) return;
			pipeline.remove(HANDLER_NAME);
		});
	}

	private Connection resolveConnection(Player player) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");

		CraftPlayer craftPlayer = (CraftPlayer) player;
		ServerPlayer nmsServerPlayer = craftPlayer.getHandle();
		ServerGamePacketListenerImpl gamePacketListener = nmsServerPlayer.connection;
		if (gamePacketListener == null) throw new IllegalStateException("gamePacketListener cannot be null");

		Connection connection = gamePacketListener.connection;
		if (connection == null) throw new IllegalStateException("Connection cannot be null");
		return connection;
	}

	private class InteractionChannelHandler extends ChannelDuplexHandler{
		private final Player player;
		public InteractionChannelHandler(Player player) {
			if (player == null) throw new IllegalArgumentException("Player cannot be null");
			this.player = player;
		}

		@Override
		public void channelRead(
				ChannelHandlerContext context,
				Object message
		) throws Exception {

			if (message instanceof ServerboundInteractPacket packet) {
				if (packet.hand() == InteractionHand.MAIN_HAND) {
					int entityId = packet.entityId();
					plugin.getServer().getScheduler().runTask(plugin, () -> {
						// main-thread work
						if (!player.isOnline()) {
							return;
						}
						playerNpcInteractionPacketHandler.handleInteraction(player, entityId);
					});
				}
			}
			context.fireChannelRead(message);
		}
	}

}


