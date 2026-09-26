package org.tomdang.critter.journal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CritterJournalRepository {
    Optional<CritterJournalEntry> find(UUID playerId, String critterId);
    List<CritterJournalEntry> findAll(UUID playerId);
    void save(CritterJournalEntry entry);
}
