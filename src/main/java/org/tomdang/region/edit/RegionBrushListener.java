package org.tomdang.region.edit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.tomdang.region.bukkit.BukkitBlockPositionAdapter;
import org.tomdang.region.override.RegionOverrideState;
import org.tomdang.region.position.BlockPosition;

public final class RegionBrushListener implements Listener {
	private final RegionBrushItemService brushItemService;
	private final RegionEditingService editingService;
	private final BukkitBlockPositionAdapter positionAdapter;

	public RegionBrushListener(RegionBrushItemService brushItemService, RegionEditingService editingService,
			BukkitBlockPositionAdapter positionAdapter) {
		if (brushItemService == null) throw new IllegalArgumentException("brushItemService cannot be null");
		if (editingService == null) throw new IllegalArgumentException("editingService cannot be null");
		if (positionAdapter == null) throw new IllegalArgumentException("positionAdapter cannot be null");
		this.brushItemService = brushItemService;
		this.editingService = editingService;
		this.positionAdapter = positionAdapter;
	}

	@EventHandler
	public void onBrushUse(PlayerInteractEvent event) {
		if (event.getHand() != EquipmentSlot.HAND || !brushItemService.isBrush(event.getItem())) return;
		if (event.getClickedBlock() == null || !isSupportedAction(event.getAction())) return;
		event.setCancelled(true);

		if (!event.getPlayer().hasPermission("tomblock.admin.region.edit")) {
			event.getPlayer().sendActionBar(Component.text("You cannot use the Region Brush.", NamedTextColor.RED));
			return;
		}
		if (editingService.session(event.getPlayer().getUniqueId()).isEmpty()) {
			event.getPlayer().sendActionBar(Component.text("Use /region edit <region> first.", NamedTextColor.RED));
			return;
		}

		RegionOverrideState state = stateFor(event);
		BlockPosition position = positionAdapter.fromLocation(event.getClickedBlock().getLocation());
		RegionEditAction result = editingService.apply(event.getPlayer().getUniqueId(), position, state);
		NamedTextColor color = switch (state) {
			case INCLUSION -> NamedTextColor.GREEN;
			case EXCLUSION -> NamedTextColor.RED;
			case NONE -> NamedTextColor.YELLOW;
		};
		event.getPlayer().sendActionBar(Component.text(result.regionId(), NamedTextColor.AQUA)
				.append(Component.text(" " + state + " ", color))
				.append(Component.text(position.x() + ", " + position.y() + ", " + position.z(), NamedTextColor.GRAY))
				.append(Component.text(editingService.hasUnsavedChanges() ? " • unsaved" : "", NamedTextColor.GOLD)));
	}

	private boolean isSupportedAction(Action action) {
		return action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK;
	}

	private RegionOverrideState stateFor(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_BLOCK) return RegionOverrideState.INCLUSION;
		return event.getPlayer().isSneaking() ? RegionOverrideState.NONE : RegionOverrideState.EXCLUSION;
	}
}
