package org.tomdang.hud.hunting;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.tomdang.hud.composition.HudPresentationMode;
import org.tomdang.hud.composition.HudRenderContext;
import org.tomdang.hud.composition.HudViewport;
import org.tomdang.hud.composition.layout.HudStack;
import org.tomdang.hud.presentation.HudPresentationContext;
import org.tomdang.hud.presentation.asset.HudAssetRegistry;
import org.tomdang.hud.presentation.theme.HudTextStyleToken;
import org.tomdang.hud.presentation.theme.HudTheme;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HuntingHudPresenterTest {
    @Test void trackingShowsCluesWhileCaptureShowsTimingAndGrade() {
        var style = new org.tomdang.hud.composition.draw.HudTextStyle("test", 0xffffff, false, true);
        var theme = new HudTheme("test", Map.of(
                HudTextStyleToken.of("heading"), style,
                HudTextStyleToken.of("primary"), style,
                HudTextStyleToken.of("muted"), style,
                HudTextStyleToken.of("warning"), style,
                HudTextStyleToken.of("positive"), style), Map.of());
        var assets = new HudAssetRegistry(); assets.seal();
        var context = new HudPresentationContext(new HudRenderContext(UUID.randomUUID(), 1,
                HudPresentationMode.FULL, HudViewport.DEFAULT, 170), theme, assets);
        var presenter = new HuntingHudPresenter();
        HudStack tracking = (HudStack) presenter.present(new HuntingHudModel(
                "Mossback", "FOLLOW THE TRAIL", 1, 3, 0, 100, 0, "CLEAN"), context).root();
        HudStack capture = (HudStack) presenter.present(new HuntingHudModel(
                "Mossback", "INTERACT NOW", 3, 3, 20, 100, 35, "STANDARD"), context).root();
        assertEquals(3, tracking.children().size());
        assertEquals(5, capture.children().size());
    }
}
