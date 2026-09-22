package org.tomdang.worldmap;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Converts one top-down source image into full-world and player-centered map-item views. */
public final class WorldMapImage {
	public static final int MAP_PIXELS = 128;
	private static final int LOCAL_INSET = 8;
	private final BufferedImage source;
	private final WorldMapBounds bounds;
	private BufferedImage fullFrame;

	public WorldMapImage(BufferedImage source, WorldMapBounds bounds) {
		if (source == null) throw new IllegalArgumentException("source cannot be null");
		this.source = source;
		this.bounds = bounds;
	}

	public boolean isCalibrated() {
		return bounds != null;
	}

	public BufferedImage full() {
		if (fullFrame != null) return fullFrame;
		BufferedImage output = background();
		Graphics2D graphics = output.createGraphics();
		try {
			nearestNeighbor(graphics);
			double scale = Math.min((double) MAP_PIXELS / source.getWidth(),
					(double) MAP_PIXELS / source.getHeight());
			int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
			int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
			int left = (MAP_PIXELS - width) / 2;
			int top = (MAP_PIXELS - height) / 2;
			graphics.drawImage(source, left, top, width, height, null);
		} finally {
			graphics.dispose();
		}
		fullFrame = output;
		return fullFrame;
	}

	public Pixel fullMarker(double playerX, double playerZ) {
		if (bounds == null || playerX < bounds.minimumX() || playerX > bounds.maximumX()
				|| playerZ < bounds.minimumZ() || playerZ > bounds.maximumZ()) return null;
		double scale = Math.min((double) MAP_PIXELS / source.getWidth(),
				(double) MAP_PIXELS / source.getHeight());
		int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
		int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
		int left = (MAP_PIXELS - width) / 2;
		int top = (MAP_PIXELS - height) / 2;
		return new Pixel(Math.clamp(left + (int) Math.round(bounds.pixelX(playerX, source.getWidth()) * scale),
				0, MAP_PIXELS - 1),
				Math.clamp(top + (int) Math.round(bounds.pixelY(playerZ, source.getHeight()) * scale),
						0, MAP_PIXELS - 1));
	}

	public BufferedImage local(double playerX, double playerZ, int diameterBlocks) {
		if (bounds == null) throw new IllegalStateException("World-map bounds are not calibrated");
		if (diameterBlocks <= 0) throw new IllegalArgumentException("diameterBlocks must be positive");
		BufferedImage output = background();
		Graphics2D graphics = output.createGraphics();
		try {
			nearestNeighbor(graphics);
			double centerX = bounds.pixelX(playerX, source.getWidth());
			double centerY = bounds.pixelY(playerZ, source.getHeight());
			double halfX = diameterBlocks * source.getWidth()
					/ (2.0 * (bounds.maximumX() - bounds.minimumX() + 1));
			double halfY = diameterBlocks * source.getHeight()
					/ (2.0 * (bounds.maximumZ() - bounds.minimumZ() + 1));
			graphics.drawImage(source, LOCAL_INSET, LOCAL_INSET,
					MAP_PIXELS - LOCAL_INSET, MAP_PIXELS - LOCAL_INSET,
					(int) Math.floor(centerX - halfX), (int) Math.floor(centerY - halfY),
					(int) Math.ceil(centerX + halfX), (int) Math.ceil(centerY + halfY), null);
			drawLocalFrame(graphics);
		} finally {
			graphics.dispose();
		}
		return output;
	}

	/** Dialogue-box-inspired parchment and gold frame for the local view. */
	private static void drawLocalFrame(Graphics2D graphics) {
		graphics.setColor(new Color(226, 184, 148));
		graphics.fillRect(0, 0, MAP_PIXELS, LOCAL_INSET);
		graphics.fillRect(0, MAP_PIXELS - LOCAL_INSET, MAP_PIXELS, LOCAL_INSET);
		graphics.fillRect(0, LOCAL_INSET, LOCAL_INSET, MAP_PIXELS - LOCAL_INSET * 2);
		graphics.fillRect(MAP_PIXELS - LOCAL_INSET, LOCAL_INSET,
				LOCAL_INSET, MAP_PIXELS - LOCAL_INSET * 2);
		graphics.setColor(new Color(155, 103, 5));
		graphics.drawRect(2, 2, MAP_PIXELS - 5, MAP_PIXELS - 5);
		graphics.setColor(new Color(194, 173, 11));
		graphics.drawRect(5, 5, MAP_PIXELS - 11, MAP_PIXELS - 11);
		graphics.setColor(new Color(149, 27, 9));
		graphics.fillRect(9, 0, 48, 8);
		graphics.setColor(new Color(187, 34, 13));
		graphics.fillRect(10, 1, 46, 6);
		graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 6));
		graphics.setColor(new Color(255, 225, 177));
		graphics.drawString("MINIMAP", 14, 6);
		graphics.setColor(new Color(194, 173, 11));
		graphics.fillRect(2, 2, 3, 3);
		graphics.fillRect(123, 2, 3, 3);
		graphics.fillRect(2, 123, 3, 3);
		graphics.fillRect(123, 123, 3, 3);
		graphics.fillRect(63, 1, 3, 3);
		graphics.fillRect(63, 124, 3, 3);
		graphics.setColor(new Color(92, 44, 18));
		graphics.drawString("N", 62, 6);
		graphics.drawString("S", 62, 126);
		graphics.drawString("W", 1, 66);
		graphics.drawString("E", 123, 66);
	}

	private static BufferedImage background() {
		BufferedImage output = new BufferedImage(MAP_PIXELS, MAP_PIXELS, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = output.createGraphics();
		graphics.setColor(new Color(17, 36, 64));
		graphics.fillRect(0, 0, MAP_PIXELS, MAP_PIXELS);
		graphics.dispose();
		return output;
	}

	private static void nearestNeighbor(Graphics2D graphics) {
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	}

	public record Pixel(int x, int y) {}
}
