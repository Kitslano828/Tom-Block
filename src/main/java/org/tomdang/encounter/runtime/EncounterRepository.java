package org.tomdang.encounter.runtime;
import java.util.Collection;
public interface EncounterRepository { Collection<EncounterSession> loadOpen(); void save(EncounterSession session); }
