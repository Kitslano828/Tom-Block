package org.tomdang.hud;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Prevents gameplay features from bypassing TomBlock's authoritative HUD engine. */
class HudOwnershipArchitectureTest {
	@Test void onlyTheHudTransportWritesMinecraftsActionBar() throws Exception {
		Path root = Path.of("src/main/java");
		List<String> violations = new ArrayList<>();
		try (var files = Files.walk(root)) {
			files.filter(path -> path.toString().endsWith(".java"))
					.filter(path -> !path.endsWith(Path.of("hud/composition/transport/ActionBarHudProtocolTransport.java")))
					.forEach(path -> inspect(path, List.of(".sendActionBar(", ".showBossBar(", ".hideBossBar(",
							".showTitle(", ".sendTitle("), violations));
		}
		assertTrue(violations.isEmpty(), "Direct Minecraft HUD writes bypass the engine: " + violations);
	}

	@Test void productionCodeDoesNotClaimTheSidebarOutsideTheIsolatedMapExperiment() throws Exception {
		Path root = Path.of("src/main/java");
		List<String> violations = new ArrayList<>();
		try (var files = Files.walk(root)) {
			files.filter(path -> path.toString().endsWith(".java"))
					.filter(path -> !path.endsWith(Path.of("worldmap/MapTestService.java")))
					.forEach(path -> inspect(path, List.of("DisplaySlot.SIDEBAR"), violations));
		}
		assertTrue(violations.isEmpty(), "Visible sidebar writes bypass the engine: " + violations);
		assertFalse(Files.readString(Path.of("src/main/java/org/tomdang/TomBlock.java")).contains("new MapTestService"),
				"The isolated scoreboard experiment must not be wired into the production plugin");
	}

	@Test void nativePlayerResourcesAreNotRegisteredAsCustomHudElements() throws Exception {
		String bootstrap = Files.readString(Path.of("src/main/java/org/tomdang/TomBlock.java"));
		assertFalse(bootstrap.contains("new org.tomdang.hud.status.HealthHudPresenter"));
		assertFalse(bootstrap.contains("new org.tomdang.hud.status.EnergyHudPresenter"));
		assertFalse(bootstrap.contains("new org.tomdang.hud.status.PlayerStatusHudService"));
		assertTrue(bootstrap.contains("new org.tomdang.hud.notification.HudNotificationPresenter"));
		assertTrue(bootstrap.contains("new org.tomdang.hud.dialogue.HudDialoguePresenter"));
	}

	private void inspect(Path path, List<String> forbidden, List<String> violations) {
		try {
			String source = Files.readString(path);
			for (String token : forbidden) if (source.contains(token)) violations.add(path + " -> " + token);
		} catch (java.io.IOException exception) { throw new java.io.UncheckedIOException(exception); }
	}
}
