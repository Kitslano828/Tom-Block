package org.tomdang.hud.hunting;

import java.util.ArrayList;
import org.tomdang.hud.composition.HudContent;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.composition.layout.HudAlignment;
import org.tomdang.hud.composition.layout.HudAxis;
import org.tomdang.hud.composition.layout.HudPrimitive;
import org.tomdang.hud.composition.layout.HudStack;
import org.tomdang.hud.presentation.HudPresentationContext;
import org.tomdang.hud.presentation.HudPresenter;
import org.tomdang.hud.presentation.theme.HudTextStyleToken;

public final class HuntingHudPresenter implements HudPresenter<HuntingHudModel> {
    @Override public Class<HuntingHudModel> modelType() { return HuntingHudModel.class; }
    @Override public HudContent present(HuntingHudModel model, HudPresentationContext context) {
        var lines = new ArrayList<org.tomdang.hud.composition.layout.HudNode>();
        lines.add(text(model.critterName().toUpperCase(), "heading", context));
        lines.add(text(model.instruction(), "primary", context));
        if (model.cluesFound() < model.cluesRequired()) {
            lines.add(text("TRACKS  " + model.cluesFound() + "/" + model.cluesRequired(), "muted", context));
        } else {
            int filled = Math.min(10, (int) Math.ceil(model.alertness() * 10.0 / model.maximumAlertness()));
            lines.add(text("ALERT  " + "|".repeat(filled) + ".".repeat(10 - filled),
                    filled >= 7 ? "warning" : "muted", context));
            if (model.windowTicksRemaining() > 0) {
                lines.add(text("WINDOW  " + String.format(java.util.Locale.ROOT, "%.1fs",
                        model.windowTicksRemaining() / 20.0), "positive", context));
            }
            lines.add(text("RESULT  " + model.projectedGrade(), "muted", context));
        }
        return new HudContent(new HudStack(HudAxis.VERTICAL, 1, HudAlignment.START, lines));
    }
    private HudPrimitive text(String value, String style, HudPresentationContext context) {
        return new HudPrimitive(new HudTextCommand(value,
                context.theme().text(HudTextStyleToken.of(style)), context.render().availableWidth(), true));
    }
}
