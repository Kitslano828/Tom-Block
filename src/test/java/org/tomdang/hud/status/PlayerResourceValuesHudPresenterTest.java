package org.tomdang.hud.status;

import org.junit.jupiter.api.Test;
import org.tomdang.hud.composition.*;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.composition.layout.*;
import org.tomdang.hud.presentation.*;
import org.tomdang.hud.presentation.asset.HudAssetRegistry;
import org.tomdang.hud.presentation.theme.*;
import org.tomdang.hud.text.TomBlockHudFonts;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PlayerResourceValuesHudPresenterTest {
	@Test void centersTwoNumericLabelsAcrossNativeNinetyPixelTracks() {
		HudPresenterRegistry presenters = new HudPresenterRegistry();
		presenters.register(new PlayerResourceValuesHudPresenter());
		HudTheme theme = new HudTheme("test", Map.of(
				StatusHudTokens.HEALTH_TEXT, new org.tomdang.hud.composition.draw.HudTextStyle("health", 0xFF4055, false, true),
				StatusHudTokens.ENERGY_TEXT, new org.tomdang.hud.composition.draw.HudTextStyle("energy", 0x00E6C3, false, true)),
				Map.of(StatusHudTokens.BAR_WIDTH, 90, StatusHudTokens.VALUE_TRACK_WIDTH, 100,
						StatusHudTokens.GAP, 2, StatusHudTokens.ICON_OFFSET_X, 1,
						StatusHudTokens.ICON_OFFSET_Y, 14));
		HudAssetRegistry assets = new HudAssetRegistry();
		assets.register(new org.tomdang.hud.presentation.asset.HudAsset(StatusHudTokens.HEART,
				org.tomdang.hud.presentation.asset.HudAsset.Kind.IMAGE, 15, 16));
		assets.register(new org.tomdang.hud.presentation.asset.HudAsset(StatusHudTokens.ENERGY,
				org.tomdang.hud.presentation.asset.HudAsset.Kind.IMAGE, 15, 16));
		assets.seal();
		PlayerHudSession session = new PlayerHudSession(UUID.randomUUID());
		var model = new PlayerResourceValuesHudModel(400, 100);
		session.put(new PresentedHudElement<>(PlayerResourceValuesHudService.VALUES, model,
				presenters.require(model), theme, assets));
		HudElementLayout placement = new HudElementLayout(true, HudAnchor.BOTTOM_CENTER, 0, -34, 202);
		HudFrame frame = new HudCompositor(new HudLayoutPolicy(Map.of(),
				Map.of(PlayerResourceValuesHudService.VALUES.id(), placement))).compose(session, 1);

		HudElementSnapshot snapshot = frame.region(HudRegion.STATUS).getFirst();
		assertEquals(202, snapshot.bounds().width());
		assertEquals(2, frame.commands().stream().filter(command -> command.command() instanceof HudTextCommand).count());
		assertEquals(List.of("status-heart", "status-energy"), frame.commands().stream()
				.filter(command -> command.command() instanceof org.tomdang.hud.composition.draw.HudImageCommand)
				.map(command -> ((org.tomdang.hud.composition.draw.HudImageCommand) command.command()).assetId()).toList());
		assertEquals(List.of("400", "100"), frame.commands().stream()
				.filter(command -> command.command() instanceof HudTextCommand)
				.filter(command -> !((HudTextCommand) command.command()).style().styleId().endsWith("-edge"))
				.map(command -> ((HudTextCommand) command.command()).text()).toList());
		var textCommands = frame.commands().stream()
				.filter(command -> command.command() instanceof HudTextCommand)
				.filter(command -> !((HudTextCommand) command.command()).style().styleId().endsWith("-edge"))
				.toList();
		var imageCommands = frame.commands().stream()
				.filter(command -> command.command() instanceof org.tomdang.hud.composition.draw.HudImageCommand).toList();
		assertEquals(-11, imageCommands.get(0).bounds().x() - snapshot.bounds().x());
		assertEquals(102 + 96, imageCommands.get(1).bounds().x() - snapshot.bounds().x());
		assertEquals(14, imageCommands.get(0).bounds().y() - textCommands.get(0).bounds().y());
		assertEquals(14, imageCommands.get(1).bounds().y() - textCommands.get(1).bounds().y());
		assertEquals(130, textCommands.get(0).bounds().y());
		assertEquals(144, imageCommands.get(0).bounds().y());
		assertTrue(textCommands.stream().allMatch(command -> !((HudTextCommand) command.command()).style().bold()));
		assertTrue(textCommands.stream().allMatch(command ->
				TomBlockHudFonts.VANILLA.equals(((HudTextCommand) command.command()).style().font())));
		HudRect healthBounds = textCommands.get(0).bounds();
		HudRect energyBounds = textCommands.get(1).bounds();
		int doubledCenterDistance = (2 * energyBounds.x() + energyBounds.width())
				- (2 * healthBounds.x() + healthBounds.width());
		assertTrue(Math.abs(204 - doubledCenterDistance) <= 1,
				"Integer pixel centering may differ by half a pixel between odd/even glyph widths");
	}
}
