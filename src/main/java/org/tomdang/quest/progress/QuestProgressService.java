package org.tomdang.quest.progress;

import org.tomdang.quest.definition.QuestDefinition;
import org.tomdang.quest.definition.QuestObjectiveDefinition;
import org.tomdang.quest.definition.QuestRegistry;
import org.tomdang.quest.definition.QuestRepeatability;
import org.tomdang.quest.definition.QuestStageDefinition;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.tomdang.quest.orchestration.QuestRuntimeHooks;

/** Cached, server-authoritative quest state. Signals are persisted only when progress actually changes. */
public final class QuestProgressService {
	private final QuestRegistry registry;
	private final QuestProgressRepository repository;
	private final Clock clock;
	private final QuestRuntimeHooks hooks;
	private final Map<UUID, Map<String, QuestProgress>> players = new ConcurrentHashMap<>();

	public QuestProgressService(QuestRegistry registry, QuestProgressRepository repository) {
		this(registry, repository, Clock.systemUTC(), QuestRuntimeHooks.NONE);
	}

	public QuestProgressService(QuestRegistry registry, QuestProgressRepository repository, QuestRuntimeHooks hooks) {
		this(registry, repository, Clock.systemUTC(), hooks);
	}

	QuestProgressService(QuestRegistry registry, QuestProgressRepository repository, Clock clock) {
		this(registry, repository, clock, QuestRuntimeHooks.NONE);
	}

	QuestProgressService(QuestRegistry registry, QuestProgressRepository repository, Clock clock, QuestRuntimeHooks hooks) {
		if (registry == null || repository == null || clock == null) throw new IllegalArgumentException("Quest dependencies are required");
		this.registry = registry;
		this.repository = repository;
		this.clock = clock;
		this.hooks = hooks == null ? QuestRuntimeHooks.NONE : hooks;
	}

	public void load(UUID playerId) {
		Map<String, QuestProgress> loaded = new LinkedHashMap<>();
		for (QuestProgress progress : repository.load(playerId)) {
			registry.require(progress.questId());
			loaded.put(progress.questId(), progress);
		}
		players.put(playerId, new ConcurrentHashMap<>(loaded));
	}

	public void unload(UUID playerId) {
		players.remove(playerId);
	}

	public boolean isLoaded(UUID playerId) {
		return players.containsKey(playerId);
	}

	public Collection<QuestProgress> progress(UUID playerId) {
		return java.util.List.copyOf(requireLoaded(playerId).values());
	}

	public Optional<QuestProgress> progress(UUID playerId, String questId) {
		return Optional.ofNullable(requireLoaded(playerId).get(questId));
	}

	public boolean isCompleted(UUID playerId, String questId) {
		return progress(playerId, questId).map(value -> value.status() == QuestStatus.COMPLETED).orElse(false);
	}

	public QuestProgress start(UUID playerId, String questId) {
		QuestDefinition definition = registry.require(questId);
		Map<String, QuestProgress> player = requireLoaded(playerId);
		QuestProgress existing = player.get(questId);
		if (existing != null && existing.status() == QuestStatus.ACTIVE) return existing;
		if (existing != null && existing.status() == QuestStatus.COMPLETED
				&& definition.repeatability() == QuestRepeatability.ONCE)
			throw new IllegalStateException("Quest has already been completed: " + questId);
		for (String prerequisite : definition.prerequisites()) {
			if (!isCompleted(playerId, prerequisite))
				throw new IllegalStateException("Quest prerequisite is incomplete: " + prerequisite);
		}
		if (!hooks.canStart(playerId, definition)) throw new IllegalStateException("Quest start conditions are not met: " + questId);
		Instant now = clock.instant();
		QuestProgress started = QuestProgress.start(playerId, questId, definition.startStageId(), now);
		store(started);
		hooks.started(definition, started);
		return started;
	}

	public Collection<QuestProgress> signal(UUID playerId, QuestSignal signal) {
		Map<String, QuestProgress> player = requireLoaded(playerId);
		java.util.List<QuestProgress> changed = new java.util.ArrayList<>();
		for (QuestProgress progress : java.util.List.copyOf(player.values())) {
			if (progress.status() != QuestStatus.ACTIVE) continue;
			QuestDefinition definition = registry.require(progress.questId());
			QuestProgress updated = applySignal(progress, definition, signal);
			if (updated != progress) {
				store(updated);
				applyHooks(progress, updated, definition);
				changed.add(updated);
			}
		}
		return java.util.List.copyOf(changed);
	}

