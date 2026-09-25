package org.tomdang.actorframework.nameplate.presentation;
import org.bukkit.entity.Player;import org.tomdang.actorframework.instance.ActorInstance;import org.tomdang.actorframework.nameplate.ActorNameplateLine;import java.util.List;
@FunctionalInterface public interface ActorNameplateLineProvider{ActorNameplateLineProvider NONE=(viewer,instance,moving)->List.of();List<ActorNameplateLine> lines(Player viewer,ActorInstance instance,boolean moving);}
