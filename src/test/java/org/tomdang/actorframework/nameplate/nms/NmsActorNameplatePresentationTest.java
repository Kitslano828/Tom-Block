package org.tomdang.actorframework.nameplate.nms;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.definition.ActorDefinition;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.nameplate.ActorNameplate;
import org.tomdang.actorframework.nameplate.ActorNameplateLine;
import org.tomdang.actorframework.nameplate.ActorNameplateLineRole;
import org.tomdang.actorframework.nameplate.layout.ActorNameplateLayout;
import org.tomdang.actorframework.nameplate.layout.ActorNameplateLayoutCalculator;
import org.tomdang.actorframework.nameplate.layout.ActorNameplateLinePlacement;
import org.tomdang.actorframework.nameplate.nms.runtime.NmsActorNameplateLine;
import org.tomdang.actorframework.nameplate.nms.runtime.NmsActorNameplateLineRegistry;
import org.tomdang.actorframework.nameplate.presentation.ActorNameplateLinePresentationHandle;
import org.tomdang.actorframework.nameplate.presentation.ActorNameplatePresentationRegistry;
import org.tomdang.actorframework.nameplate.presentation.ActorNameplateViewerHandle;
import org.tomdang.actorframework.nameplate.presentation.ActorNameplateViewerKey;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NmsActorNameplatePresentationTest {

	@Test
	void constructorRejectsNullDependencies() {
		ActorNameplatePresentationRegistry presentationRegistry = new ActorNameplatePresentationRegistry();
		NmsActorNameplateLineRegistry runtimeRegistry = new NmsActorNameplateLineRegistry();
		ActorNameplateLayoutCalculator layoutCalculator = mock(ActorNameplateLayoutCalculator.class);
		NmsActorNameplateLineFactory lineFactory = mock(NmsActorNameplateLineFactory.class);
		NmsActorNameplateViewer nameplateViewer = mock(NmsActorNameplateViewer.class);
		ActorNameplateLayout layout = new ActorNameplateLayout(2.3, 0.3);
		Server server = mock(Server.class);

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> new NmsActorNameplatePresentation(null, runtimeRegistry, layoutCalculator, lineFactory, nameplateViewer, layout, server)),
				() -> assertThrows(IllegalArgumentException.class, () -> new NmsActorNameplatePresentation(presentationRegistry, null, layoutCalculator, lineFactory, nameplateViewer, layout, server)),
				() -> assertThrows(IllegalArgumentException.class, () -> new NmsActorNameplatePresentation(presentationRegistry, runtimeRegistry, null, lineFactory, nameplateViewer, layout, server)),
				() -> assertThrows(IllegalArgumentException.class, () -> new NmsActorNameplatePresentation(presentationRegistry, runtimeRegistry, layoutCalculator, null, nameplateViewer, layout, server)),
				() -> assertThrows(IllegalArgumentException.class, () -> new NmsActorNameplatePresentation(presentationRegistry, runtimeRegistry, layoutCalculator, lineFactory, null, layout, server)),
				() -> assertThrows(IllegalArgumentException.class, () -> new NmsActorNameplatePresentation(presentationRegistry, runtimeRegistry, layoutCalculator, lineFactory, nameplateViewer, null, server)),
				() -> assertThrows(IllegalArgumentException.class, () -> new NmsActorNameplatePresentation(presentationRegistry, runtimeRegistry, layoutCalculator, lineFactory, nameplateViewer, layout, null))
		);
	}

	@Test
	void showToViewerCreatesRegistersAndShowsCompleteNameplate() {
		ActorNameplatePresentationRegistry presentationRegistry = new ActorNameplatePresentationRegistry();
		NmsActorNameplateLineRegistry runtimeRegistry = new NmsActorNameplateLineRegistry();
		ActorNameplateLayoutCalculator layoutCalculator = mock(ActorNameplateLayoutCalculator.class);
		NmsActorNameplateLineFactory lineFactory = mock(NmsActorNameplateLineFactory.class);
		NmsActorNameplateViewer nameplateViewer = mock(NmsActorNameplateViewer.class);
		ActorNameplateLayout layout = new ActorNameplateLayout(2.3, 0.3);
		NmsActorNameplatePresentation presentation = new NmsActorNameplatePresentation(
				presentationRegistry,
				runtimeRegistry,
				layoutCalculator,
				lineFactory,
				nameplateViewer,
				layout,
				mock(Server.class)
		);

		UUID instanceUUID = UUID.randomUUID();
		UUID viewerUUID = UUID.randomUUID();
		UUID presentationUUID = UUID.randomUUID();
		int entityID = 42;
		boolean isMoving = true;

		Player viewer = mock(Player.class);
		when(viewer.getUniqueId()).thenReturn(viewerUUID);
		World world = mock(World.class);
		Location currentLocation = new Location(world, 10.0, 64.0, -5.0);

		ActorNameplateLine nameLine = new ActorNameplateLine(
				ActorNameplateLineRole.NAME,
				Component.text("Blacksmith"),
				true
		);
		ActorNameplate nameplate = new ActorNameplate(List.of(nameLine));
		ActorDefinition definition = mock(ActorDefinition.class);
		when(definition.getActorNameplate()).thenReturn(nameplate);
		ActorInstance instance = mock(ActorInstance.class);
		when(instance.getInstanceID()).thenReturn(instanceUUID);
		when(instance.getActorDefinition()).thenReturn(definition);

		List<ActorNameplateLine> visibleLines = List.of(nameLine);
		ActorNameplateLinePlacement placement = new ActorNameplateLinePlacement(nameLine, 2.3);
		when(layoutCalculator.calculateLinePlacements(visibleLines, layout)).thenReturn(List.of(placement));

		NmsActorNameplateLine runtimeLine = mock(NmsActorNameplateLine.class);
		when(runtimeLine.getPresentationUUID()).thenReturn(presentationUUID);
		when(runtimeLine.getEntityID()).thenReturn(entityID);
		when(lineFactory.create(currentLocation, placement)).thenReturn(runtimeLine);

		boolean result = presentation.showToViewer(viewer, instance, currentLocation, isMoving);

		assertTrue(result);
		verify(layoutCalculator).calculateLinePlacements(visibleLines, layout);
		verify(lineFactory).create(currentLocation, placement);
		verify(nameplateViewer).show(viewer, runtimeLine);
		assertSame(runtimeLine, runtimeRegistry.get(presentationUUID));

		ActorNameplateViewerKey viewerKey = new ActorNameplateViewerKey(instanceUUID, viewerUUID);
		ActorNameplateViewerHandle storedHandle = presentationRegistry.lookup(viewerKey);
		assertNotNull(storedHandle);
		assertEquals(viewerKey, storedHandle.actorNameplateViewerKey());
		assertEquals(1, storedHandle.linePresentationHandles().size());
		assertEquals(presentationUUID, storedHandle.linePresentationHandles().getFirst().presentationUUID());
		assertEquals(entityID, storedHandle.linePresentationHandles().getFirst().entityID());
		assertEquals(placement.verticalOffset(), storedHandle.linePresentationHandles().getFirst().verticalOffset());
		assertEquals(isMoving, storedHandle.inMovingState());
	}

	@Test
	void showToViewerReturnsFalseWhenPresentationAlreadyExists() {
		ActorNameplatePresentationRegistry presentationRegistry = new ActorNameplatePresentationRegistry();
		NmsActorNameplateLineRegistry runtimeRegistry = new NmsActorNameplateLineRegistry();
		ActorNameplateLayoutCalculator layoutCalculator = mock(ActorNameplateLayoutCalculator.class);
		NmsActorNameplateLineFactory lineFactory = mock(NmsActorNameplateLineFactory.class);
		NmsActorNameplateViewer nameplateViewer = mock(NmsActorNameplateViewer.class);
		NmsActorNameplatePresentation presentation = new NmsActorNameplatePresentation(
				presentationRegistry, runtimeRegistry, layoutCalculator, lineFactory,
				nameplateViewer, new ActorNameplateLayout(2.3, 0.3), mock(Server.class)
		);

		UUID instanceUUID = UUID.randomUUID();
		UUID viewerUUID = UUID.randomUUID();
		Player viewer = mock(Player.class);
		when(viewer.getUniqueId()).thenReturn(viewerUUID);
		ActorInstance instance = mock(ActorInstance.class);
		when(instance.getInstanceID()).thenReturn(instanceUUID);
		ActorNameplateViewerKey key = new ActorNameplateViewerKey(instanceUUID, viewerUUID);
		ActorNameplateViewerHandle existingHandle = new ActorNameplateViewerHandle(
				key,
				List.of(new ActorNameplateLinePresentationHandle(UUID.randomUUID(), 8, 2.3)),
				false
		);
		presentationRegistry.register(existingHandle);
		Location location = new Location(mock(World.class), 1.0, 2.0, 3.0);

		boolean result = presentation.showToViewer(viewer, instance, location, false);

		assertFalse(result);
		assertSame(existingHandle, presentationRegistry.lookup(key));
		assertTrue(runtimeRegistry.getAllLines().isEmpty());
		verifyNoInteractions(layoutCalculator, lineFactory, nameplateViewer);
	}

	@Test
	void showToViewerRejectsInvalidInputsBeforeCreatingAnything() {
		ActorNameplatePresentationRegistry presentationRegistry = new ActorNameplatePresentationRegistry();
		NmsActorNameplateLineRegistry runtimeRegistry = new NmsActorNameplateLineRegistry();
		ActorNameplateLayoutCalculator layoutCalculator = mock(ActorNameplateLayoutCalculator.class);
		NmsActorNameplateLineFactory lineFactory = mock(NmsActorNameplateLineFactory.class);
		NmsActorNameplateViewer nameplateViewer = mock(NmsActorNameplateViewer.class);
		NmsActorNameplatePresentation presentation = new NmsActorNameplatePresentation(
				presentationRegistry, runtimeRegistry, layoutCalculator, lineFactory,
				nameplateViewer, new ActorNameplateLayout(2.3, 0.3), mock(Server.class)
		);
		Player viewer = mock(Player.class);
		when(viewer.getUniqueId()).thenReturn(UUID.randomUUID());
		ActorInstance instance = mock(ActorInstance.class);
		when(instance.getInstanceID()).thenReturn(UUID.randomUUID());
		World world = mock(World.class);

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> presentation.showToViewer(null, instance, new Location(world, 1.0, 2.0, 3.0), false)),
				() -> assertThrows(IllegalArgumentException.class, () -> presentation.showToViewer(viewer, null, new Location(world, 1.0, 2.0, 3.0), false)),
				() -> assertThrows(IllegalArgumentException.class, () -> presentation.showToViewer(viewer, instance, null, false)),
				() -> assertThrows(IllegalArgumentException.class, () -> presentation.showToViewer(viewer, instance, new Location(null, 1.0, 2.0, 3.0), false)),
				() -> assertThrows(IllegalArgumentException.class, () -> presentation.showToViewer(viewer, instance, new Location(world, Double.NaN, 2.0, 3.0), false)),
				() -> assertThrows(IllegalArgumentException.class, () -> presentation.showToViewer(viewer, instance, new Location(world, 1.0, Double.POSITIVE_INFINITY, 3.0), false)),
				() -> assertThrows(IllegalArgumentException.class, () -> presentation.showToViewer(viewer, instance, new Location(world, 1.0, 2.0, Double.NEGATIVE_INFINITY), false))
		);

		assertTrue(presentationRegistry.findActorHandles(instance.getInstanceID()).isEmpty());
		assertTrue(runtimeRegistry.getAllLines().isEmpty());
		verifyNoInteractions(layoutCalculator, lineFactory, nameplateViewer);
	}

	@Test
	void showToViewerRollsBackAllRegisteredLinesWhenLaterShowFails() {
		ActorNameplatePresentationRegistry presentationRegistry = new ActorNameplatePresentationRegistry();
		NmsActorNameplateLineRegistry runtimeRegistry = new NmsActorNameplateLineRegistry();
		ActorNameplateLayoutCalculator layoutCalculator = mock(ActorNameplateLayoutCalculator.class);
		NmsActorNameplateLineFactory lineFactory = mock(NmsActorNameplateLineFactory.class);
		NmsActorNameplateViewer nameplateViewer = mock(NmsActorNameplateViewer.class);
		ActorNameplateLayout layout = new ActorNameplateLayout(2.3, 0.3);
		NmsActorNameplatePresentation presentation = new NmsActorNameplatePresentation(
				presentationRegistry, runtimeRegistry, layoutCalculator, lineFactory, nameplateViewer, layout, mock(Server.class)
		);

		Player viewer = mock(Player.class);
		UUID viewerUUID = UUID.randomUUID();
		when(viewer.getUniqueId()).thenReturn(viewerUUID);
		ActorInstance instance = mock(ActorInstance.class);
		UUID instanceUUID = UUID.randomUUID();
		when(instance.getInstanceID()).thenReturn(instanceUUID);
		ActorDefinition definition = mock(ActorDefinition.class);
		when(instance.getActorDefinition()).thenReturn(definition);

		ActorNameplateLine statusLine = new ActorNameplateLine(ActorNameplateLineRole.STATUS, Component.text("QUEST"), false);
		ActorNameplateLine nameLine = new ActorNameplateLine(ActorNameplateLineRole.NAME, Component.text("Blacksmith"), true);
		when(definition.getActorNameplate()).thenReturn(new ActorNameplate(List.of(statusLine, nameLine)));
		ActorNameplateLinePlacement firstPlacement = new ActorNameplateLinePlacement(statusLine, 2.6);
		ActorNameplateLinePlacement secondPlacement = new ActorNameplateLinePlacement(nameLine, 2.3);
		when(layoutCalculator.calculateLinePlacements(List.of(statusLine, nameLine), layout))
				.thenReturn(List.of(firstPlacement, secondPlacement));

		Location location = new Location(mock(World.class), 10.0, 64.0, -5.0);
		NmsActorNameplateLine firstRuntimeLine = runtimeLine(101);
		NmsActorNameplateLine secondRuntimeLine = runtimeLine(102);
		when(lineFactory.create(location, firstPlacement)).thenReturn(firstRuntimeLine);
		when(lineFactory.create(location, secondPlacement)).thenReturn(secondRuntimeLine);
		RuntimeException failure = new RuntimeException("metadata send failed");
		doThrow(failure).when(nameplateViewer).show(viewer, secondRuntimeLine);

		RuntimeException thrown = assertThrows(RuntimeException.class, () ->
				presentation.showToViewer(viewer, instance, location, false)
		);

		assertSame(failure, thrown);
		assertTrue(runtimeRegistry.getAllLines().isEmpty());
		assertFalse(presentationRegistry.doesPresentationHandleExist(new ActorNameplateViewerKey(instanceUUID, viewerUUID)));
		verify(nameplateViewer).hide(viewer, secondRuntimeLine);
		verify(nameplateViewer).hide(viewer, firstRuntimeLine);
	}

	@Test
	void hideFromViewerHidesAllLinesAndRemovesBothRegistryLevels() {
		HideFixture fixture = new HideFixture();
		NmsActorNameplateLine firstLine = runtimeLine(201);
		NmsActorNameplateLine secondLine = runtimeLine(202);
		ActorNameplateViewerKey key = fixture.registerPresentation(firstLine, secondLine);

		boolean result = fixture.presentation.hideFromViewer(fixture.viewer, fixture.instanceUUID);

		assertTrue(result);
		verify(fixture.nameplateViewer).hide(fixture.viewer, firstLine);
		verify(fixture.nameplateViewer).hide(fixture.viewer, secondLine);
		assertTrue(fixture.runtimeRegistry.getAllLines().isEmpty());
		assertNull(fixture.presentationRegistry.lookup(key));
	}

	@Test
	void hideFromViewerReturnsFalseWhenPresentationDoesNotExist() {
		HideFixture fixture = new HideFixture();

		boolean result = fixture.presentation.hideFromViewer(fixture.viewer, fixture.instanceUUID);

		assertFalse(result);
		verifyNoInteractions(fixture.nameplateViewer);
		assertTrue(fixture.runtimeRegistry.getAllLines().isEmpty());
	}

	@Test
	void hideFromViewerRejectsInvalidInputs() {
		HideFixture fixture = new HideFixture();

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> fixture.presentation.hideFromViewer(null, fixture.instanceUUID)),
				() -> assertThrows(IllegalArgumentException.class, () -> fixture.presentation.hideFromViewer(fixture.viewer, null))
		);
		verifyNoInteractions(fixture.nameplateViewer);
	}

	@Test
	void hideFromViewerFailsBeforePacketsWhenRuntimeLineIsMissing() {
		HideFixture fixture = new HideFixture();
		NmsActorNameplateLine presentLine = runtimeLine(301);
		NmsActorNameplateLine missingLine = runtimeLine(302);
		ActorNameplateViewerKey key = fixture.registerPresentation(presentLine, missingLine);
		fixture.runtimeRegistry.remove(missingLine.getPresentationUUID());

		assertThrows(IllegalStateException.class, () ->
				fixture.presentation.hideFromViewer(fixture.viewer, fixture.instanceUUID)
		);

		verifyNoInteractions(fixture.nameplateViewer);
		assertSame(presentLine, fixture.runtimeRegistry.get(presentLine.getPresentationUUID()));
		assertNotNull(fixture.presentationRegistry.lookup(key));
	}

	@Test
	void hideFromViewerPreservesRegistriesWhenPacketDeliveryFails() {
		HideFixture fixture = new HideFixture();
		NmsActorNameplateLine firstLine = runtimeLine(401);
		NmsActorNameplateLine secondLine = runtimeLine(402);
		ActorNameplateViewerKey key = fixture.registerPresentation(firstLine, secondLine);
		RuntimeException failure = new RuntimeException("remove packet failed");
		doThrow(failure).when(fixture.nameplateViewer).hide(fixture.viewer, secondLine);

		RuntimeException thrown = assertThrows(RuntimeException.class, () ->
				fixture.presentation.hideFromViewer(fixture.viewer, fixture.instanceUUID)
		);

		assertSame(failure, thrown);
		assertSame(firstLine, fixture.runtimeRegistry.get(firstLine.getPresentationUUID()));
		assertSame(secondLine, fixture.runtimeRegistry.get(secondLine.getPresentationUUID()));
		assertNotNull(fixture.presentationRegistry.lookup(key));
	}

	@Test
	void updateNameplateTeleportsEveryLineUsingIndependentOffsets() {
		UpdateFixture fixture = new UpdateFixture();
		Player viewer = mock(Player.class);
		NmsActorNameplateLine firstLine = runtimeLine(501);
		NmsActorNameplateLine secondLine = runtimeLine(502);
		fixture.registerPresentation(viewer, false, List.of(firstLine, secondLine), List.of(2.6, 2.3));
		World world = mock(World.class);
		Location actorLocation = new Location(world, 10.0, 64.0, -5.0);

		fixture.presentation.updateNameplate(fixture.instance, actorLocation, false);

		verify(fixture.nameplateViewer).teleport(
				eq(viewer),
				eq(firstLine),
				argThat(location -> location != actorLocation
						&& location.getWorld() == world
						&& location.getX() == 10.0
						&& location.getY() == 66.6
						&& location.getZ() == -5.0)
		);
		verify(fixture.nameplateViewer).teleport(
				eq(viewer),
				eq(secondLine),
				argThat(location -> location != actorLocation
						&& location.getWorld() == world
						&& location.getX() == 10.0
						&& location.getY() == 66.3
						&& location.getZ() == -5.0)
		);
		assertEquals(64.0, actorLocation.getY());
	}

	@Test
	void updateNameplateDoesNothingWhenActorHasNoPresentations() {
		UpdateFixture fixture = new UpdateFixture();
		Location location = new Location(mock(World.class), 1.0, 2.0, 3.0);

		fixture.presentation.updateNameplate(fixture.instance, location, false);

		verifyNoInteractions(fixture.server, fixture.nameplateViewer);
	}

	@Test
	void updateNameplateReplacesLinesWhenMovementStateChanges() {
		UpdateFixture fixture = new UpdateFixture();
		Player viewer = mock(Player.class);
		NmsActorNameplateLine stationaryLine = runtimeLine(601);
		ActorNameplateViewerKey key = fixture.registerPresentation(viewer, false, List.of(stationaryLine), List.of(2.3));
		ActorNameplateLine nameLine = new ActorNameplateLine(ActorNameplateLineRole.NAME, Component.text("Blacksmith"), true);
		ActorNameplateLinePlacement placement = new ActorNameplateLinePlacement(nameLine, 2.3);
		NmsActorNameplateLine movingLine = runtimeLine(602);
		ActorDefinition definition = mock(ActorDefinition.class);
		when(fixture.instance.getActorDefinition()).thenReturn(definition);
		when(definition.getActorNameplate()).thenReturn(new ActorNameplate(List.of(nameLine)));
		when(fixture.layoutCalculator.calculateLinePlacements(anyList(), eq(fixture.layout))).thenReturn(List.of(placement));
		when(fixture.lineFactory.create(any(Location.class), eq(placement))).thenReturn(movingLine);
		Location location = new Location(mock(World.class), 1.0, 2.0, 3.0);

		fixture.presentation.updateNameplate(fixture.instance, location, true);

		verify(fixture.nameplateViewer).show(viewer, movingLine);
		verify(fixture.nameplateViewer).hide(viewer, stationaryLine);
		ActorNameplateViewerHandle replacement = fixture.presentationRegistry.lookup(key);
		assertTrue(replacement.inMovingState());
		assertEquals(movingLine.getPresentationUUID(), replacement.linePresentationHandles().getFirst().presentationUUID());
		assertNull(fixture.runtimeRegistry.get(stationaryLine.getPresentationUUID()));
		assertSame(movingLine, fixture.runtimeRegistry.get(movingLine.getPresentationUUID()));
	}

	@Test
	void updateNameplateSkipsDisconnectedViewer() {
		UpdateFixture fixture = new UpdateFixture();
		NmsActorNameplateLine runtimeLine = runtimeLine(701);
		fixture.registerPresentation(null, false, List.of(runtimeLine), List.of(2.3));
		Location location = new Location(mock(World.class), 1.0, 2.0, 3.0);

		fixture.presentation.updateNameplate(fixture.instance, location, false);

		verify(fixture.server).getPlayer(any(UUID.class));
		verifyNoInteractions(fixture.nameplateViewer);
		assertSame(runtimeLine, fixture.runtimeRegistry.get(runtimeLine.getPresentationUUID()));
	}

	@Test
	void clearViewerRemovesHandlesAndRuntimeLinesWithoutSendingPackets() {
		UpdateFixture fixture = new UpdateFixture();
		Player viewer = mock(Player.class);
		NmsActorNameplateLine runtimeLine = runtimeLine(750);
		ActorNameplateViewerKey key = fixture.registerPresentation(viewer, false, List.of(runtimeLine), List.of(2.3));

		fixture.presentation.clearViewer(key.viewerUUID());

		assertNull(fixture.presentationRegistry.lookup(key));
		assertNull(fixture.runtimeRegistry.get(runtimeLine.getPresentationUUID()));
		verifyNoInteractions(fixture.nameplateViewer);
	}

	@Test
	void clearViewerRejectsNullViewerId() {
		UpdateFixture fixture = new UpdateFixture();

		assertThrows(IllegalArgumentException.class, () -> fixture.presentation.clearViewer(null));
	}

	@Test
	void updateNameplateResolvesAllLinesBeforeSendingTeleportPackets() {
		UpdateFixture fixture = new UpdateFixture();
		Player viewer = mock(Player.class);
		NmsActorNameplateLine presentLine = runtimeLine(801);
		NmsActorNameplateLine missingLine = runtimeLine(802);
		fixture.registerPresentation(viewer, false, List.of(presentLine, missingLine), List.of(2.6, 2.3));
		fixture.runtimeRegistry.remove(missingLine.getPresentationUUID());
		Location location = new Location(mock(World.class), 1.0, 2.0, 3.0);

		assertThrows(IllegalStateException.class, () ->
				fixture.presentation.updateNameplate(fixture.instance, location, false)
		);

		verifyNoInteractions(fixture.nameplateViewer);
	}

	@Test
	void updateNameplateRejectsInvalidInputAndOverflowedLineDestination() {
		UpdateFixture fixture = new UpdateFixture();
		World world = mock(World.class);

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, () -> fixture.presentation.updateNameplate(null, new Location(world, 1.0, 2.0, 3.0), false)),
				() -> assertThrows(IllegalArgumentException.class, () -> fixture.presentation.updateNameplate(fixture.instance, null, false)),
				() -> assertThrows(IllegalArgumentException.class, () -> fixture.presentation.updateNameplate(fixture.instance, new Location(null, 1.0, 2.0, 3.0), false)),
				() -> assertThrows(IllegalArgumentException.class, () -> fixture.presentation.updateNameplate(fixture.instance, new Location(world, Double.NaN, 2.0, 3.0), false)),
				() -> assertThrows(IllegalArgumentException.class, () -> fixture.presentation.updateNameplate(fixture.instance, new Location(world, 1.0, Double.POSITIVE_INFINITY, 3.0), false)),
				() -> assertThrows(IllegalArgumentException.class, () -> fixture.presentation.updateNameplate(fixture.instance, new Location(world, 1.0, 2.0, Double.NEGATIVE_INFINITY), false))
		);

		Player viewer = mock(Player.class);
		fixture.registerPresentation(viewer, false, List.of(runtimeLine(901)), List.of(Double.MAX_VALUE));
		assertThrows(IllegalStateException.class, () ->
				fixture.presentation.updateNameplate(fixture.instance, new Location(world, 1.0, Double.MAX_VALUE, 3.0), false)
		);
		verifyNoInteractions(fixture.nameplateViewer);
	}

	@Test
	void removeNameplateRemovesAllScopedViewersAndLeavesUnrelatedActorUntouched() {
		UpdateFixture fixture = new UpdateFixture();
		Player onlineViewer = mock(Player.class);
		NmsActorNameplateLine firstOnlineLine = runtimeLine(1001);
		NmsActorNameplateLine secondOnlineLine = runtimeLine(1002);
		NmsActorNameplateLine offlineLine = runtimeLine(1003);
		fixture.registerPresentation(onlineViewer, false, List.of(firstOnlineLine, secondOnlineLine), List.of(2.6, 2.3));
		fixture.registerPresentation(null, false, List.of(offlineLine), List.of(2.3));

		UUID unrelatedInstanceUUID = UUID.randomUUID();
		UUID unrelatedViewerUUID = UUID.randomUUID();
		NmsActorNameplateLine unrelatedLine = runtimeLine(1004);
		fixture.runtimeRegistry.register(unrelatedLine);
		ActorNameplateViewerKey unrelatedKey = new ActorNameplateViewerKey(unrelatedInstanceUUID, unrelatedViewerUUID);
		ActorNameplateViewerHandle unrelatedHandle = new ActorNameplateViewerHandle(
				unrelatedKey,
				List.of(new ActorNameplateLinePresentationHandle(
						unrelatedLine.getPresentationUUID(),
						unrelatedLine.getEntityID(),
						2.3
				)),
				false
		);
		fixture.presentationRegistry.register(unrelatedHandle);

		boolean result = fixture.presentation.removeNameplate(fixture.instanceUUID);

		assertTrue(result);
		verify(fixture.nameplateViewer).hide(onlineViewer, firstOnlineLine);
		verify(fixture.nameplateViewer).hide(onlineViewer, secondOnlineLine);
		verify(fixture.nameplateViewer, never()).hide(any(), eq(offlineLine));
		assertNull(fixture.runtimeRegistry.get(firstOnlineLine.getPresentationUUID()));
		assertNull(fixture.runtimeRegistry.get(secondOnlineLine.getPresentationUUID()));
		assertNull(fixture.runtimeRegistry.get(offlineLine.getPresentationUUID()));
		assertSame(unrelatedLine, fixture.runtimeRegistry.get(unrelatedLine.getPresentationUUID()));
		assertTrue(fixture.presentationRegistry.findActorHandles(fixture.instanceUUID).isEmpty());
		assertSame(unrelatedHandle, fixture.presentationRegistry.lookup(unrelatedKey));
	}

	@Test
	void removeNameplateReturnsFalseWhenActorHasNoPresentations() {
		UpdateFixture fixture = new UpdateFixture();

		boolean result = fixture.presentation.removeNameplate(fixture.instanceUUID);

		assertFalse(result);
		verifyNoInteractions(fixture.server, fixture.nameplateViewer);
	}

	@Test
	void removeNameplateRejectsNullInstanceUUID() {
		UpdateFixture fixture = new UpdateFixture();

		assertThrows(IllegalArgumentException.class, () -> fixture.presentation.removeNameplate(null));
		verifyNoInteractions(fixture.server, fixture.nameplateViewer);
	}

	@Test
	void removeNameplateFailsBeforePacketsWhenRuntimeStateIsMissing() {
		UpdateFixture fixture = new UpdateFixture();
		Player viewer = mock(Player.class);
		NmsActorNameplateLine presentLine = runtimeLine(1101);
		NmsActorNameplateLine missingLine = runtimeLine(1102);
		fixture.registerPresentation(viewer, false, List.of(presentLine, missingLine), List.of(2.6, 2.3));
		fixture.runtimeRegistry.remove(missingLine.getPresentationUUID());

		assertThrows(IllegalStateException.class, () ->
				fixture.presentation.removeNameplate(fixture.instanceUUID)
		);

		verifyNoInteractions(fixture.nameplateViewer);
		assertSame(presentLine, fixture.runtimeRegistry.get(presentLine.getPresentationUUID()));
		assertEquals(1, fixture.presentationRegistry.findActorHandles(fixture.instanceUUID).size());
	}

	@Test
	void removeNameplatePreservesRegistriesWhenPacketDeliveryFails() {
		UpdateFixture fixture = new UpdateFixture();
		Player viewer = mock(Player.class);
		NmsActorNameplateLine firstLine = runtimeLine(1201);
		NmsActorNameplateLine secondLine = runtimeLine(1202);
		fixture.registerPresentation(viewer, false, List.of(firstLine, secondLine), List.of(2.6, 2.3));
		RuntimeException failure = new RuntimeException("remove packet failed");
		doThrow(failure).when(fixture.nameplateViewer).hide(viewer, secondLine);

		RuntimeException thrown = assertThrows(RuntimeException.class, () ->
				fixture.presentation.removeNameplate(fixture.instanceUUID)
		);

		assertSame(failure, thrown);
		assertSame(firstLine, fixture.runtimeRegistry.get(firstLine.getPresentationUUID()));
		assertSame(secondLine, fixture.runtimeRegistry.get(secondLine.getPresentationUUID()));
		assertEquals(1, fixture.presentationRegistry.findActorHandles(fixture.instanceUUID).size());
	}

	private static NmsActorNameplateLine runtimeLine(int entityID) {
		NmsActorNameplateLine runtimeLine = mock(NmsActorNameplateLine.class);
		when(runtimeLine.getPresentationUUID()).thenReturn(UUID.randomUUID());
		when(runtimeLine.getEntityID()).thenReturn(entityID);
		return runtimeLine;
	}

	private static final class HideFixture {
		private final ActorNameplatePresentationRegistry presentationRegistry = new ActorNameplatePresentationRegistry();
		private final NmsActorNameplateLineRegistry runtimeRegistry = new NmsActorNameplateLineRegistry();
		private final NmsActorNameplateViewer nameplateViewer = mock(NmsActorNameplateViewer.class);
		private final Server server = mock(Server.class);
		private final Player viewer = mock(Player.class);
		private final UUID viewerUUID = UUID.randomUUID();
		private final UUID instanceUUID = UUID.randomUUID();
		private final NmsActorNameplatePresentation presentation;

		private HideFixture() {
			when(viewer.getUniqueId()).thenReturn(viewerUUID);
			presentation = new NmsActorNameplatePresentation(
					presentationRegistry,
					runtimeRegistry,
					mock(ActorNameplateLayoutCalculator.class),
					mock(NmsActorNameplateLineFactory.class),
					nameplateViewer,
					new ActorNameplateLayout(2.3, 0.3),
					server
			);
		}

		private ActorNameplateViewerKey registerPresentation(NmsActorNameplateLine... runtimeLines) {
			List<ActorNameplateLinePresentationHandle> lineHandles = java.util.Arrays.stream(runtimeLines)
					.peek(runtimeRegistry::register)
					.map(runtimeLine -> new ActorNameplateLinePresentationHandle(
							runtimeLine.getPresentationUUID(),
							runtimeLine.getEntityID(),
							2.3
					))
					.toList();
			ActorNameplateViewerKey key = new ActorNameplateViewerKey(instanceUUID, viewerUUID);
			presentationRegistry.register(new ActorNameplateViewerHandle(key, lineHandles, false));
			return key;
		}
	}

	private static final class UpdateFixture {
		private final ActorNameplatePresentationRegistry presentationRegistry = new ActorNameplatePresentationRegistry();
		private final NmsActorNameplateLineRegistry runtimeRegistry = new NmsActorNameplateLineRegistry();
		private final NmsActorNameplateViewer nameplateViewer = mock(NmsActorNameplateViewer.class);
		private final Server server = mock(Server.class);
		private final UUID instanceUUID = UUID.randomUUID();
		private final ActorInstance instance = mock(ActorInstance.class);
		private final ActorNameplateLayoutCalculator layoutCalculator = mock(ActorNameplateLayoutCalculator.class);
		private final NmsActorNameplateLineFactory lineFactory = mock(NmsActorNameplateLineFactory.class);
		private final ActorNameplateLayout layout = new ActorNameplateLayout(2.3, 0.3);
		private final NmsActorNameplatePresentation presentation;

		private UpdateFixture() {
			when(instance.getInstanceID()).thenReturn(instanceUUID);
			presentation = new NmsActorNameplatePresentation(
					presentationRegistry,
					runtimeRegistry,
					layoutCalculator,
					lineFactory,
					nameplateViewer,
					layout,
					server
			);
		}

		private ActorNameplateViewerKey registerPresentation(
				Player viewer,
				boolean moving,
				List<NmsActorNameplateLine> runtimeLines,
				List<Double> offsets
		) {
			if (runtimeLines.size() != offsets.size()) throw new IllegalArgumentException("Each runtime line requires an offset");
			UUID viewerUUID = UUID.randomUUID();
			when(server.getPlayer(viewerUUID)).thenReturn(viewer);

			List<ActorNameplateLinePresentationHandle> lineHandles = new java.util.ArrayList<>();
			for (int index = 0; index < runtimeLines.size(); index++) {
				NmsActorNameplateLine runtimeLine = runtimeLines.get(index);
				runtimeRegistry.register(runtimeLine);
				lineHandles.add(new ActorNameplateLinePresentationHandle(
						runtimeLine.getPresentationUUID(),
						runtimeLine.getEntityID(),
						offsets.get(index)
				));
			}

			ActorNameplateViewerKey key = new ActorNameplateViewerKey(instanceUUID, viewerUUID);
			presentationRegistry.register(new ActorNameplateViewerHandle(key, lineHandles, moving));
			return key;
		}
	}
}
