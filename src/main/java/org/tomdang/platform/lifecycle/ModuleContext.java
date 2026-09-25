package org.tomdang.platform.lifecycle;

import org.tomdang.platform.runtime.RuntimeScope;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModuleContext {
	private final Map<ServiceKey<?>, Object> services = new LinkedHashMap<>();
	private final RuntimeScope runtimeScope;

	public ModuleContext(RuntimeScope runtimeScope) {
		if (runtimeScope == null) throw new IllegalArgumentException("runtimeScope cannot be null");
		this.runtimeScope = runtimeScope;
	}

	public <T> void provide(ServiceKey<T> key, T service) {
		if (key == null) throw new IllegalArgumentException("key cannot be null");
		if (service == null) throw new IllegalArgumentException("service cannot be null");
		if (!key.type().isInstance(service)) throw new IllegalArgumentException("Service does not match " + key.type().getName());
		if (services.putIfAbsent(key, service) != null) throw new IllegalArgumentException("Service already provided: " + key.name());
	}

	public <T> T require(ServiceKey<T> key) {
		Object value = services.get(key);
		if (value == null) throw new IllegalStateException("Missing service: " + key.name());
		return key.type().cast(value);
	}

	public RuntimeScope runtime() { return runtimeScope; }
}
