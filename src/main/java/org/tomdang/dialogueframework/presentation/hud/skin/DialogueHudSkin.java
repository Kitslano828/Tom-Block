package org.tomdang.dialogueframework.presentation.hud.skin;

import lombok.Getter;

public class DialogueHudSkin {

	@Getter
	private final String skinID;
	@Getter
	private final String backgroundAssetID;

	public DialogueHudSkin(String skinID, String backgroundAssetID) {
		if (skinID == null) throw new IllegalArgumentException("skinID cannot be null");
		if (skinID.isBlank()) throw new IllegalArgumentException("skinID cannot be blank");
		if (backgroundAssetID == null) throw new IllegalArgumentException("backgroundAssetID cannot be null");
		if (backgroundAssetID.isBlank()) throw new IllegalArgumentException("backgroundAssetID cannot be blank");

		this.skinID = skinID;
		this.backgroundAssetID = backgroundAssetID;
	}

}
