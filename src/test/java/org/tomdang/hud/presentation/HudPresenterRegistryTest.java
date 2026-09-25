package org.tomdang.hud.presentation;

import org.junit.jupiter.api.Test;
import org.tomdang.hud.composition.HudContent;
import org.tomdang.hud.composition.layout.HudPrimitive;
import org.tomdang.hud.composition.draw.HudTextCommand;
import static org.junit.jupiter.api.Assertions.*;

class HudPresenterRegistryTest {
	record Model(String value) implements HudViewModel {}
	@Test void rejectsDuplicatesMissingPresentersAndMutationAfterSeal() {
		HudPresenterRegistry registry = new HudPresenterRegistry();
		HudPresenter<Model> presenter = presenter();
		registry.register(presenter);
		assertSame(presenter, registry.require(new Model("first")));
		assertThrows(IllegalArgumentException.class, () -> registry.register(presenter));
		registry.seal();
		assertThrows(IllegalStateException.class, () -> registry.register(presenter));
		assertThrows(IllegalArgumentException.class, () -> registry.require(new Other()));
	}
	private HudPresenter<Model> presenter() { return new HudPresenter<>() {
		public Class<Model> modelType() { return Model.class; }
		public HudContent present(Model model, HudPresentationContext context) {
			return new HudContent(new HudPrimitive(HudTextCommand.text(model.value())));
		}
	}; }
	record Other() implements HudViewModel {}
}
