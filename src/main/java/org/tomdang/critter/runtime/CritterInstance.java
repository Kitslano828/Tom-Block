package org.tomdang.critter.runtime;
import java.time.Instant;import java.util.UUID;
public record CritterInstance(UUID instanceId,String definitionId,UUID ownerId,CritterState state,int relocations,Instant stateSince){public CritterInstance{if(instanceId==null||definitionId==null||definitionId.isBlank()||ownerId==null||state==null||relocations<0||stateSince==null)throw new IllegalArgumentException("Invalid critter instance");}}
