package org.tomdang.player.playeractionbar;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.tomdang.hud.text.MinecraftDefaultTextWidthService;

public final class StatusHudRenderer {

    private static final Key STATUS_FONT = Key.key("tomblock", "status_hud");
    private static final Key STATUS_TEXT_FONT = Key.key("tomblock", "status_text");
    private static final TextColor ENERGY_COLOR = TextColor.color(0x00E6C3);
    private final MinecraftDefaultTextWidthService textWidthService = new MinecraftDefaultTextWidthService();

    private static final char HEART = '\uE100';
    private static final char ENERGY = '\uE103';
    private static final char SPACE_POSITIVE_1 = '\uE120';
    private static final char SPACE_NEGATIVE_1 = '\uE128';

    /**
     * Draws one zero-width, screen-centred HUD. Positioning glyphs let each value
     * and icon be anchored independently of the text width instead of relying on
     * ordinary spaces. The vanilla HUD sprites supply the ten bar cells below it.
     */
    public Component render(int currentHealth, int currentEnergy) {
        String health = String.valueOf(Math.max(0, currentHealth));
        String energy = String.valueOf(Math.max(0, currentEnergy));

        int healthWidth = estimatedTextWidth(health);
        int energyWidth = estimatedTextWidth(energy);

        int cursor = 0;
        Component result = Component.empty();

        result = result.append(shift(-112 - cursor));
        cursor = -112;
        result = result.append(icon(HEART));
        cursor += 16;

        int healthStart = -50 - healthWidth / 2;
        result = result.append(shift(healthStart - cursor));
        cursor = healthStart;
        result = result.append(value(health, NamedTextColor.RED));
        cursor += healthWidth;

        int energyStart = 50 - energyWidth / 2;
        result = result.append(shift(energyStart - cursor));
        cursor = energyStart;
        result = result.append(value(energy, ENERGY_COLOR));
        cursor += energyWidth;

        result = result.append(shift(96 - cursor));
        cursor = 96;
        result = result.append(icon(ENERGY));
        cursor += 15;

        return result.append(shift(-cursor));
    }

    private Component value(String text, TextColor color) {
        return Component.text(text, color).font(STATUS_TEXT_FONT);
    }

    private Component icon(char character) {
        return Component.text(String.valueOf(character), NamedTextColor.WHITE).font(STATUS_FONT);
    }

    private Component shift(int pixels) {
        if (pixels == 0) return Component.empty();
        StringBuilder glyphs = new StringBuilder();
        int remaining = Math.abs(pixels);
        int bit = 0;
        char base = pixels > 0 ? SPACE_POSITIVE_1 : SPACE_NEGATIVE_1;
        while (remaining != 0) {
            if ((remaining & 1) != 0) glyphs.append((char)(base + bit));
            remaining >>>= 1;
            bit++;
        }
        return Component.text(glyphs.toString()).font(STATUS_FONT);
    }

    private int estimatedTextWidth(String text) {
        return textWidthService.measure(text);
    }
}
