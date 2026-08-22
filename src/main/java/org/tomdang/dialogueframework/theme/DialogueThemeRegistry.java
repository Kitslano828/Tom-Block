package org.tomdang.dialogueframework.theme;

import java.util.HashMap;
import java.util.Map;

public class DialogueThemeRegistry {

	private final Map<String, DialogueThemeDefinition> themeDefinitions = new HashMap<>();
	private final Map<String, String > sourceBindings = new HashMap<>();

	public void registerTheme(DialogueThemeDefinition definition) {
		if (definition == null) throw new IllegalArgumentException("Theme cannot be null");

		String themeID = definition.themeID();
		if (themeDefinitions.containsKey(themeID)) throw new IllegalStateException(themeID + " already exists");

		themeDefinitions.put(themeID, definition);
	}

	public void bindSource(String sourceID, String themeID) {
		if (sourceID == null) throw new IllegalArgumentException("Source ID cannot be null!");
		if (sourceID.isBlank()) throw new IllegalArgumentException("Source ID cannot be blank!");
		if (themeID == null) throw new IllegalArgumentException("Theme ID cannot be null!");
		if (themeID.isBlank()) throw new IllegalArgumentException("Theme ID cannot be blank!");

		if (!themeDefinitions.containsKey(themeID)) throw new IllegalStateException(themeID + " does not exist");
		if (sourceBindings.containsKey(sourceID)) throw new IllegalStateException(sourceID + " already contains a binding");

		sourceBindings.put(sourceID, themeID);
	}

	public DialogueThemeDefinition lookupTheme(String themeID) {
		if (themeID == null) throw new IllegalArgumentException("Theme ID cannot be null!");
		if (themeID.isBlank()) throw new IllegalArgumentException("Theme ID cannot be blank!");
		return themeDefinitions.get(themeID);
	}

	public DialogueThemeDefinition resolveThemeForSource(String sourceID) {
		if (sourceID == null) throw new IllegalArgumentException("Source ID cannot be null!");
		if (sourceID.isBlank()) throw new IllegalArgumentException("Source ID cannot be blank!");

		String themeID = sourceBindings.get(sourceID);
		if (themeID == null) return null;
		DialogueThemeDefinition definition = themeDefinitions.get(themeID);

		if (definition == null) throw new IllegalStateException("Definition does not exist");
		return definition;
	}


}
