package org.tomdang.quest.definition;

import java.util.Collection;
import org.tomdang.platform.identity.ContentKey;
import org.tomdang.platform.registry.DefinitionRegistry;
import org.tomdang.platform.registry.SealableRegistry;
import org.tomdang.platform.validation.ValidationReport;

import java.util.Optional;

public final class QuestRegistry implements DefinitionRegistry<QuestDefinition> {
	private final SealableRegistry<QuestDefinition> quests = new SealableRegistry<>("quest");

	public void register(QuestDefinition quest) {
		if (quest == null) throw new IllegalArgumentException("Quest cannot be null");
		register(QuestKeys.fromStoredId(quest.id()), quest);
	}

	@Override public void register(ContentKey<QuestDefinition> key, QuestDefinition quest) {
		if (quest == null) throw new IllegalArgumentException("Quest cannot be null");
		ContentKey<QuestDefinition> definitionKey = QuestKeys.fromStoredId(quest.id());
		if (!key.equals(definitionKey)) throw new IllegalArgumentException(
				"Quest key " + key + " does not match definition id " + quest.id());
		quests.register(key, quest);
	}

	@Override public Optional<QuestDefinition> find(ContentKey<QuestDefinition> key) { return quests.find(key); }
	@Override public QuestDefinition require(ContentKey<QuestDefinition> key) { return quests.require(key); }
	public Optional<QuestDefinition> find(String id) { return find(QuestKeys.fromStoredId(id)); }
	public QuestDefinition require(String id) {
		return require(QuestKeys.fromStoredId(id));
	}

	@Override public Collection<QuestDefinition> all() { return quests.all(); }
	@Override public Collection<ContentKey<QuestDefinition>> keys() { return quests.keys(); }
	@Override public boolean isSealed() { return quests.isSealed(); }
	@Override public void seal() { quests.seal(); }

	public void validatePrerequisites() {
		ValidationReport report = new ValidationReport();
		for (QuestDefinition quest : quests.all()) {
			for (String prerequisite : quest.prerequisites()) {
				if (find(prerequisite).isEmpty()) report.error(quest.id(), "Unknown prerequisite " + prerequisite);
			}
		}
		try { report.throwIfInvalid(); }
		catch (org.tomdang.platform.validation.ContentValidationException exception) {
			throw new IllegalArgumentException(exception.getMessage(), exception);
		}
	}

	public void validateAndSeal() {
		validatePrerequisites();
		seal();
	}
}
