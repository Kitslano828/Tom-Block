package org.tomdang.hud.status;

import org.junit.jupiter.api.Test;
import org.tomdang.hud.composition.*;
import org.tomdang.hud.composition.draw.*;
import org.tomdang.hud.composition.layout.*;
import org.tomdang.hud.presentation.*;
import org.tomdang.hud.presentation.asset.*;
import org.tomdang.hud.presentation.theme.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StatusHudPresenterTest {
	@Test void composesOnlyTheTwoCompactFramedTracksOnOneRow() {
		HudPresenterRegistry presenters = new HudPresenterRegistry();
		presenters.register(new HealthHudPresenter());
		presenters.register(new EnergyHudPresenter());
		HudAssetRegistry assets = assets();
		HudTheme theme = theme();
		PlayerHudSession session = new PlayerHudSession(UUID.randomUUID());
		session.put(new PresentedHudElement<>(PlayerStatusHudService.HEALTH, new HealthHudModel(75, 100),
				presenters.require(new HealthHudModel(75, 100)), theme, assets));
		session.put(new PresentedHudElement<>(PlayerStatusHudService.ENERGY, new EnergyHudModel(40, 100),
				presenters.require(new EnergyHudModel(40, 100)), theme, assets));
		HudRegionLayout layout = new HudRegionLayout(HudAnchor.BOTTOM_CENTER, 0, -8, 280, 2,
				HudAxis.HORIZONTAL, 12, HudAlignment.CENTER);
		HudFrame frame = new HudCompositor(new HudLayoutPolicy(Map.of(HudRegion.STATUS, layout))).compose(session, 1);
		assertEquals(2, frame.region(HudRegion.STATUS).size());
		assertTrue(frame.region(HudRegion.STATUS).get(0).bounds().x() < frame.region(HudRegion.STATUS).get(1).bounds().x());
		assertEquals(frame.region(HudRegion.STATUS).get(0).bounds().y(), frame.region(HudRegion.STATUS).get(1).bounds().y());
		assertEquals(8, frame.region(HudRegion.STATUS).get(0).bounds().height());
		assertEquals(2, frame.commands().stream().filter(command -> command.command() instanceof HudProgressBarCommand).count());
		assertEquals(0, frame.commands().stream().filter(command -> command.command() instanceof HudImageCommand).count());
		assertEquals(0, frame.commands().stream().filter(command -> command.command() instanceof HudTextCommand).count());
	}

	@Test void clampsModelsAndFormatsWholeValuesWithoutDecimals() {
		assertEquals(100, new HealthHudModel(120, 100).current());
		assertEquals("100", StatusHudPresenterSupport.number(100));
		assertEquals("99.5", StatusHudPresenterSupport.number(99.5));
		assertThrows(IllegalArgumentException.class, () -> new EnergyHudModel(-1, 100));
	}

	private HudTheme theme() {
		return new HudTheme("test", Map.of(
				StatusHudTokens.HEALTH_TEXT, new HudTextStyle("health", 0xFF4055, false, true),
				StatusHudTokens.ENERGY_TEXT, new HudTextStyle("energy", 0x00E6C3, false, true)),
				Map.of(StatusHudTokens.GAP, 2, StatusHudTokens.BAR_WIDTH, 90));
	}
	private HudAssetRegistry assets() {
		HudAssetRegistry assets = new HudAssetRegistry();
		assets.register(new HudAsset(StatusHudTokens.HEART, HudAsset.Kind.IMAGE, 15, 16));
		assets.register(new HudAsset(StatusHudTokens.ENERGY, HudAsset.Kind.IMAGE, 15, 15));
		assets.register(new HudAsset(StatusHudTokens.HEALTH_BAR, HudAsset.Kind.BAR, 90, 8));
		assets.register(new HudAsset(StatusHudTokens.ENERGY_BAR, HudAsset.Kind.BAR, 90, 8));
		assets.seal();
		return assets;
	}
}
