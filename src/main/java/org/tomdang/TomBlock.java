package org.tomdang;

import org.bukkit.plugin.java.JavaPlugin;
import org.tomdang.bootstrap.TomBlockApplication;

/** Paper entry point. Application composition and lifecycle live in {@link TomBlockApplication}. */
public final class TomBlock extends JavaPlugin {
	private TomBlockApplication application;

	@Override
	public void onEnable() {
		application = new TomBlockApplication(this);
		try {
			application.start();
		} catch (RuntimeException | Error failure) {
			try {
				application.close();
			} catch (RuntimeException shutdownFailure) {
				failure.addSuppressed(shutdownFailure);
			}
			application = null;
			throw failure;
		}
	}

	@Override
	public void onDisable() {
		if (application != null) {
			application.close();
			application = null;
		}
	}
}
