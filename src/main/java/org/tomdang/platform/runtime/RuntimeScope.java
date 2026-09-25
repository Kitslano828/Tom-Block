package org.tomdang.platform.runtime;

import java.util.ArrayDeque;
import java.util.Deque;

/** Owns runtime resources and closes them in reverse creation order. */
public final class RuntimeScope implements AutoCloseable {
	private final Deque<AutoCloseable> resources = new ArrayDeque<>();
	private boolean closed;

	public <T extends AutoCloseable> T own(T resource) {
		if (resource == null) throw new IllegalArgumentException("resource cannot be null");
		if (closed) throw new IllegalStateException("Runtime scope is closed");
		resources.push(resource);
		return resource;
	}

	public boolean isClosed() { return closed; }

	@Override
	public void close() {
		if (closed) return;
		closed = true;
		RuntimeException failure = null;
		while (!resources.isEmpty()) {
			try { resources.pop().close(); }
			catch (Exception exception) {
				if (failure == null) failure = new IllegalStateException("Could not close runtime resources");
				failure.addSuppressed(exception);
			}
		}
		if (failure != null) throw failure;
	}
}
