package org.tomdang.hud.presentation;

import org.junit.jupiter.api.Test;
import org.tomdang.hud.composition.*;
import org.tomdang.hud.composition.draw.*;
import org.tomdang.hud.composition.layout.HudPrimitive;
import org.tomdang.hud.presentation.asset.HudAssetRegistry;
import org.tomdang.hud.presentation.theme.HudTheme;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PresentedHudElementTest {
	record Model(String text) implements HudViewModel {}
	@Test void adaptsSemanticModelWithoutLeakingTransportDetails() {
		HudElementSpec spec = new HudElementSpec(HudElementId.of("test", "model"), HudRegion.STATUS,
				42, true, 100, Set.of(HudRegion.QUEST_TRACKER));
		HudPresenter<Model> presenter = new HudPresenter<>() {
			public Class<Model> modelType() { return Model.class; }
			public HudContent present(Model model, HudPresentationContext context) {
				return new HudContent(new HudPrimitive(HudTextCommand.text(model.text())));
			}
		};
		PresentedHudElement<Model> element = new PresentedHudElement<>(spec, new Model("semantic"), presenter,
				new HudTheme("test", Map.of(), Map.of()), new HudAssetRegistry());
		HudContent content = element.render(new HudRenderContext(UUID.randomUUID(), 1, HudPresentationMode.FULL,
				HudViewport.DEFAULT, 100));
		assertEquals(spec.id(), element.id());
		assertInstanceOf(HudPrimitive.class, content.root());
		assertEquals(Set.of(HudRegion.QUEST_TRACKER), element.suppressesRegions());
	}
}
