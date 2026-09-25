package org.tomdang.entityai.bukkit;
import org.bukkit.Location;import org.bukkit.entity.*;import org.tomdang.entityai.core.*;import org.tomdang.entityai.navigation.*;import java.util.*;
public final class BukkitMobGroundAgent implements NativeGroundNavigator.NativeGroundAgent,BukkitEntityAgent{
	private final Mob mob;private final AiVector home;
	public BukkitMobGroundAgent(Mob mob){if(mob==null)throw new IllegalArgumentException("Mob is required");this.mob=mob;this.home=vector(mob.getLocation());}
	public UUID id(){return mob.getUniqueId();}public String worldId(){return mob.getWorld().getKey().asString();}public AiVector position(){return vector(mob.getLocation());}public AiVector home(){return home;}public Set<AiCapability> capabilities(){return Set.of(AiCapability.GROUND,AiCapability.LOOKING);}public boolean valid(){return mob.isValid()&&!mob.isDead();}public Entity visibilityEntity(){return mob;}
	public boolean navigateNative(NavigationRequest request){return mob.getPathfinder().moveTo(new Location(mob.getWorld(),request.destination().x(),request.destination().y(),request.destination().z()),request.speed());}public void stopNativeNavigation(){mob.getPathfinder().stopPathfinding();}
	private AiVector vector(Location location){return new AiVector(location.getX(),location.getY(),location.getZ());}
}
