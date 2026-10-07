package org.tomdang.hud.protocol;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class HudProtocolConfigurationTest {
	@Test void loadsVersionAndUniqueGlyphRegistry() {
		HudProtocolConfiguration protocol = load("E000", "E001");
		assertEquals("0.2", protocol.version());
		assertEquals(UUID.fromString("82c70131-0d1c-330f-b91c-65cd253ffda2"), protocol.packId());
		assertEquals('\uE000', protocol.requireGlyph("lab-icon"));
	}
	@Test void rejectsDuplicateAllocations() {
		assertThrows(IllegalArgumentException.class, () -> load("E000", "E000"));
	}
	@Test void productionPaletteCoversEveryEngineOwnedTextSurface() {
		HudProtocolConfiguration protocol = HudProtocolConfiguration.load(
				getClass().getClassLoader().getResourceAsStream("hud-protocol.properties"));
		for (int rgb : new int[]{0xFFFFFF, 0xFF4055, 0x00E6C3, 0xF2AE32, 0xAAAAAA,
				0xFFAA00, 0xFF5555, 0xFFFF55, 0x55FF55, 0x00AAAA, 0x555555,
				0xFCA800, 0xCE3303, 0xECBE74, 0xFC5454, 0xA8A8A8})
			assertDoesNotThrow(() -> protocol.requirePalette(rgb));
	}
	private HudProtocolConfiguration load(String first, String second) {
		String manifest = "protocol.version=0.2\nminecraft.version=26.2\npack.id=82c70131-0d1c-330f-b91c-65cd253ffda2\nfont.id=tomblock:hud_protocol\n"
				+ "marker.red=250\nactionbar.baseline.offset=59\nencoded.ascent=7\nbar.cell.advance=17\n"
				+ "glyph.lab_icon=" + first + "\nglyph.bar_filled=" + second + "\npalette.0=FFFFFF\n";
		return HudProtocolConfiguration.load(new ByteArrayInputStream(manifest.getBytes(StandardCharsets.UTF_8)));
	}
}
