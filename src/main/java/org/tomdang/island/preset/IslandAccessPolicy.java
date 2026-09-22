package org.tomdang.island.preset;

import org.tomdang.island.runtime.IslandRole;

public enum IslandAccessPolicy {
	DENY, OWNER, MEMBERS, EVERYONE;

	public boolean allows(IslandRole role) {
		return switch (this) {
			case DENY -> false;
			case OWNER -> role == IslandRole.OWNER;
			case MEMBERS -> role == IslandRole.OWNER || role == IslandRole.MEMBER;
			case EVERYONE -> true;
		};
	}
}
