package org.tomdang.hud.composition.layout;

import org.tomdang.hud.composition.HudSize;
import org.tomdang.hud.composition.draw.HudTextCommand;

public interface HudTextMetrics {
	HudSize measure(HudTextCommand text);
}
