package org.tomdang.platform.presentation;

import java.util.UUID;

public record PresentationHandle(UUID id) {
	public PresentationHandle { if (id == null) throw new IllegalArgumentException("id cannot be null"); }
}
