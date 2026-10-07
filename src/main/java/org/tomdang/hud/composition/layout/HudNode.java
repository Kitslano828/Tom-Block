package org.tomdang.hud.composition.layout;

public sealed interface HudNode permits HudPrimitive, HudStack, HudPadding, HudOverlay, HudClip, HudTranslate {}
