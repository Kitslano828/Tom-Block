package org.tomdang.actorframework.skin;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ActorSkinConfigurationLoaderTest {
	private final ActorSkinConfigurationLoader loader = new ActorSkinConfigurationLoader();

	@Test
	void loadsReusableSkinAndAllowsEmptyCatalog() {
		assertTrue(loader.load(new StringReader("skins: {}" )).isEmpty());
		Map<String, ActorSkin> skins = loader.load(new StringReader("""
				skins:
				  BLACKSMITH:
				    value: "dGVzdA=="
				    signature: "signed"
				"""));
		assertEquals("dGVzdA==", skins.get("BLACKSMITH").value());
		assertEquals("signed", skins.get("BLACKSMITH").signature());
	}

	@Test
	void rejectsMalformedSkinData() {
		assertThrows(IllegalArgumentException.class, () -> loader.load(new StringReader("other: {}")));
		assertThrows(IllegalArgumentException.class, () -> loader.load(new StringReader("skins:\n  BAD:\n    value: not-base64!")));
		assertThrows(IllegalArgumentException.class, () -> loader.load(new StringReader("skins:\n  BAD:\n    signature: signed")));
	}

	@Test
	void bundledMushroomSkinContainsGeneratedTexture() throws Exception {
		try (var stream = getClass().getResourceAsStream("/actors/skins.yml")) {
			assertNotNull(stream);
			ActorSkin skin = loader.load(new InputStreamReader(stream, StandardCharsets.UTF_8)).get("MUSHROOM_MAN");
			assertNotNull(skin);
			assertNotNull(skin.signature());
			String decoded = new String(Base64.getDecoder().decode(skin.value()), StandardCharsets.UTF_8);
			assertTrue(decoded.contains("6ba51534e986f06c2c72d4cc23c61db14f23a32bfa9bae9a1d1a4509519db189"));
		}
	}
}
