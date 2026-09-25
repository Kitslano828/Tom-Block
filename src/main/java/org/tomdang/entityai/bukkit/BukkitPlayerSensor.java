package org.tomdang.entityai.bukkit;
import org.bukkit.Bukkit;import org.bukkit.entity.Player;import org.tomdang.entityai.core.*;import java.util.*;
public final class BukkitPlayerSensor implements AiSensor{
	private final UUID owner;private final double range;public BukkitPlayerSensor(UUID owner,double range){if(owner==null||range<=0)throw new IllegalArgumentException("Player sensor configuration is invalid");this.owner=owner;this.range=range;}
	@Override public PerceptionSnapshot sense(AiAgent agent,long tick){List<PerceivedEntity> result=new ArrayList<>();var visibilityEntity=asEntity(agent);for(Player player:Bukkit.getOnlinePlayers()){if(!player.getWorld().getKey().asString().equals(agent.worldId()))continue;AiVector position=vector(player.getLocation().getX(),player.getLocation().getY(),player.getLocation().getZ());if(position.distanceSquared(agent.position())>range*range)continue;var velocity=player.getVelocity();boolean visible=visibilityEntity==null||player.hasLineOfSight(visibilityEntity);result.add(new PerceivedEntity(player.getUniqueId(),position,vector(velocity.getX(),velocity.getY(),velocity.getZ()),visible,player.getUniqueId().equals(owner),false));}return new PerceptionSnapshot(tick,result);}
	private org.bukkit.entity.Entity asEntity(AiAgent agent){return agent instanceof BukkitEntityAgent value?value.visibilityEntity():null;}
	private AiVector vector(double x,double y,double z){return new AiVector(x,y,z);}
}
