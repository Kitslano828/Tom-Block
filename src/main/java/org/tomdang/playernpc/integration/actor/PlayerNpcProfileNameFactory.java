package org.tomdang.playernpc.integration.actor;

import java.util.UUID;

public class PlayerNpcProfileNameFactory {

	private static final String PREFIX = "tb_";
	private static final int UUID_CHARACTERS = 13;

	public String create(UUID actorInstanceID) {
		if (actorInstanceID == null) throw new IllegalArgumentException("actorInstanceID cannot be null");

		String compactUUID = actorInstanceID.toString().replace("-", "");
		return PREFIX + compactUUID.substring(0, UUID_CHARACTERS);
	}
}
