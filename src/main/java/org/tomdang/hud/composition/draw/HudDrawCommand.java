package org.tomdang.hud.composition.draw;

/** Renderer-neutral drawing intent. No command contains a glyph, packet, or carrier detail. */
public sealed interface HudDrawCommand permits HudTextCommand, HudImageCommand, HudProgressBarCommand, HudPanelCommand,
		HudComponentCommand {}
