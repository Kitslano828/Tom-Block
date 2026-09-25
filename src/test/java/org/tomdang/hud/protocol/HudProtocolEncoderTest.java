package org.tomdang.hud.protocol;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;
import org.tomdang.hud.composition.*;
import org.tomdang.hud.composition.draw.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import org.tomdang.hud.text.TomBlockHudFonts;

class HudProtocolEncoderTest {
	private final HudProtocolConfiguration protocol = new HudProtocolConfiguration("0.2", "26.2",
			UUID.fromString("82c70131-0d1c-330f-b91c-65cd253ffda2"), Key.key("tomblock", "hud_protocol"), 250, 59, 7,
			Map.of("lab-icon", '\uE000', "bar-filled", '\uE001', "bar-empty", '\uE002', "panel", '\uE003'),
			Map.of(0xFFFFFF, 0), 17, Map.of("status-health-bar", 9, "status-energy-bar", 9));

	@Test void encodesOnlyRequestedRegionWithProtocolFontAndMarker() {
		HudFrame frame = frame(new HudTextCommand("HELLO", HudTextStyle.DEFAULT, 0, false), HudRegion.DEBUG,
				new HudRect(270, 8, 30, 9));
		Component encoded = new HudProtocolEncoder(protocol, new HudLayoutPolicy(Map.of())).encode(frame, HudRegion.DEBUG);
		Component visual = descendants(encoded).stream().filter(component -> protocol.font().equals(component.style().font())).findFirst().orElseThrow();
		assertEquals("HELLO", ((net.kyori.adventure.text.TextComponent) visual).content());
		assertEquals(250, visual.color().red());
		assertEquals(18, visual.color().green()); // top-right anchor
		assertEquals(136, visual.color().blue()); // y=8, biased by 128
	}

	@Test void encodesThemeColorAsProtocolPaletteWithoutLosingAnchor() {
		HudProtocolConfiguration colored = new HudProtocolConfiguration("0.2", "26.2",
				protocol.packId(), protocol.font(), 250, 59, 7, protocol.glyphs(),
				Map.of(0xFFFFFF, 0, 0xFF4055, 1), 17, protocol.barCellAdvances());
		HudFrame frame = frame(new HudTextCommand("100", new HudTextStyle("health", 0xFF4055, false, true), 20, false),
				HudRegion.STATUS, new HudRect(150, 150, 20, 9));
		Component encoded = new HudProtocolEncoder(colored, new HudLayoutPolicy(Map.of())).encode(frame, HudRegion.STATUS);
		Component visual = descendants(encoded).stream().filter(component -> colored.font().equals(component.style().font())).findFirst().orElseThrow();
		assertEquals(16 + 7 + 9, visual.color().green()); // bottom-center anchor + palette slot one
	}

	@Test void encodesTextWithTheFontSelectedByItsStyle() {
		HudTextStyle qrafty = new HudTextStyle("quest-title", 0xFFFFFF, false, true, TomBlockHudFonts.QRAFTY);
		HudFrame frame = frame(new HudTextCommand("QUEST", qrafty, 180, false), HudRegion.QUEST_TRACKER,
				new HudRect(260, 8, 52, 12));
		Component encoded = new HudProtocolEncoder(protocol, new HudLayoutPolicy(Map.of()))
				.encode(frame, HudRegion.QUEST_TRACKER);
		assertTrue(descendants(encoded).stream().anyMatch(component ->
				TomBlockHudFonts.QRAFTY.equals(component.style().font())
						&& component instanceof net.kyori.adventure.text.TextComponent text
						&& text.content().equals("QUEST")));
	}

	@Test void rejectsCharactersOutsideGeneratedAtlas() {
		HudFrame frame = frame(HudTextCommand.text("not ASCII: ✓"), HudRegion.DEBUG, new HudRect(0, 0, 50, 9));
		assertThrows(IllegalArgumentException.class,
				() -> new HudProtocolEncoder(protocol, new HudLayoutPolicy(Map.of())).encode(frame, HudRegion.DEBUG));
	}

	@Test void resetsWrappedTextByItsActualGlyphAdvanceRatherThanItsCappedLayoutWidth() {
		HudFrame frame = frame(new HudTextCommand("A LONG MESSAGE", HudTextStyle.DEFAULT, 20, true),
				HudRegion.NOTIFICATION, new HudRect(150, 8, 20, 9));
		Component encoded = new HudProtocolEncoder(protocol, new HudLayoutPolicy(Map.of()))
				.encode(frame, HudRegion.NOTIFICATION);
		List<net.kyori.adventure.text.TextComponent> spacers = descendants(encoded).stream()
				.filter(component -> Key.key("tomblock", "spacing").equals(component.style().font()))
				.map(component -> (net.kyori.adventure.text.TextComponent) component).toList();
		String trailingSpacing = spacers.getLast().content();
		assertEquals(-(relativeX(HudRegion.NOTIFICATION, 150) + new org.tomdang.hud.text.MinecraftDefaultTextWidthService()
				.measure("A LONG MESSAGE")), spacingAdvance(trailingSpacing));
	}

