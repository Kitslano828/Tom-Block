package org.tomdang.quest.orchestration;

import org.tomdang.quest.definition.QuestDefinition;
import org.tomdang.quest.definition.QuestStageDefinition;
import org.tomdang.quest.progress.QuestProgress;
import java.util.UUID;

public interface QuestRuntimeHooks {
	QuestRuntimeHooks NONE = new QuestRuntimeHooks() {};
	default boolean canStart(UUID playerId, QuestDefinition quest) { return true; }
	default boolean canCompleteStage(UUID playerId, QuestDefinition quest, QuestStageDefinition stage, QuestProgress progress) { return true; }
	default void started(QuestDefinition quest, QuestProgress progress) {}
	default void stageExited(QuestDefinition quest, QuestStageDefinition stage, QuestProgress progress) {}
	default void stageEntered(QuestDefinition quest, QuestStageDefinition stage, QuestProgress progress) {}
	default void completed(QuestDefinition quest, QuestProgress progress) {}
	default void reset(UUID playerId, QuestDefinition quest) {}
}
