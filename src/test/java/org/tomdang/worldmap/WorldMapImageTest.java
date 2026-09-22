package org.tomdang.worldmap;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class WorldMapImageTest {
	@Test
	void fullViewFitsWholeImageWithoutCalibration() {
		BufferedImage source = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
		source.setRGB(0, 0, Color.RED.getRGB());
		WorldMapImage image = new WorldMapImage(source, null);
		BufferedImage frame = image.full();
		assertEquals(128, frame.getWidth());
		assertEquals(128, frame.getHeight());
		assertEquals(Color.RED.getRGB(), frame.getRGB(0, 32));
	}

	@Test
	void localViewCentersOnWorldCoordinate() {
		BufferedImage source = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		source.setRGB(128, 128, Color.RED.getRGB());
		WorldMapImage image = new WorldMapImage(source, new WorldMapBounds(0, 0, 255, 255));
		BufferedImage frame = image.local(128, 128, 128);
		assertEquals(128, frame.getWidth());
		assertEquals(Color.RED.getRGB(), frame.getRGB(64, 64));
		assertEquals(new Color(226, 184, 148).getRGB(), frame.getRGB(3, 20));
		assertEquals(new Color(187, 34, 13).getRGB(), frame.getRGB(11, 2));
		assertEquals(128.0, new WorldMapBounds(0, 0, 255, 255).pixelX(128, 256));
	}

	@Test
	void fullImageStaysFixedWhileMarkerCoordinatesChange() {
		BufferedImage source = new BufferedImage(256, 128, BufferedImage.TYPE_INT_RGB);
		WorldMapImage image = new WorldMapImage(source, new WorldMapBounds(0, 0, 255, 127));
		BufferedImage first = image.full();
		assertSame(first, image.full());
		assertEquals(new WorldMapImage.Pixel(0, 32), image.fullMarker(0, 0));
		assertEquals(new WorldMapImage.Pixel(127, 96), image.fullMarker(255, 127));
		assertNull(image.fullMarker(256, 0));
	}

	@Test
	void localViewRejectsUnknownBounds() {
		WorldMapImage image = new WorldMapImage(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), null);
		assertThrows(IllegalStateException.class, () -> image.local(1, 1, 128));
	}

	@Test
	void exportedPixelBoundsMatchTheSelectedWorldRectangle() {
		WorldMapBounds bounds = new WorldMapBounds(-1536, -1152, 1663, 1151);
		assertEquals(0.0, bounds.pixelX(-1536, 3200));
		assertEquals(3199.0, bounds.pixelX(1663, 3200));
		assertEquals(0.0, bounds.pixelY(-1152, 2304));
		assertEquals(2303.0, bounds.pixelY(1151, 2304));
		assertEquals(1736.0, bounds.pixelX(200, 3200));
		assertEquals(1067.0, bounds.pixelY(-85, 2304));
	}
}
