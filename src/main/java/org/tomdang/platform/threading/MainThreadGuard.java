package org.tomdang.platform.threading;

import java.util.function.BooleanSupplier;

public final class MainThreadGuard {
	private final BooleanSupplier mainThreadCheck;

	public MainThreadGuard(BooleanSupplier mainThreadCheck) {
		if (mainThreadCheck == null) throw new IllegalArgumentException("mainThreadCheck cannot be null");
		this.mainThreadCheck = mainThreadCheck;
	}

	public void requireMainThread(String operation) {
		if (!mainThreadCheck.getAsBoolean()) throw new IllegalStateException(operation + " must run on the server thread");
	}
}
