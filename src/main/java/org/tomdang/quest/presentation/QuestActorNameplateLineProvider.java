package org.tomdang.quest.presentation;
import net.kyori.adventure.text.Component;import net.kyori.adventure.text.format.*;import org.bukkit.entity.Player;import org.tomdang.actorframework.instance.ActorInstance;import org.tomdang.actorframework.nameplate.*;import org.tomdang.actorframework.nameplate.presentation.ActorNameplateLineProvider;import java.util.*;
public final class QuestActorNameplateLineProvider implements ActorNameplateLineProvider{
	private final QuestActorMarkerResolver markers;public QuestActorNameplateLineProvider(QuestActorMarkerResolver markers){this.markers=markers;}
	public List<ActorNameplateLine> lines(Player viewer,ActorInstance instance,boolean moving){return markers.resolve(viewer.getUniqueId(),instance.getActorDefinition().getActorID()).map(marker->{TextColor color=switch(marker){case QUEST->NamedTextColor.GOLD;case CONTINUE->NamedTextColor.YELLOW;case COMPLETE->NamedTextColor.GREEN;};return List.of(new ActorNameplateLine(ActorNameplateLineRole.STATUS,Component.text(marker.name(),color).decorate(TextDecoration.BOLD),true));}).orElseGet(List::of);}
}
