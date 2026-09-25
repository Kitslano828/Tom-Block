package org.tomdang.quest.definition;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record QuestDefinition(
		String id,
		String displayName,
		String description,
		String category,
		QuestRepeatability repeatability,
		QuestStartPolicy startPolicy,
		Set<String> prerequisites,
		String startStageId,
		Map<String, QuestStageDefinition> stages,
		List<QuestConditionDefinition> startConditions,
		List<QuestRewardDefinition> rewards
) {
	public QuestDefinition {
		if (id == null || id.isBlank()) throw new IllegalArgumentException("Quest id cannot be blank");
		if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("Quest display name cannot be blank");
		if (description == null) throw new IllegalArgumentException("Quest description cannot be null");
		if (category == null || category.isBlank()) throw new IllegalArgumentException("Quest category cannot be blank");
		if (repeatability == null) throw new IllegalArgumentException("Quest repeatability cannot be null");
		if (startPolicy == null) throw new IllegalArgumentException("Quest start policy cannot be null");
		if (prerequisites == null || prerequisites.stream().anyMatch(value -> value == null || value.isBlank()))
			throw new IllegalArgumentException("Quest prerequisites cannot be null or blank");
		if (startStageId == null || startStageId.isBlank()) throw new IllegalArgumentException("Quest start stage cannot be blank");
		if (stages == null || stages.isEmpty()) throw new IllegalArgumentException("Quest requires stages");
		if (startConditions == null || rewards == null) throw new IllegalArgumentException("Quest orchestration definitions cannot be null");
		if (startConditions.stream().anyMatch(java.util.Objects::isNull) || rewards.stream().anyMatch(java.util.Objects::isNull))
			throw new IllegalArgumentException("Quest orchestration definitions cannot contain null");
		if (startConditions.stream().map(QuestConditionDefinition::id).distinct().count() != startConditions.size())
			throw new IllegalArgumentException("Quest start condition ids must be unique");
		if (rewards.stream().map(QuestRewardDefinition::id).distinct().count() != rewards.size())
			throw new IllegalArgumentException("Quest reward ids must be unique");
		prerequisites = Set.copyOf(prerequisites);
		stages = Map.copyOf(stages);
		startConditions = List.copyOf(startConditions);
		rewards = List.copyOf(rewards);
		validateGraph(startStageId, stages);
	}

	public QuestDefinition(String id, String displayName, String description, String category,
			QuestRepeatability repeatability, Set<String> prerequisites, String startStageId,
			Map<String, QuestStageDefinition> stages) {
		this(id, displayName, description, category, repeatability, QuestStartPolicy.MANUAL,
				prerequisites, startStageId, stages, List.of(), List.of());
	}

	public QuestDefinition(String id, String displayName, String description, String category,
			QuestRepeatability repeatability, QuestStartPolicy startPolicy, Set<String> prerequisites,
			String startStageId, Map<String, QuestStageDefinition> stages) {
		this(id, displayName, description, category, repeatability, startPolicy, prerequisites,
				startStageId, stages, List.of(), List.of());
	}

	private static void validateGraph(String startStageId, Map<String, QuestStageDefinition> stages) {
		if (!stages.containsKey(startStageId)) throw new IllegalArgumentException("Unknown start stage: " + startStageId);
		for (Map.Entry<String, QuestStageDefinition> entry : stages.entrySet()) {
			if (!entry.getKey().equals(entry.getValue().id()))
				throw new IllegalArgumentException("Stage map key does not match stage id: " + entry.getKey());
			QuestStageDefinition stage = entry.getValue();
			if (stage.nextStageId() != null && !stages.containsKey(stage.nextStageId()))
				throw new IllegalArgumentException("Stage " + stage.id() + " references unknown next stage " + stage.nextStageId());
			for (String target : stage.branches().values()) {
				if (!stages.containsKey(target))
					throw new IllegalArgumentException("Stage " + stage.id() + " references unknown branch stage " + target);
			}
		}
		Set<String> reachable = new LinkedHashSet<>();
		ArrayDeque<String> pending = new ArrayDeque<>();
		pending.add(startStageId);
		while (!pending.isEmpty()) {
			String current = pending.removeFirst();
			if (!reachable.add(current)) continue;
			QuestStageDefinition stage = stages.get(current);
			if (stage.nextStageId() != null) pending.add(stage.nextStageId());
			pending.addAll(stage.branches().values());
		}
		Set<String> unreachable = new LinkedHashSet<>(stages.keySet());
		unreachable.removeAll(reachable);
		if (!unreachable.isEmpty()) throw new IllegalArgumentException("Unreachable quest stages: " + unreachable);
		if (stages.values().stream().noneMatch(QuestStageDefinition::terminal))
			throw new IllegalArgumentException("Quest requires at least one terminal stage");
	}
}