	public QuestProgress chooseBranch(UUID playerId, String questId, String branchId) {
		QuestProgress current = requireActive(playerId, questId);
		QuestDefinition quest = registry.require(questId);
		QuestStageDefinition stage = quest.stages().get(current.currentStageId());
		if (!requiredObjectivesComplete(current, stage)) throw new IllegalStateException("Stage objectives are incomplete");
		if (!hooks.canCompleteStage(playerId, quest, stage, current)) throw new IllegalStateException("Stage conditions are incomplete");
		String target = stage.branches().get(branchId);
		if (target == null) throw new IllegalArgumentException("Unknown branch " + branchId + " for stage " + stage.id());
		QuestProgress advanced = withStage(current, target, clock.instant());
		store(advanced);
		applyHooks(current, advanced, quest);
		return advanced;
	}

	public QuestProgress complete(UUID playerId, String questId) {
		QuestProgress current = requireActive(playerId, questId);
		QuestDefinition definition = registry.require(questId);
		QuestProgress completed = completed(current, clock.instant());
		store(completed);
		applyHooks(current, completed, definition);
		return completed;
	}

	public void reset(UUID playerId, String questId) {
		QuestDefinition definition = registry.require(questId);
		requireLoaded(playerId).remove(questId);
		repository.delete(playerId, questId);
		hooks.reset(playerId, definition);
	}

	private QuestProgress applySignal(QuestProgress current, QuestDefinition quest, QuestSignal signal) {
		QuestStageDefinition stage = quest.stages().get(current.currentStageId());
		Map<QuestObjectiveProgressKey, Long> amounts = new LinkedHashMap<>(current.objectiveProgress());
		boolean changed = false;
		for (QuestObjectiveDefinition objective : stage.objectives()) {
			if (objective.type() != signal.type() || !objective.target().equalsIgnoreCase(signal.target())) continue;
			QuestObjectiveProgressKey key = new QuestObjectiveProgressKey(stage.id(), objective.id());
			long existing = amounts.getOrDefault(key, 0L);
			long updated = Math.min(objective.requiredAmount(), Math.addExact(existing, signal.amount()));
			if (updated != existing) { amounts.put(key, updated); changed = true; }
		}
		if (!changed) return current;
		Instant now = clock.instant();
		QuestProgress updated = new QuestProgress(current.playerId(), current.questId(), current.status(),
				current.currentStageId(), amounts, current.revision() + 1, current.startedAt(), now, current.completedAt());
		if (!requiredObjectivesComplete(updated, stage)) return updated;
		if (!hooks.canCompleteStage(current.playerId(), quest, stage, updated)) return updated;
		if (stage.nextStageId() != null) return withStage(updated, stage.nextStageId(), now);
		if (stage.terminal()) return completed(updated, now);
		return updated; // A completed branching stage waits for an explicit choice.
	}

	private boolean requiredObjectivesComplete(QuestProgress progress, QuestStageDefinition stage) {
		return stage.objectives().stream().filter(objective -> !objective.optional())
				.allMatch(objective -> progress.amount(stage.id(), objective.id()) >= objective.requiredAmount());
	}

	private QuestProgress withStage(QuestProgress current, String stageId, Instant now) {
		return new QuestProgress(current.playerId(), current.questId(), QuestStatus.ACTIVE, stageId,
				current.objectiveProgress(), current.revision() + 1, current.startedAt(), now, null);
	}

	private QuestProgress completed(QuestProgress current, Instant now) {
		return new QuestProgress(current.playerId(), current.questId(), QuestStatus.COMPLETED, current.currentStageId(),
				current.objectiveProgress(), current.revision() + 1, current.startedAt(), now, now);
	}

	private void applyHooks(QuestProgress previous, QuestProgress updated, QuestDefinition quest) {
		boolean stageChanged = !previous.currentStageId().equals(updated.currentStageId());
		boolean completed = previous.status() != QuestStatus.COMPLETED && updated.status() == QuestStatus.COMPLETED;
		if (stageChanged || completed) hooks.stageExited(quest,
				quest.stages().get(previous.currentStageId()), updated);
		if (stageChanged && updated.status() == QuestStatus.ACTIVE) hooks.stageEntered(quest,
				quest.stages().get(updated.currentStageId()), updated);
		if (completed) hooks.completed(quest, updated);
	}

	private QuestProgress requireActive(UUID playerId, String questId) {
		QuestProgress progress = requireLoaded(playerId).get(questId);
		if (progress == null || progress.status() != QuestStatus.ACTIVE)
			throw new IllegalStateException("Quest is not active: " + questId);
		return progress;
	}

	private Map<String, QuestProgress> requireLoaded(UUID playerId) {
		Map<String, QuestProgress> loaded = players.get(playerId);
		if (loaded == null) throw new IllegalStateException("Quest progress is not loaded for " + playerId);
		return loaded;
	}

	private void store(QuestProgress progress) {
		requireLoaded(progress.playerId()).put(progress.questId(), progress);
		repository.save(progress);
	}
}
