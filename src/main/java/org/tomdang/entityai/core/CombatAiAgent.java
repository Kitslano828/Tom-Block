package org.tomdang.entityai.core;

import java.util.UUID;

/** Optional capability implemented by adapters that can execute a native combat attack. */
public interface CombatAiAgent extends AiAgent {
    boolean attack(UUID targetId);
}
