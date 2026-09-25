package org.tomdang.critter.journal;
import java.time.Instant;import java.util.UUID;
public record CritterJournalEntry(UUID playerId,String critterId,CritterKnowledge knowledge,int observations,int successfulHunts,Instant firstSeenAt,Instant lastSeenAt){public CritterJournalEntry{if(playerId==null||critterId==null||critterId.isBlank()||knowledge==null||observations<0||successfulHunts<0||firstSeenAt==null||lastSeenAt==null)throw new IllegalArgumentException("Invalid journal entry");}}
