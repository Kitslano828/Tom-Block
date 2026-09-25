package org.tomdang.quest.orchestration;

import org.tomdang.quest.definition.*;
import org.tomdang.quest.progress.QuestProgress;

import java.util.UUID;

public final class QuestOrchestrationService implements QuestRuntimeHooks {
	private final QuestHandlerRegistry<QuestAction> actions;
	private final QuestHandlerRegistry<QuestCondition> conditions;
	private final QuestHandlerRegistry<QuestReward> rewards;
	private final QuestRewardLedger rewardLedger;
	private final QuestLifecycleBus lifecycle;

	public QuestOrchestrationService(QuestHandlerRegistry<QuestAction> actions,
			QuestHandlerRegistry<QuestCondition> conditions, QuestHandlerRegistry<QuestReward> rewards,
			QuestRewardLedger rewardLedger, QuestLifecycleBus lifecycle) {
		this.actions = actions; this.conditions = conditions; this.rewards = rewards;
		this.rewardLedger = rewardLedger; this.lifecycle = lifecycle;
	}

	public void validate(QuestRegistry definitions) {
		for (QuestDefinition quest : definitions.all()) {
			quest.startConditions().forEach(value -> requireCondition(quest.id(), value));
			quest.rewards().forEach(value -> { if (!rewards.contains(value.type())) throw unknown(quest.id(), "reward", value.type()); });
			for (QuestStageDefinition stage : quest.stages().values()) {
				stage.enterActions().forEach(value -> requireAction(quest.id(), stage.id(), value));
				stage.exitActions().forEach(value -> requireAction(quest.id(), stage.id(), value));
				stage.completionConditions().forEach(value -> requireCondition(quest.id(), value));
			}
		}
		actions.seal(); conditions.seal(); rewards.seal();
	}

	@Override public boolean canStart(UUID playerId, QuestDefinition quest) {
		QuestRuntimeContext context = new QuestRuntimeContext(playerId, quest, quest.stages().get(quest.startStageId()), null);
		return quest.startConditions().stream().allMatch(value -> conditions.require(value.type()).test(context, value.parameters()));
	}

	@Override public boolean canCompleteStage(UUID playerId, QuestDefinition quest, QuestStageDefinition stage, QuestProgress progress) {
		QuestRuntimeContext context = new QuestRuntimeContext(playerId, quest, stage, progress);
		return stage.completionConditions().stream().allMatch(value -> conditions.require(value.type()).test(context, value.parameters()));
	}

	@Override public void started(QuestDefinition quest, QuestProgress progress) {
		publish(QuestLifecycleType.STARTED, progress);
		stageEntered(quest, quest.stages().get(progress.currentStageId()), progress);
	}

	@Override public void stageExited(QuestDefinition quest, QuestStageDefinition stage, QuestProgress progress) {
		execute(stage.exitActions(), new QuestRuntimeContext(progress.playerId(), quest, stage, progress));
		publish(QuestLifecycleType.STAGE_EXITED, progress, stage.id());
	}

	@Override public void stageEntered(QuestDefinition quest, QuestStageDefinition stage, QuestProgress progress) {
		execute(stage.enterActions(), new QuestRuntimeContext(progress.playerId(), quest, stage, progress));
		publish(QuestLifecycleType.STAGE_ENTERED, progress, stage.id());
	}

	@Override public void completed(QuestDefinition quest, QuestProgress progress) {
		QuestRuntimeContext context = new QuestRuntimeContext(progress.playerId(), quest,
				quest.stages().get(progress.currentStageId()), progress);
		for (QuestRewardDefinition reward : quest.rewards()) {
			String deliveryId = quest.id() + ":" + progress.startedAt() + ":" + reward.id();
			if (!rewardLedger.claim(progress.playerId(), deliveryId)) continue;
			try { rewards.require(reward.type()).grant(context, reward.parameters()); }
			catch (RuntimeException exception) { rewardLedger.release(progress.playerId(), deliveryId); throw exception; }
		}
		publish(QuestLifecycleType.COMPLETED, progress);
	}

	@Override public void reset(UUID playerId, QuestDefinition quest) {
		lifecycle.publish(new QuestLifecycleEvent(QuestLifecycleType.RESET, playerId, quest.id(), null));
	}

	private void execute(java.util.List<QuestActionDefinition> definitions, QuestRuntimeContext context) {
		for (QuestActionDefinition definition : definitions) actions.require(definition.type()).execute(context, definition.parameters());
	}
	private void publish(QuestLifecycleType type, QuestProgress progress) { publish(type, progress, progress.currentStageId()); }
	private void publish(QuestLifecycleType type, QuestProgress progress, String stageId) {
		lifecycle.publish(new QuestLifecycleEvent(type, progress.playerId(), progress.questId(), stageId));
	}
	private void requireAction(String quest, String stage, QuestActionDefinition value) {
		if (!actions.contains(value.type())) throw unknown(quest + "/" + stage, "action", value.type());
	}
	private void requireCondition(String quest, QuestConditionDefinition value) {
		if (!conditions.contains(value.type())) throw unknown(quest, "condition", value.type());
	}
	private IllegalArgumentException unknown(String source, String kind, String type) {
		return new IllegalArgumentException(source + " references unknown quest " + kind + " handler " + type);
	}
}
