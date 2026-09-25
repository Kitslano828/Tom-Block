package org.tomdang.quest.presentation;
import org.bukkit.Bukkit;import org.tomdang.actorframework.nameplate.presentation.ActorNameplatePresentation;import org.tomdang.playernpc.integration.actor.PlayerNpcActorResolver;import org.tomdang.playernpc.lifecycle.PlayerNpcLifecycleService;import org.tomdang.playernpc.runtime.PlayerNpcRegistry;import org.tomdang.quest.orchestration.*;
public final class QuestActorMarkerRefreshService implements AutoCloseable{
	private final AutoCloseable subscription;private final PlayerNpcRegistry npcs;private final PlayerNpcActorResolver resolver;private final PlayerNpcLifecycleService lifecycle;private final ActorNameplatePresentation nameplates;
	public QuestActorMarkerRefreshService(QuestLifecycleBus events,PlayerNpcRegistry npcs,PlayerNpcActorResolver resolver,PlayerNpcLifecycleService lifecycle,ActorNameplatePresentation nameplates){this.npcs=npcs;this.resolver=resolver;this.lifecycle=lifecycle;this.nameplates=nameplates;subscription=events.subscribe(event->refresh(event.playerId()));}
	public void refresh(java.util.UUID playerId){var viewer=Bukkit.getPlayer(playerId);if(viewer==null)return;for(var npc:npcs.getRegisteredNpcs()){var instance=resolver.resolveByProfileID(npc.getProfileUUID());if(instance==null)continue;nameplates.refreshForViewer(viewer,instance,lifecycle.getNpcLocation(npc.getProfileUUID()),false);}}
	@Override public void close(){try{subscription.close();}catch(Exception exception){throw new IllegalStateException("Could not close quest marker subscription",exception);}}
}