	@Test void usesStyleSpecificAdvanceForTenCompactStatusSegments() {
		Map<String, Character> glyphs = new HashMap<>(protocol.glyphs());
		glyphs.put("status-health-bar-filled", '\uE006');
		glyphs.put("status-health-bar-empty", '\uE007');
		glyphs.put("bar-joiner", '\uE00A');
		HudProtocolConfiguration statusProtocol = new HudProtocolConfiguration(protocol.version(), protocol.minecraftVersion(),
				protocol.packId(), protocol.font(), protocol.markerRed(), protocol.actionBarBaselineOffset(),
				protocol.encodedAscent(), glyphs, protocol.palette(), protocol.barCellAdvance(), protocol.barCellAdvances());
		HudFrame frame = frame(new HudProgressBarCommand("status-health-bar", 50, 100, 90, 8),
				HudRegion.STATUS, new HudRect(100, 160, 90, 8));
		Component encoded = new HudProtocolEncoder(statusProtocol, new HudLayoutPolicy(Map.of())).encode(frame, HudRegion.STATUS);
		var visual = descendants(encoded).stream().filter(component -> statusProtocol.font().equals(component.style().font()))
				.map(component -> (net.kyori.adventure.text.TextComponent) component).findFirst().orElseThrow();
		assertEquals(20, visual.content().length());
		assertEquals(("\uE006\uE00A").repeat(5) + ("\uE007\uE00A").repeat(5), visual.content());
	}

	@Test void preservesPrecomposedDialogueComponentInsideTheEngineCarrier() {
		Component dialogue = Component.text("\uE001").font(Key.key("tomblock", "dialogue"));
		HudFrame frame = frame(new HudComponentCommand(dialogue, 256, 64), HudRegion.DIALOGUE,
				new HudRect(32, 66, 256, 64));
		Component encoded = new HudProtocolEncoder(protocol, new HudLayoutPolicy(Map.of())).encode(frame, HudRegion.DIALOGUE);
		assertTrue(descendants(encoded).stream().anyMatch(dialogue::equals));
		var spacers = descendants(encoded).stream()
				.filter(component -> Key.key("tomblock", "spacing").equals(component.style().font()))
				.map(component -> (net.kyori.adventure.text.TextComponent) component).toList();
		assertEquals(-129, spacingAdvance(spacers.getLast().content())); // x=-128, plus 257px cursor advance
	}

	private HudFrame frame(HudDrawCommand command, HudRegion region, HudRect bounds) {
		UUID player = UUID.randomUUID();
		HudElementId owner = HudElementId.of("test", "command");
		HudContent content = new HudContent(new org.tomdang.hud.composition.layout.HudPrimitive(command));
		HudElementSnapshot snapshot = new HudElementSnapshot(owner, region, 1, HudPresentationMode.FULL, content, bounds);
		PositionedHudCommand positioned = new PositionedHudCommand(owner, region, 1, bounds, null, command);
		return new HudFrame(player, 1, HudViewport.DEFAULT, Map.of(region, List.of(snapshot)), List.of(positioned));
	}
	private List<Component> descendants(Component root) {
		List<Component> result = new ArrayList<>();
		result.add(root);
		root.children().forEach(child -> result.addAll(descendants(child)));
		return result;
	}
	private int relativeX(HudRegion region, int x) {
		HudAnchor anchor = new HudLayoutPolicy(Map.of()).region(region).anchor();
		return switch (anchor) {
			case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> x;
			case TOP_CENTER, CENTER, BOTTOM_CENTER -> x - HudViewport.DEFAULT.width() / 2;
			case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> x - HudViewport.DEFAULT.width();
		};
	}
	private int spacingAdvance(String value) {
		Map<Character, Integer> advances = Map.ofEntries(
				Map.entry('\uE108', -256), Map.entry('\uE107', -128), Map.entry('\uE106', -64),
				Map.entry('\uE105', -32), Map.entry('\uE104', -16), Map.entry('\uE103', -8),
				Map.entry('\uE102', -4), Map.entry('\uE101', -2), Map.entry('\uE100', -1),
				Map.entry('\uE118', 256), Map.entry('\uE117', 128), Map.entry('\uE116', 64),
				Map.entry('\uE115', 32), Map.entry('\uE114', 16), Map.entry('\uE113', 8),
				Map.entry('\uE112', 4), Map.entry('\uE111', 2), Map.entry('\uE110', 1));
		return value.chars().map(code -> advances.getOrDefault((char) code, 0)).sum();
	}
}
