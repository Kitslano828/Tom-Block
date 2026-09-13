package org.tomdang.dialogueframework.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class DialogueConfigurationLoader {

	public List<DialogueConfigurationDefinition> loadDefinitions(Reader reader) {
		if (reader == null) throw new IllegalArgumentException("reader cannot be null");

		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
		ConfigurationSection dialoguesSection = configuration.getConfigurationSection("dialogues");
		if (dialoguesSection == null) {
			throw new IllegalArgumentException("Configuration must contain a dialogues section");
		}

		List<DialogueConfigurationDefinition> definitions = new ArrayList<>();
		for (String dialogueID : dialoguesSection.getKeys(false)) {
			ConfigurationSection dialogueSection = requireSection(dialoguesSection, dialogueID, "Dialogue " + dialogueID);
			String startingNodeID = requireString(dialogueSection, "starting-node", "Dialogue " + dialogueID);
			ConfigurationSection nodesSection = dialogueSection.getConfigurationSection("nodes");
			if (nodesSection == null || nodesSection.getKeys(false).isEmpty()) {
				throw new IllegalArgumentException("Dialogue " + dialogueID + " must contain a non-empty nodes section");
			}

			List<DialogueNodeConfigurationDefinition> nodes = new ArrayList<>();
			for (String nodeID : nodesSection.getKeys(false)) {
				ConfigurationSection nodeSection = requireSection(nodesSection, nodeID, "Node " + dialogueID + "." + nodeID);
				String text = requireString(nodeSection, "text", "Node " + dialogueID + "." + nodeID);
				List<DialogueChoiceConfigurationDefinition> choices = loadChoices(dialogueID, nodeID, nodeSection);
				nodes.add(new DialogueNodeConfigurationDefinition(nodeID, text, choices));
			}

			definitions.add(new DialogueConfigurationDefinition(dialogueID, startingNodeID, nodes));
		}
		return List.copyOf(definitions);
	}

	private List<DialogueChoiceConfigurationDefinition> loadChoices(
			String dialogueID,
			String nodeID,
			ConfigurationSection nodeSection
	) {
		ConfigurationSection choicesSection = nodeSection.getConfigurationSection("choices");
		if (choicesSection == null) return List.of();

		List<DialogueChoiceConfigurationDefinition> choices = new ArrayList<>();
		for (String choiceID : choicesSection.getKeys(false)) {
			String path = "Choice " + dialogueID + "." + nodeID + "." + choiceID;
			ConfigurationSection choiceSection = requireSection(choicesSection, choiceID, path);
			String displayText = requireString(choiceSection, "display-text", path);
			String nextNodeID = optionalString(choiceSection, "next-node", path);
			String actionID = optionalString(choiceSection, "action", path);
			choices.add(new DialogueChoiceConfigurationDefinition(choiceID, displayText, nextNodeID, actionID));
		}
		return List.copyOf(choices);
	}

	private ConfigurationSection requireSection(ConfigurationSection parent, String key, String owner) {
		ConfigurationSection section = parent.getConfigurationSection(key);
		if (section == null) throw new IllegalArgumentException(owner + " must be a section");
		return section;
	}

	private String requireString(ConfigurationSection section, String key, String owner) {
		String value = section.getString(key);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(owner + " has a missing or invalid " + key);
		}
		return value;
	}

	private String optionalString(ConfigurationSection section, String key, String owner) {
		if (!section.contains(key)) return null;
		String value = section.getString(key);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(owner + " has an invalid " + key);
		}
		return value;
	}
}
