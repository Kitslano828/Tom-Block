package org.tomdang.hud.progression;

import org.tomdang.hud.composition.HudContent;
import org.tomdang.hud.composition.HudInsets;
import org.tomdang.hud.composition.draw.HudPanelCommand;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.composition.layout.HudAlignment;
import org.tomdang.hud.composition.layout.HudOverlay;
import org.tomdang.hud.composition.layout.HudPadding;
import org.tomdang.hud.composition.layout.HudPrimitive;
import org.tomdang.hud.composition.layout.HudStack;
import org.tomdang.hud.presentation.HudPresentationContext;
import org.tomdang.hud.presentation.HudPresenter;
import org.tomdang.hud.presentation.theme.HudTextStyleToken;
import org.tomdang.hud.presentation.theme.HudMetricToken;

import java.util.List;

public final class ProgressionNotificationPresenter implements HudPresenter<ProgressionNotificationModel> {
    @Override public Class<ProgressionNotificationModel> modelType() { return ProgressionNotificationModel.class; }

    @Override
    public HudContent present(ProgressionNotificationModel model, HudPresentationContext context) {
        var children = model.lines().stream().map(line -> {
            int configuredWidth = context.theme().metric(HudMetricToken.of("progression-row-width"));
            int panelWidth = Math.min(context.render().availableWidth(), configuredWidth);
            var text = new HudPadding(new HudInsets(0, 6, 0, 6), new HudPrimitive(new HudTextCommand(line.text(),
                context.theme().text(HudTextStyleToken.of(switch (line.tone()) {
                    case XP -> "positive";
                    case LEVEL -> "heading";
                    case REWARD -> "primary";
                })), Math.max(1, panelWidth - 12), false)));
            return (org.tomdang.hud.composition.layout.HudNode) new HudOverlay(HudAlignment.START, HudAlignment.CENTER,
                    List.of(new HudPrimitive(new HudPanelCommand("progression-row", panelWidth, 9)), text));
        }).toList();
        return new HudContent(HudStack.vertical(2, HudAlignment.START, children));
    }
}
