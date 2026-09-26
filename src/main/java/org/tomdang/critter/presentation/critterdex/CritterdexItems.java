package org.tomdang.critter.presentation.critterdex;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class CritterdexItems {
    private CritterdexItems() { }

    static ItemStack item(Material material, Component name, List<Component> lore) {
        ItemStack item = ItemStack.of(material);
        var meta = item.getItemMeta();
        meta.displayName(noItalic(name));
        meta.lore(lore.stream().map(CritterdexItems::noItalic).toList());
        item.setItemMeta(meta);
        return item;
    }

    static ItemStack button(Material material, String name, NamedTextColor color) {
        return item(material, Component.text(name, color), List.of());
    }

    static List<Component> wrapped(String text, NamedTextColor color, int width) {
        List<Component> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : text.split("\\s+")) {
            if (!current.isEmpty() && current.length() + word.length() + 1 > width) {
                lines.add(Component.text(current.toString(), color));
                current.setLength(0);
            }
            if (!current.isEmpty()) current.append(' ');
            current.append(word);
        }
        if (!current.isEmpty()) lines.add(Component.text(current.toString(), color));
        return lines;
    }

    static String readable(Iterable<?> values) {
        List<String> result = new ArrayList<>();
        for (Object value : values) result.add(title(value.toString()));
        return result.isEmpty() ? "Unknown" : String.join(", ", result);
    }

    static String title(String value) {
        String[] words = value.toLowerCase(java.util.Locale.ROOT).split("_");
        for (int index = 0; index < words.length; index++) {
            if (!words[index].isEmpty()) words[index] = Character.toUpperCase(words[index].charAt(0)) + words[index].substring(1);
        }
        return String.join(" ", words);
    }

    private static Component noItalic(Component value) { return value.decoration(TextDecoration.ITALIC, false); }
}
