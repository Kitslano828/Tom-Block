package org.tomdang.critter.journal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryCritterJournalRepository implements CritterJournalRepository {
    private final Map<String, CritterJournalEntry> values = new HashMap<>();

    public Optional<CritterJournalEntry> find(UUID playerId, String critterId) {
        return Optional.ofNullable(values.get(playerId + ":" + critterId.toUpperCase(Locale.ROOT)));
    }

    public List<CritterJournalEntry> findAll(UUID playerId) {
        List<CritterJournalEntry> result = new ArrayList<>();
        for (CritterJournalEntry entry : values.values()) if (entry.playerId().equals(playerId)) result.add(entry);
        result.sort(Comparator.comparing(CritterJournalEntry::critterId));
        return List.copyOf(result);
    }

    public void save(CritterJournalEntry entry) {
        values.put(entry.playerId() + ":" + entry.critterId().toUpperCase(Locale.ROOT), entry);
    }
}
