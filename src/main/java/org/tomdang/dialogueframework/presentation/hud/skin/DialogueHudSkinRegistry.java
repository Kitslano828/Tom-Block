package org.tomdang.dialogueframework.presentation.hud.skin;

import java.util.HashMap;
import java.util.Map;

public class DialogueHudSkinRegistry {

	private final Map<String, DialogueHudSkin> hudSkins = new HashMap<>();

	public void registerSkin(DialogueHudSkin hudSkin) {
		if (hudSkin == null) throw new IllegalArgumentException("Hud Skin cannot be null");
		String skinID = hudSkin.getSkinID();
		if (hudSkins.containsKey(skinID)) throw new IllegalStateException("Hud Skin ID already exist");
		hudSkins.put(skinID, hudSkin);
	}

	public DialogueHudSkin lookupSkin(String skinID) {
		if (skinID == null) throw new IllegalArgumentException("Skin ID cannot be null");
		if (skinID.isBlank()) throw new IllegalArgumentException("Skin ID cannot be blank");
		return hudSkins.get(skinID);
	}

	public boolean isSkinRegistered(String skinID) {
		if (skinID == null) throw new IllegalArgumentException("Skin ID cannot be null");
		if (skinID.isBlank()) throw new IllegalArgumentException("Skin ID cannot be blank");
		return hudSkins.containsKey(skinID);
	}

}
