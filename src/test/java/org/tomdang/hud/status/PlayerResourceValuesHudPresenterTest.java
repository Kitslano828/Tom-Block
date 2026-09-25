package org.tomdang.hud.status;

import org.junit.jupiter.api.Test;
import org.tomdang.hud.composition.*;
import org.tomdang.hud.composition.draw.HudTextCommand;
import org.tomdang.hud.composition.layout.*;
import org.tomdang.hud.presentation.*;
import org.tomdang.hud.presentation.asset.HudAssetRegistry;
import org.tomdang.hud.presentation.theme.*;
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
						StatusHudTokens.GAP, 2));
		HudAssetRegistry assets = new HudAssetRegistry();
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
		assertEquals(List.of("400", "100"), frame.commands().stream()
				.filter(command -> command.command() instanceof HudTextCommand)
				.map(command -> ((HudTextCommand) command.command()).text()).toList());
		assertEquals(102, frame.commands().get(1).bounds().x() - frame.commands().get(0).bounds().x());
	}
}
