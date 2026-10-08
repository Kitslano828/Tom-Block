package org.tomdang.hud.calendar;

import java.util.List;
import java.util.Locale;
import org.tomdang.hud.composition.HudContent;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.composition.layout.HudAlignment;
import org.tomdang.hud.composition.layout.HudAxis;
import org.tomdang.hud.composition.layout.HudPrimitive;
import org.tomdang.hud.composition.layout.HudStack;
import org.tomdang.hud.presentation.HudPresentationContext;
import org.tomdang.hud.presentation.HudPresenter;
import org.tomdang.hud.presentation.theme.HudTextStyleToken;

public final class CalendarHudPresenter implements HudPresenter<CalendarHudModel> {
    @Override public Class<CalendarHudModel> modelType() { return CalendarHudModel.class; }
    @Override public HudContent present(CalendarHudModel model, HudPresentationContext context) {
        String month = model.monthName().length() <= 3 ? model.monthName().toUpperCase(Locale.ROOT)
                : model.monthName().substring(0, 3).toUpperCase(Locale.ROOT);
        var date = text(model.season() + "  |  " + month + " " + model.day(), "heading", context);
        var time = text(model.period() + "  |  %02d:%02d".formatted(model.hour(), model.minute()), "muted", context);
        return new HudContent(new HudStack(HudAxis.VERTICAL, 1, HudAlignment.START, List.of(date, time)));
    }
    private static HudPrimitive text(String value, String style, HudPresentationContext context) {
        return new HudPrimitive(new HudTextCommand(value, context.theme().text(HudTextStyleToken.of(style)),
                context.render().availableWidth(), false));
    }
}
