package org.tomdang.quest.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tomdang.quest.definition.QuestDefinition;
import org.tomdang.quest.definition.QuestObjectiveDefinition;
import org.tomdang.quest.definition.QuestObjectiveType;
import org.tomdang.quest.definition.QuestRepeatability;
import org.tomdang.quest.definition.QuestStageDefinition;
import org.tomdang.quest.definition.QuestStartPolicy;
import org.tomdang.quest.definition.QuestActionDefinition;
import org.tomdang.quest.definition.QuestConditionDefinition;
import org.tomdang.quest.definition.QuestRewardDefinition;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class QuestConfigurationLoader {
	public List<QuestDefinition> load(InputStream input) {
		if (input == null) throw new IllegalArgumentException("Quest configuration input cannot be null");
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(
				new InputStreamReader(input, StandardCharsets.UTF_8));
		ConfigurationSection quest = yaml.getConfigurationSection("quest");
		if (quest != null) {
			String id = requiredString(quest, "id");
			return List.of(readQuest(id, quest));
		}
		ConfigurationSection quests = yaml.getConfigurationSection("quests");
		if (quests == null) throw new IllegalArgumentException("Quest file requires a quest section");
		List<QuestDefinition> loaded = new ArrayList<>();
		for (String id : quests.getKeys(false)) loaded.add(readQuest(id, requiredSection(quests, id)));
		return List.copyOf(loaded);
	}

	private QuestDefinition readQuest(String id, ConfigurationSection section) {
		ConfigurationSection stagesSection = requiredSection(section, "stages");
		Map<String, QuestStageDefinition> stages = new LinkedHashMap<>();
		for (String stageId : stagesSection.getKeys(false)) {
			stages.put(stageId, readStage(stageId, requiredSection(stagesSection, stageId)));
		}
		return new QuestDefinition(
				id,
				requiredString(section, "display-name"),
				section.getString("description", ""),
				requiredString(section, "category"),
				readEnum(QuestRepeatability.class, section.getString("repeatability", "ONCE"), id + ".repeatability"),
				readEnum(QuestStartPolicy.class, section.getString("start-policy", "MANUAL"), id + ".start-policy"),
				new LinkedHashSet<>(section.getStringList("prerequisites")),
				requiredString(section, "start-stage"),
				stages,
				readConditions(section, "start-conditions"),
				readRewards(section, "rewards")
		);
	}

	private QuestStageDefinition readStage(String id, ConfigurationSection section) {
		ConfigurationSection objectivesSection = requiredSection(section, "objectives");
		List<QuestObjectiveDefinition> objectives = new ArrayList<>();
		for (String objectiveId : objectivesSection.getKeys(false)) {
			objectives.add(readObjective(objectiveId, requiredSection(objectivesSection, objectiveId)));
		}
		Map<String, String> branches = new LinkedHashMap<>();
		ConfigurationSection branchesSection = section.getConfigurationSection("branches");
		if (branchesSection != null) {
			for (String branch : branchesSection.getKeys(false)) {
				branches.put(branch, requiredString(branchesSection, branch));
			}
		}
		return new QuestStageDefinition(
				id,
				requiredString(section, "display-name"),
				objectives,
				section.getString("next-stage"),
				branches,
				readActions(section, "on-enter"),
				readActions(section, "on-exit"),
				readConditions(section, "completion-conditions")
		);
	}

	private List<QuestActionDefinition> readActions(ConfigurationSection parent, String path) {
		ConfigurationSection section = parent.getConfigurationSection(path);
		if (section == null) return List.of();
		List<QuestActionDefinition> values = new ArrayList<>();
		for (String id : section.getKeys(false)) {
			ConfigurationSection value = requiredSection(section, id);
			values.add(new QuestActionDefinition(id, requiredString(value, "type"), readParameters(value)));
		}
		return List.copyOf(values);
	}

	private List<QuestConditionDefinition> readConditions(ConfigurationSection parent, String path) {
		ConfigurationSection section = parent.getConfigurationSection(path);
		if (section == null) return List.of();
		List<QuestConditionDefinition> values = new ArrayList<>();
		for (String id : section.getKeys(false)) {
			ConfigurationSection value = requiredSection(section, id);
			values.add(new QuestConditionDefinition(id, requiredString(value, "type"), readParameters(value)));
		}
		return List.copyOf(values);
	}

	private List<QuestRewardDefinition> readRewards(ConfigurationSection parent, String path) {
		ConfigurationSection section = parent.getConfigurationSection(path);
		if (section == null) return List.of();
		List<QuestRewardDefinition> values = new ArrayList<>();
		for (String id : section.getKeys(false)) {
			ConfigurationSection value = requiredSection(section, id);
			values.add(new QuestRewardDefinition(id, requiredString(value, "type"), readParameters(value)));
		}
		return List.copyOf(values);
	}

	private Map<String, String> readParameters(ConfigurationSection section) {
		Map<String, String> parameters = new LinkedHashMap<>();
		ConfigurationSection values = section.getConfigurationSection("parameters");
		if (values == null) return Map.of();
		for (String key : values.getKeys(false)) {
			Object value = values.get(key);
			if (value == null || value instanceof ConfigurationSection)
				throw new IllegalArgumentException("Parameter " + key + " must be scalar");
			parameters.put(key, String.valueOf(value));
		}
		return Map.copyOf(parameters);
	}

	private QuestObjectiveDefinition readObjective(String id, ConfigurationSection section) {
		Map<String, String> parameters = new LinkedHashMap<>();
		ConfigurationSection parameterSection = section.getConfigurationSection("parameters");
		if (parameterSection != null) {
			for (String key : parameterSection.getKeys(false)) {
				Object value = parameterSection.get(key);
				if (value == null || value instanceof ConfigurationSection)
					throw new IllegalArgumentException("Objective parameter " + key + " must be a scalar value");
				parameters.put(key, String.valueOf(value));
			}
		}
		long amount = section.getLong("amount", 1);
		return new QuestObjectiveDefinition(
				id,
				readEnum(QuestObjectiveType.class, requiredString(section, "type"), id + ".type"),
				requiredString(section, "target"),
				amount,
				section.getBoolean("optional", false),
				parameters
		);
	}

	private static ConfigurationSection requiredSection(ConfigurationSection parent, String path) {
		ConfigurationSection section = parent.getConfigurationSection(path);
		if (section == null) throw new IllegalArgumentException("Missing configuration section: " + parent.getCurrentPath() + "." + path);
		return section;
	}

	private static String requiredString(ConfigurationSection section, String path) {
		String value = section.getString(path);
		if (value == null || value.isBlank())
			throw new IllegalArgumentException("Missing string: " + section.getCurrentPath() + "." + path);
		return value;
	}

	private static <E extends Enum<E>> E readEnum(Class<E> type, String value, String path) {
		try {
			return Enum.valueOf(type, value.trim().toUpperCase(java.util.Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Invalid " + path + ": " + value, exception);
		}
	}
}
