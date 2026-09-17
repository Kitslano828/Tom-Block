package org.tomdang.playernpc.nms;

import com.mojang.authlib.GameProfile;
import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.skin.ActorSkin;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NmsPlayerNpcFactoryTest {
	@Test
	void attachesConfiguredTextureAndLeavesUnconfiguredProfileAlone() {
		GameProfile configured = NmsPlayerNpcFactory.profileWithSkin(
				UUID.randomUUID(), "Npc", new ActorSkin("BLACKSMITH", "dGVzdA==", "signed"));
		assertEquals("dGVzdA==", configured.properties().get("textures").iterator().next().value());
		assertEquals("signed", configured.properties().get("textures").iterator().next().signature());
		GameProfile unsigned = NmsPlayerNpcFactory.profileWithSkin(
				UUID.randomUUID(), "OtherNpc", new ActorSkin("OTHER", "dGVzdDI=", null));
		assertEquals("dGVzdDI=", unsigned.properties().get("textures").iterator().next().value());
		assertFalse(unsigned.properties().get("textures").iterator().next().hasSignature());

		GameProfile unchanged = NmsPlayerNpcFactory.profileWithSkin(UUID.randomUUID(), "DefaultNpc", null);
		assertTrue(unchanged.properties().get("textures").isEmpty());
	}
}
