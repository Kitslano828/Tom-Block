package org.tomdang.island.preset;

import org.junit.jupiter.api.Test;
import org.tomdang.island.runtime.IslandRole;

import static org.junit.jupiter.api.Assertions.*;

class IslandAccessPolicyTest {
	@Test void evaluatesRolesConsistently() {
		assertFalse(IslandAccessPolicy.DENY.allows(IslandRole.OWNER));
		assertTrue(IslandAccessPolicy.OWNER.allows(IslandRole.OWNER));
		assertFalse(IslandAccessPolicy.OWNER.allows(IslandRole.MEMBER));
		assertTrue(IslandAccessPolicy.MEMBERS.allows(IslandRole.MEMBER));
		assertFalse(IslandAccessPolicy.MEMBERS.allows(IslandRole.VISITOR));
		assertTrue(IslandAccessPolicy.EVERYONE.allows(IslandRole.VISITOR));
	}
}
