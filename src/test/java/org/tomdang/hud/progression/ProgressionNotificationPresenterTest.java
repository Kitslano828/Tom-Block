package org.tomdang.hud.progression;

import org.junit.jupiter.api.Test;
import org.tomdang.hud.composition.*;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.composition.layout.HudStack;
import org.tomdang.hud.composition.layout.HudOverlay;
import org.tomdang.hud.composition.layout.HudPadding;
import org.tomdang.hud.composition.layout.HudPrimitive;
import org.tomdang.hud.composition.draw.HudPanelCommand;
import org.tomdang.hud.presentation.HudPresentationContext;
import org.tomdang.hud.presentation.asset.HudAssetRegistry;
import org.tomdang.hud.presentation.theme.HudMetricToken;
import org.tomdang.hud.presentation.theme.HudTextStyleToken;
import org.tomdang.hud.presentation.theme.HudTheme;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgressionNotificationPresenterTest {
    @Test
    void rendersQueuedAwardsAsLeftAlignedRowsWithSemanticStyles() {
        var xp = new org.tomdang.hud.composition.draw.HudTextStyle("xp", 0x55FF55, false, true);
        var level = new org.tomdang.hud.composition.draw.HudTextStyle("level", 0xF2AE32, true, true);
        var reward = new org.tomdang.hud.composition.draw.HudTextStyle("reward", 0xFFFFFF, false, true);
        HudTheme theme = new HudTheme("test", Map.of(
                HudTextStyleToken.of("positive"), xp,
                HudTextStyleToken.of("heading"), level,
                HudTextStyleToken.of("primary"), reward), Map.of(HudMetricToken.of("progression-row-width"), 176));
        HudAssetRegistry assets = new HudAssetRegistry(); assets.seal();
        var context = new HudPresentationContext(new HudRenderContext(UUID.randomUUID(), 1,
                HudPresentationMode.FULL, HudViewport.DEFAULT, 180), theme, assets);
        var model = new ProgressionNotificationModel(List.of(
                new ProgressionNotificationLine("+15 HUNTING EXP", ProgressionNotificationTone.XP),
                new ProgressionNotificationLine("HUNTING LEVEL 2", ProgressionNotificationTone.LEVEL),
                new ProgressionNotificationLine("+2 WILD FORTUNE", ProgressionNotificationTone.REWARD)));

        HudStack stack = (HudStack) new ProgressionNotificationPresenter().present(model, context).root();
        assertEquals(3, stack.children().size());
        assertEquals(List.of("+15 HUNTING EXP", "HUNTING LEVEL 2", "+2 WILD FORTUNE"),
                stack.children().stream().map(node -> (HudOverlay) node)
                        .map(overlay -> (HudPadding) overlay.children().get(1))
                        .map(padding -> (HudPrimitive) padding.child())
                        .map(primitive -> ((HudTextCommand) primitive.command()).text()).toList());
        assertEquals(List.of(176, 176, 176), stack.children().stream().map(node -> (HudOverlay) node)
                .map(overlay -> (HudPrimitive) overlay.children().getFirst())
                .map(primitive -> ((HudPanelCommand) primitive.command()).width()).toList());
    }
}
