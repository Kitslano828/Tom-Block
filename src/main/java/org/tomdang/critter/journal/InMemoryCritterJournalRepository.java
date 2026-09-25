package org.tomdang.critter.journal;
import java.util.*;
public final class InMemoryCritterJournalRepository implements CritterJournalRepository{private final Map<String,CritterJournalEntry> values=new HashMap<>();public Optional<CritterJournalEntry> find(UUID playerId,String critterId){return Optional.ofNullable(values.get(playerId+":"+critterId.toUpperCase(Locale.ROOT)));}public void save(CritterJournalEntry entry){values.put(entry.playerId()+":"+entry.critterId().toUpperCase(Locale.ROOT),entry);}}
