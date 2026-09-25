package org.tomdang.critter.journal;
import java.util.*;
public interface CritterJournalRepository{Optional<CritterJournalEntry> find(UUID playerId,String critterId);void save(CritterJournalEntry entry);}
