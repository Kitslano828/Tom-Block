package org.tomdang.actorframework.nameplate.nms;

import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityTypes;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.tomdang.actorframework.nameplate.layout.ActorNameplateLinePlacement;
import org.tomdang.actorframework.nameplate.nms.runtime.NmsActorNameplateLine;

public class NmsActorNameplateLineFactory {

	public NmsActorNameplateLine create(Location actorLocation, ActorNameplateLinePlacement linePlacement) {
		if (actorLocation == null || actorLocation.getWorld() == null) throw new IllegalArgumentException("Location or its world cannot be null");
		if (linePlacement == null) throw new IllegalArgumentException("linePlacement cannot be null");

		double x = actorLocation.getX();
		double originalY = actorLocation.getY();
		double z = actorLocation.getZ();

		if (!Double.isFinite(x) || !Double.isFinite(originalY) || !Double.isFinite(z)) {
			throw new IllegalArgumentException("Actor location coordinates (X, Y, Z) must be finite");
		}

		double finalY = originalY + linePlacement.verticalOffset();
		if (!Double.isFinite(finalY)) {
			throw new IllegalArgumentException("Calculated final Y coordinate is not finite: " + finalY);
		}

		ServerLevel serverLevel = ((CraftWorld) actorLocation.getWorld()).getHandle();

		Display.TextDisplay textDisplay = new Display.TextDisplay(EntityTypes.TEXT_DISPLAY, serverLevel);
		textDisplay.setPos(x, finalY, z);

		textDisplay.setText(PaperAdventure.asVanilla(linePlacement.line().getText()));

		textDisplay.setBillboardConstraints(Display.BillboardConstraints.CENTER);

		return new NmsActorNameplateLine(textDisplay);
	}

}
