package org.tomdang.hud.status;

final class StatusHudPresenterSupport {
	static String number(double value) {
		long rounded = Math.round(value);
		return Math.abs(value - rounded) < 0.001 ? Long.toString(rounded) : String.format(java.util.Locale.ROOT, "%.1f", value);
	}
	private StatusHudPresenterSupport() {}
}
