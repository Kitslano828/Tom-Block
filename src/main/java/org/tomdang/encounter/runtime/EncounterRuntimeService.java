package org.tomdang.encounter.runtime;

import org.tomdang.encounter.definition.*;
import org.tomdang.gameplay.event.GameplayEventBus;
import org.tomdang.gameplay.event.type.EncounterCompleted;
import org.tomdang.platform.runtime.RuntimeScope;
import java.time.*;
import java.util.*;
import org.tomdang.activity.ActivityAccessService;

public final class EncounterRuntimeService implements AutoCloseable {
	private final EncounterRegistry definitions; private final EncounterBehaviorRegistry behaviors;
	private final EncounterRepository repository; private final GameplayEventBus events; private final Clock clock;
	private final Map<UUID,Active> active=new LinkedHashMap<>();
	private final ActivityAccessService activityAccess = new ActivityAccessService();
	public EncounterRuntimeService(EncounterRegistry definitions,EncounterBehaviorRegistry behaviors,EncounterRepository repository,GameplayEventBus events){this(definitions,behaviors,repository,events,Clock.systemUTC());}
	EncounterRuntimeService(EncounterRegistry definitions,EncounterBehaviorRegistry behaviors,EncounterRepository repository,GameplayEventBus events,Clock clock){this.definitions=definitions;this.behaviors=behaviors;this.repository=repository;this.events=events;this.clock=clock;}
	public void recover(){for(EncounterSession stored:repository.loadOpen()){EncounterDefinition definition=definitions.require(stored.definitionId());Instant now=clock.instant();EncounterSession session=copy(stored,EncounterState.SUSPENDED,now,now.plus(definition.disconnectGrace()),"SERVER_RESTART");repository.save(session);active.put(session.instanceId(),new Active(session,new RuntimeScope()));}}
	public EncounterSession start(String definitionId,UUID owner,Set<UUID> participants){EncounterDefinition definition=definitions.require(definitionId);Set<UUID> members=new LinkedHashSet<>(participants==null?Set.of():participants);members.add(owner);if(definition.mode()==EncounterMode.PLAYER&&members.size()!=1)throw new IllegalArgumentException("Player encounter accepts only its owner");if(findFor(owner).isPresent())throw new IllegalStateException("Player already has an active encounter");Instant now=clock.instant();EncounterSession session=new EncounterSession(UUID.randomUUID(),definition.id(),owner,members,EncounterState.ACTIVE,now,now,now.plus(definition.timeout()),null,0,null);Active runtime=new Active(session,new RuntimeScope());active.put(session.instanceId(),runtime);repository.save(session);behaviors.require(definition.behavior()).start(context(runtime));return session;}
	public Optional<EncounterSession> findFor(UUID player){return active.values().stream().map(value->value.session).filter(value->value.participants().contains(player)&&!value.terminal()).findFirst();}
	public void disconnect(UUID player){for(Active runtime:new ArrayList<>(active.values()))if(runtime.session.participants().contains(player)&&runtime.session.state()==EncounterState.ACTIVE){EncounterDefinition definition=definitions.require(runtime.session.definitionId());Instant now=clock.instant();runtime.session=copy(runtime.session,EncounterState.SUSPENDED,now,now.plus(definition.disconnectGrace()),null);repository.save(runtime.session);behaviors.require(definition.behavior()).suspend(context(runtime));}}
	public void reconnect(UUID player){for(Active runtime:active.values())if(runtime.session.ownerId().equals(player)&&runtime.session.state()==EncounterState.SUSPENDED){runtime.session=copy(runtime.session,EncounterState.ACTIVE,clock.instant(),null,null);repository.save(runtime.session);behaviors.require(definitions.require(runtime.session.definitionId()).behavior()).resume(context(runtime));}}
	public void complete(UUID instanceId){Active runtime=require(instanceId);EncounterDefinition definition=definitions.require(runtime.session.definitionId());runtime.session=copy(runtime.session,EncounterState.COMPLETED,clock.instant(),null,null);repository.save(runtime.session);EncounterRuntimeContext context=context(runtime);behaviors.require(definition.behavior()).complete(context);for(UUID player:activityAccess.rewardRecipients(context.activity()))events.publish(new EncounterCompleted(player,definition.id()));remove(runtime);}
	public void fail(UUID instanceId,String reason){Active runtime=require(instanceId);EncounterDefinition definition=definitions.require(runtime.session.definitionId());runtime.session=copy(runtime.session,EncounterState.FAILED,clock.instant(),null,reason);repository.save(runtime.session);behaviors.require(definition.behavior()).fail(context(runtime),reason);remove(runtime);}
	public void tick(){Instant now=clock.instant();for(Active runtime:new ArrayList<>(active.values())){if(!now.isBefore(runtime.session.expiresAt()))fail(runtime.session.instanceId(),"TIMEOUT");else if(runtime.session.state()==EncounterState.SUSPENDED&&runtime.session.disconnectDeadline()!=null&&!now.isBefore(runtime.session.disconnectDeadline()))fail(runtime.session.instanceId(),"DISCONNECT_TIMEOUT");}}
	private Active require(UUID id){Active value=active.get(id);if(value==null)throw new IllegalArgumentException("Unknown active encounter "+id);return value;}
	private EncounterRuntimeContext context(Active value){return new EncounterRuntimeContext(definitions.require(value.session.definitionId()),value.session,value.scope);}
	private EncounterSession copy(EncounterSession value,EncounterState state,Instant now,Instant deadline,String reason){return new EncounterSession(value.instanceId(),value.definitionId(),value.ownerId(),value.participants(),state,value.startedAt(),now,value.expiresAt(),deadline,value.revision()+1,reason);}
	private void remove(Active value){active.remove(value.session.instanceId());value.scope.close();}
	@Override public void close(){for(Active value:new ArrayList<>(active.values()))value.scope.close();active.clear();}
	private static final class Active{private EncounterSession session;private final RuntimeScope scope;private Active(EncounterSession session,RuntimeScope scope){this.session=session;this.scope=scope;}}
}
