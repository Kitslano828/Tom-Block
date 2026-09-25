package org.tomdang.platform.presentation;

import org.tomdang.platform.identity.ContentKey;

public record PresentationRequest(
		ContentKey<?> owner,
		PresentationChannel channel,
		int priority,
		Object model
) {
	public PresentationRequest {
		if (owner == null) throw new IllegalArgumentException("owner cannot be null");
		if (channel == null) throw new IllegalArgumentException("channel cannot be null");
		if (model == null) throw new IllegalArgumentException("model cannot be null");
	}
}
