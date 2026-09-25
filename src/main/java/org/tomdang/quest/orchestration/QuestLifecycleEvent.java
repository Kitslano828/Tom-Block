package org.tomdang.quest.orchestration;
import java.util.UUID;
public record QuestLifecycleEvent(QuestLifecycleType type, UUID playerId, String questId, String stageId) {}
