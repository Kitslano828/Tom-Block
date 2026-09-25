package org.tomdang.hud.presentation;

import org.junit.jupiter.api.Test;
import org.tomdang.hud.presentation.asset.*;
import org.tomdang.hud.presentation.theme.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class HudConfigurationTest {
	@Test void loadsNamedThemeValues() {
		String yaml = "id: test\ntext-styles:\n  title:\n    color: '#12AB34'\n    bold: true\nmetrics:\n  gap: 4\n";
		HudTheme theme = new HudThemeConfigurationLoader().load(stream(yaml));
		assertEquals(0x12AB34, theme.text(HudTextStyleToken.of("title")).color());
		assertEquals(4, theme.metric(HudMetricToken.of("gap")));
	}
	@Test void loadsAndSealsAssets() {
		String yaml = "assets:\n  tomblock:heart:\n    kind: image\n    width: 9\n    height: 8\n";
		HudAssetRegistry assets = new HudAssetConfigurationLoader().load(stream(yaml));
		assertEquals(9, assets.require(HudAssetId.of("tomblock", "heart")).width());
		assertThrows(IllegalStateException.class, () -> assets.register(
				new HudAsset(HudAssetId.of("test", "late"), HudAsset.Kind.IMAGE, 1, 1)));
	}
	private ByteArrayInputStream stream(String value) { return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)); }
}
