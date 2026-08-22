package org.tomdang.dialogueframework.theme;

public record DialogueThemeDefinition(String themeID, String speakerName, String hudSkinID) {

	public DialogueThemeDefinition {
		if (themeID == null) throw new IllegalArgumentException("ThemeID cannot be null");
		if (themeID.isBlank()) throw new IllegalArgumentException("ThemeID cannot be blank");
		if (speakerName == null) throw new IllegalArgumentException("Speaker Name cannot be null");
		if (speakerName.isBlank()) throw new IllegalArgumentException("Speaker Name cannot be blank");
		if (hudSkinID == null) throw new IllegalArgumentException("HudSkinID cannot be null");
		if (hudSkinID.isBlank()) throw new IllegalArgumentException("HudSkinID cannot be blank");
	}
}
