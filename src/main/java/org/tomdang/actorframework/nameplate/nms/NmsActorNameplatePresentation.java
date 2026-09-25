package org.tomdang.actorframework.nameplate.nms;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.tomdang.actorframework.instance.ActorInstance;
import org.tomdang.actorframework.nameplate.ActorNameplateLine;
import org.tomdang.actorframework.nameplate.layout.ActorNameplateLayout;
import org.tomdang.actorframework.nameplate.layout.ActorNameplateLayoutCalculator;
import org.tomdang.actorframework.nameplate.layout.ActorNameplateLinePlacement;
import org.tomdang.actorframework.nameplate.nms.runtime.NmsActorNameplateLine;
import org.tomdang.actorframework.nameplate.nms.runtime.NmsActorNameplateLineRegistry;
import org.tomdang.actorframework.nameplate.presentation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NmsActorNameplatePresentation implements ActorNameplatePresentation {

	private final ActorNameplatePresentationRegistry actorNameplatePresentationRegistry;
	private final NmsActorNameplateLineRegistry nmsActorNameplateLineRegistry;
	private final ActorNameplateLayoutCalculator actorNameplateLayoutCalculator;
	private final NmsActorNameplateLineFactory nmsActorNameplateLineFactory;
	private final NmsActorNameplateViewer nmsActorNameplateViewer;
	private final ActorNameplateLayout actorNameplateLayout;
	private final Server server;
	private final ActorNameplateLineProvider additionalLines;

	public NmsActorNameplatePresentation(ActorNameplatePresentationRegistry actorNameplatePresentationRegistry, NmsActorNameplateLineRegistry nmsActorNameplateLineRegistry, ActorNameplateLayoutCalculator actorNameplateLayoutCalculator,
	                                     NmsActorNameplateLineFactory nmsActorNameplateLineFactory, NmsActorNameplateViewer nmsActorNameplateViewer, ActorNameplateLayout actorNameplateLayout, Server server) {
		this(actorNameplatePresentationRegistry,nmsActorNameplateLineRegistry,actorNameplateLayoutCalculator,nmsActorNameplateLineFactory,nmsActorNameplateViewer,actorNameplateLayout,server,ActorNameplateLineProvider.NONE);
	}

	public NmsActorNameplatePresentation(ActorNameplatePresentationRegistry actorNameplatePresentationRegistry, NmsActorNameplateLineRegistry nmsActorNameplateLineRegistry, ActorNameplateLayoutCalculator actorNameplateLayoutCalculator,
	                                     NmsActorNameplateLineFactory nmsActorNameplateLineFactory, NmsActorNameplateViewer nmsActorNameplateViewer, ActorNameplateLayout actorNameplateLayout, Server server, ActorNameplateLineProvider additionalLines) {
		if (actorNameplatePresentationRegistry == null) throw new IllegalArgumentException("actorNameplatePresentationRegistry cannot be null");
		if (nmsActorNameplateLineRegistry == null) throw new IllegalArgumentException("nmsActorNameplateLineRegistry cannot be null");
		if (actorNameplateLayoutCalculator == null) throw new IllegalArgumentException("actorNameplateLayoutCalculator cannot be null");
		if (nmsActorNameplateLineFactory == null) throw new IllegalArgumentException("nmsActorNameplateLineFactory cannot be null");
		if (nmsActorNameplateViewer == null) throw new IllegalArgumentException("nmsActorNameplateViewer cannot be null");
		if (actorNameplateLayout == null) throw new IllegalArgumentException("actorNameplateLayout cannot be null");
		if (server == null) throw new IllegalArgumentException("server cannot be null");
		if (additionalLines == null) throw new IllegalArgumentException("additionalLines cannot be null");

		this.actorNameplatePresentationRegistry = actorNameplatePresentationRegistry;
		this.nmsActorNameplateLineRegistry = nmsActorNameplateLineRegistry;
		this.actorNameplateLayoutCalculator = actorNameplateLayoutCalculator;
		this.nmsActorNameplateLineFactory = nmsActorNameplateLineFactory;
		this.nmsActorNameplateViewer = nmsActorNameplateViewer;
		this.actorNameplateLayout = actorNameplateLayout;
		this.server = server;
		this.additionalLines = additionalLines;
	}

	@Override
	public boolean showToViewer(Player viewer, ActorInstance instance, Location currentLocation, boolean isMoving) {
		if (viewer == null) throw new IllegalArgumentException("viewer cannot be null");
		if (instance == null) throw new IllegalArgumentException("instance cannot be null");
		if (currentLocation == null || currentLocation.getWorld() == null) throw new IllegalArgumentException("currentLocation and its world cannot be null");

		double x = currentLocation.getX();
		double y = currentLocation.getY();
		double z = currentLocation.getZ();

		ActorNameplateViewerKey viewerKey = new ActorNameplateViewerKey(instance.getInstanceID(), viewer.getUniqueId());

		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
			throw new IllegalArgumentException("Actor location coordinates (X, Y, Z) must be finite");
		}

		if (actorNameplatePresentationRegistry.doesPresentationHandleExist(viewerKey)) return false;

		PreparedNameplate preparedNameplate = prepareNameplate(viewer, instance, currentLocation, isMoving, viewerKey);

		try {
			actorNameplatePresentationRegistry.register(preparedNameplate.viewerHandle());
		} catch (RuntimeException exception) {
			rollbackShow(viewer, preparedNameplate.runtimeLines(), exception);
			throw exception;
		}

		return true;
	}

	private PreparedNameplate prepareNameplate(
			Player viewer,
			ActorInstance instance,
			Location currentLocation,
			boolean isMoving,
			ActorNameplateViewerKey viewerKey
	) {
		List<ActorNameplateLine> lines = new ArrayList<>(additionalLines.lines(viewer, instance, isMoving));
		lines.addAll(instance.getActorDefinition().getActorNameplate().getVisibleLines(isMoving));

		List<ActorNameplateLinePlacement> linePlacements = actorNameplateLayoutCalculator.calculateLinePlacements(lines, actorNameplateLayout);

		List<ActorNameplateLinePresentationHandle> linePresentationHandles = new ArrayList<>();
		List<NmsActorNameplateLine> registeredRuntimeLines = new ArrayList<>();

		try {
			for (ActorNameplateLinePlacement linePlacement : linePlacements) {
				NmsActorNameplateLine runtimeLine = nmsActorNameplateLineFactory.create(currentLocation, linePlacement);
				nmsActorNameplateLineRegistry.register(runtimeLine);
				registeredRuntimeLines.add(runtimeLine);
				nmsActorNameplateViewer.show(viewer, runtimeLine);
				ActorNameplateLinePresentationHandle presentationHandle = new ActorNameplateLinePresentationHandle(runtimeLine.getPresentationUUID(), runtimeLine.getEntityID(), linePlacement.verticalOffset());
				linePresentationHandles.add(presentationHandle);
			}

			ActorNameplateViewerHandle viewerHandle = new ActorNameplateViewerHandle(viewerKey, linePresentationHandles, isMoving);
			return new PreparedNameplate(viewerHandle, registeredRuntimeLines);
		} catch (RuntimeException exception) {
			rollbackShow(viewer, registeredRuntimeLines, exception);
			throw exception;
		}
	}

	@Override
	public boolean hideFromViewer(Player viewer, UUID instanceUUID) {
		if (viewer == null) throw new IllegalArgumentException("viewer cannot be null");
		if (instanceUUID == null) throw new IllegalArgumentException("instanceUUID cannot be null");

		ActorNameplateViewerKey key = new ActorNameplateViewerKey(instanceUUID, viewer.getUniqueId());
		ActorNameplateViewerHandle viewerHandle = actorNameplatePresentationRegistry.lookup(key);

		if (viewerHandle == null) return false;

		List<NmsActorNameplateLine> runtimeLines = new ArrayList<>();

		for (ActorNameplateLinePresentationHandle handle : viewerHandle.linePresentationHandles()) {
			UUID presentationUUID = handle.presentationUUID();
			NmsActorNameplateLine nameplateLine = nmsActorNameplateLineRegistry.get(presentationUUID);
			if (nameplateLine == null) throw new IllegalStateException("nameplateLine does not exist for " + presentationUUID);
			runtimeLines.add(nameplateLine);
		}

		for (NmsActorNameplateLine runtimeLine : runtimeLines) {
			nmsActorNameplateViewer.hide(viewer, runtimeLine);
		}

		for (NmsActorNameplateLine runtimeLine : runtimeLines) {
			nmsActorNameplateLineRegistry.remove(runtimeLine.getPresentationUUID());
		}

		actorNameplatePresentationRegistry.removeHandle(key);

		return true;
	}

	@Override
	public void updateNameplate(ActorInstance instance, Location newLocation, boolean isMoving) {
		if (instance == null) throw new IllegalArgumentException("instance cannot be null");
		if (newLocation == null || newLocation.getWorld() == null) throw new IllegalArgumentException("newLocation and its world cannot be null");

		double x = newLocation.getX();
		double y = newLocation.getY();
		double z = newLocation.getZ();

		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
			throw new IllegalArgumentException("Actor location coordinates (X, Y, Z) must be finite");
		}

		List<ActorNameplateViewerHandle> viewerHandles = actorNameplatePresentationRegistry.findActorHandles(instance.getInstanceID());
		if (viewerHandles.isEmpty()) return;

		List<NameplateLineTeleport> lineTeleports = new ArrayList<>();
		List<NameplateTransition> transitions = new ArrayList<>();

		for (ActorNameplateViewerHandle viewerHandle : viewerHandles) {
			UUID viewerUUID = viewerHandle.actorNameplateViewerKey().viewerUUID();
			Player viewer = server.getPlayer(viewerUUID);
			if (viewer == null) continue;

			List<NmsActorNameplateLine> runtimeLines = resolveRuntimeLines(viewerHandle);
			if (viewerHandle.inMovingState() != isMoving) {
				transitions.add(new NameplateTransition(viewer, viewerHandle, runtimeLines));
				continue;
			}

			for (int index = 0; index < viewerHandle.linePresentationHandles().size(); index++) {
				ActorNameplateLinePresentationHandle lineHandle = viewerHandle.linePresentationHandles().get(index);
				NmsActorNameplateLine runtimeLine = runtimeLines.get(index);
				Location location = newLocation.clone();
				location.setY(location.getY() + lineHandle.verticalOffset());
				if (!Double.isFinite(location.getY())) throw new IllegalStateException("cloned y location is not finite");
				lineTeleports.add(new NameplateLineTeleport(viewer, runtimeLine, location));
			}
		}

		for (NameplateLineTeleport lineTeleport : lineTeleports) {
			nmsActorNameplateViewer.teleport(lineTeleport.viewer, lineTeleport.runtimeLine, lineTeleport.destination);
		}

		for (NameplateTransition transition : transitions) {
			replaceNameplateForViewer(transition, instance, newLocation, isMoving);
		}
	}

	private void replaceNameplateForViewer(NameplateTransition transition, ActorInstance instance, Location newLocation, boolean isMoving) {
		PreparedNameplate preparedNameplate = prepareNameplate(
				transition.viewer(),
				instance,
				newLocation,
				isMoving,
				transition.oldHandle().actorNameplateViewerKey()
		);

		try {
			for (NmsActorNameplateLine oldRuntimeLine : transition.oldRuntimeLines()) {
				nmsActorNameplateViewer.hide(transition.viewer(), oldRuntimeLine);
			}
			actorNameplatePresentationRegistry.replaceHandle(preparedNameplate.viewerHandle());
		} catch (RuntimeException transitionException) {
			rollbackShow(transition.viewer(), preparedNameplate.runtimeLines(), transitionException);
			for (NmsActorNameplateLine oldRuntimeLine : transition.oldRuntimeLines()) {
				try {
					nmsActorNameplateViewer.show(transition.viewer(), oldRuntimeLine);
				} catch (RuntimeException restoreException) {
					transitionException.addSuppressed(restoreException);
				}
			}
			throw transitionException;
		}

		for (NmsActorNameplateLine oldRuntimeLine : transition.oldRuntimeLines()) {
			nmsActorNameplateLineRegistry.remove(oldRuntimeLine.getPresentationUUID());
		}
	}

	@Override
	public boolean removeNameplate(UUID instanceUUID) {
		if (instanceUUID == null) throw new IllegalArgumentException("Instance UUID cannot be null");

		List<ActorNameplateViewerHandle> viewerHandles = actorNameplatePresentationRegistry.findActorHandles(instanceUUID);
		if (viewerHandles.isEmpty()) return false;

		List<NmsActorNameplateLine> scopedRuntimeLines = new ArrayList<>();
		List<NameplateLineHide> hidePlans = new ArrayList<>();

		for (ActorNameplateViewerHandle viewerHandle : viewerHandles) {
			UUID viewerUUID = viewerHandle.actorNameplateViewerKey().viewerUUID();
			Player viewer = server.getPlayer(viewerUUID);

			for (ActorNameplateLinePresentationHandle lineHandle : viewerHandle.linePresentationHandles()) {
				UUID presentationUUID = lineHandle.presentationUUID();
				NmsActorNameplateLine runtimeLine = nmsActorNameplateLineRegistry.get(presentationUUID);
				if (runtimeLine == null) throw new IllegalStateException("Runtime line missing for presentation UUID: " + presentationUUID);

				scopedRuntimeLines.add(runtimeLine);

				if (viewer != null) {
					hidePlans.add(new NameplateLineHide(viewer, runtimeLine));
				}
			}
		}

		// Packet execution phase
		for (NameplateLineHide hidePlan : hidePlans) {
			nmsActorNameplateViewer.hide(hidePlan.viewer(), hidePlan.runtimeLine());
		}

		// Runtime registry cleanup phase
		for (NmsActorNameplateLine runtimeLine : scopedRuntimeLines) {
			nmsActorNameplateLineRegistry.remove(runtimeLine.getPresentationUUID());
		}

		// Presentation handle ownership cleanup phase
		for (ActorNameplateViewerHandle viewerHandle : viewerHandles) {
			actorNameplatePresentationRegistry.removeHandle(viewerHandle.actorNameplateViewerKey());
		}

		return true;
	}

	@Override
	public void clearViewer(UUID viewerUUID) {
		if (viewerUUID == null) throw new IllegalArgumentException("viewerUUID cannot be null");

		List<ActorNameplateViewerHandle> viewerHandles = actorNameplatePresentationRegistry.findViewerHandles(viewerUUID);
		for (ActorNameplateViewerHandle viewerHandle : viewerHandles) {
			for (NmsActorNameplateLine runtimeLine : resolveRuntimeLines(viewerHandle)) {
				nmsActorNameplateLineRegistry.remove(runtimeLine.getPresentationUUID());
			}
			actorNameplatePresentationRegistry.removeHandle(viewerHandle.actorNameplateViewerKey());
		}
	}

	private List<NmsActorNameplateLine> resolveRuntimeLines(ActorNameplateViewerHandle viewerHandle) {
		List<NmsActorNameplateLine> runtimeLines = new ArrayList<>();
		for (ActorNameplateLinePresentationHandle lineHandle : viewerHandle.linePresentationHandles()) {
			NmsActorNameplateLine runtimeLine = nmsActorNameplateLineRegistry.get(lineHandle.presentationUUID());
			if (runtimeLine == null) {
				throw new IllegalStateException("Runtime line missing for presentation UUID: " + lineHandle.presentationUUID());
			}
			runtimeLines.add(runtimeLine);
		}
		return List.copyOf(runtimeLines);
	}

	private void rollbackShow(Player viewer, List<NmsActorNameplateLine> registeredRuntimeLines, RuntimeException originalException) {
		for (int index = registeredRuntimeLines.size() - 1; index >= 0; index--) {
			NmsActorNameplateLine runtimeLine = registeredRuntimeLines.get(index);

			try {
				nmsActorNameplateViewer.hide(viewer, runtimeLine);
			} catch (RuntimeException cleanupException) {
				originalException.addSuppressed(cleanupException);
			}

			try {
				nmsActorNameplateLineRegistry.remove(runtimeLine.getPresentationUUID());
			} catch (RuntimeException cleanupException) {
				originalException.addSuppressed(cleanupException);
			}
		}
	}

	private record NameplateLineTeleport(Player viewer, NmsActorNameplateLine runtimeLine, Location destination) {

	}

	private record NameplateLineHide(Player viewer, NmsActorNameplateLine runtimeLine) {

	}

	private record NameplateTransition(
			Player viewer,
			ActorNameplateViewerHandle oldHandle,
			List<NmsActorNameplateLine> oldRuntimeLines
	) {
		private NameplateTransition {
			oldRuntimeLines = List.copyOf(oldRuntimeLines);
		}
	}

	private record PreparedNameplate(
			ActorNameplateViewerHandle viewerHandle,
			List<NmsActorNameplateLine> runtimeLines
	) {
		private PreparedNameplate {
			if (viewerHandle == null) throw new IllegalArgumentException("viewerHandle cannot be null");
			if (runtimeLines == null) throw new IllegalArgumentException("runtimeLines cannot be null");
			if (runtimeLines.stream().anyMatch(java.util.Objects::isNull)) {
				throw new IllegalArgumentException("runtimeLines cannot contain null elements");
			}
			runtimeLines = List.copyOf(runtimeLines);
		}
	}
}
