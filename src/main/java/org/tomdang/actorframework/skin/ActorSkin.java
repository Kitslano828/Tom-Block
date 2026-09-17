package org.tomdang.actorframework.skin;

import java.util.Base64;

/** Texture data is kept independent of the version-specific NPC profile implementation. */
public record ActorSkin(String id, String value, String signature) {
	public ActorSkin {
		if (id == null || id.isBlank() || value == null || value.isBlank())
			throw new IllegalArgumentException("Skin id and texture value are required");
		try {
			Base64.getDecoder().decode(value);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Skin " + id + " has an invalid base64 texture value", exception);
		}
		if (signature != null && signature.isBlank())
			throw new IllegalArgumentException("Skin " + id + " has a blank signature");
	}
}
