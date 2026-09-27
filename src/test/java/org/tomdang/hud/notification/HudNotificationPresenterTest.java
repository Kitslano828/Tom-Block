package org.tomdang.hud.notification;

import org.junit.jupiter.api.Test;
import org.tomdang.hud.composition.HudPresentationMode;
import org.tomdang.hud.composition.HudRenderContext;
import org.tomdang.hud.composition.HudViewport;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.composition.draw.HudTextStyle;
import org.tomdang.hud.composition.layout.HudPrimitive;
import org.tomdang.hud.presentation.HudPresentationContext;
import org.tomdang.hud.presentation.asset.HudAssetRegistry;
import org.tomdang.hud.presentation.theme.HudTextStyleToken;
import org.tomdang.hud.presentation.theme.HudTheme;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HudNotificationPresenterTest {
	@Test
	void measuresTheRealSingleLineWidthForCentering() {
		HudTextStyle style = new HudTextStyle("heading", 0xFFAA00, false, false);
		HudTheme theme = new HudTheme("test",
				Map.of(HudTextStyleToken.of("heading"), style), Map.of());
		var context = new HudPresentationContext(
				new HudRenderContext(UUID.randomUUID(), 0, HudPresentationMode.FULL,
						HudViewport.DEFAULT, 240), theme, new HudAssetRegistry());

		var content = new HudNotificationPresenter().present(
				new HudNotificationModel("A notification wider than its configured region", HudNotificationTone.ACCENT),
				context);
		HudTextCommand command = (HudTextCommand) ((HudPrimitive) content.root()).command();

		assertEquals(0, command.maxWidth());
		assertFalse(command.wrap());
	}
}
