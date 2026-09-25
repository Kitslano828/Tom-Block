package org.tomdang.quest.orchestration;

import org.tomdang.quest.definition.QuestDefinition;
import org.tomdang.quest.definition.QuestStageDefinition;
import org.tomdang.quest.progress.QuestProgress;
import java.util.UUID;

public record QuestRuntimeContext(UUID playerId, QuestDefinition quest, QuestStageDefinition stage, QuestProgress progress) {}
