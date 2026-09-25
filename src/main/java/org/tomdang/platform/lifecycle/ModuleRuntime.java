package org.tomdang.platform.lifecycle;

import org.tomdang.platform.identity.ContentKey;
import org.tomdang.platform.runtime.RuntimeScope;
import org.tomdang.platform.validation.ValidationReport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Runs every module through the same deterministic startup and reverse shutdown lifecycle. */
public final class ModuleRuntime implements AutoCloseable {
	private final RuntimeScope scope = new RuntimeScope();
	private final ModuleContext context = new ModuleContext(scope);
	private final List<TomBlockModule> orderedModules;
	private final Map<ContentKey<TomBlockModule>, ModulePhase> phases = new LinkedHashMap<>();
	private boolean started;
	private boolean closed;

	public ModuleRuntime(Collection<? extends TomBlockModule> modules) {
		orderedModules = order(modules);
		orderedModules.forEach(module -> phases.put(module.id(), ModulePhase.CREATED));
	}

	public ModuleContext context() { return context; }
	public List<TomBlockModule> modules() { return List.copyOf(orderedModules); }
	public ModulePhase phase(ContentKey<TomBlockModule> id) { return phases.get(id); }

	public void start() {
		if (started) throw new IllegalStateException("Module runtime already started");
		if (closed) throw new IllegalStateException("Module runtime is closed");
		try {
			run(ModulePhase.LOADED, module -> module.load(context));
			run(ModulePhase.REGISTERED, module -> module.register(context));
			ValidationReport report = new ValidationReport();
			for (TomBlockModule module : orderedModules) {
				module.validate(context, report);
				phases.put(module.id(), ModulePhase.VALIDATED);
			}
			report.throwIfInvalid();
			run(ModulePhase.SEALED, module -> module.seal(context));
			run(ModulePhase.STARTED, module -> module.start(context));
			started = true;
		} catch (RuntimeException failure) {
			closeAfterFailure(failure);
			throw failure;
		}
	}

	private void run(ModulePhase phase, ModuleAction action) {
		for (TomBlockModule module : orderedModules) {
			action.run(module);
			phases.put(module.id(), phase);
		}
	}

	@Override
	public void close() {
		if (closed) return;
		closed = true;
		RuntimeException failure = null;
		for (int index = orderedModules.size() - 1; index >= 0; index--) {
			TomBlockModule module = orderedModules.get(index);
			if (phases.get(module.id()) == ModulePhase.STARTED) {
				try { module.stop(context); }
				catch (RuntimeException exception) {
					if (failure == null) failure = new IllegalStateException("Could not stop modules");
					failure.addSuppressed(exception);
				}
			}
			phases.put(module.id(), ModulePhase.STOPPED);
		}
		try { scope.close(); }
		catch (RuntimeException exception) {
			if (failure == null) failure = exception; else failure.addSuppressed(exception);
		}
		if (failure != null) throw failure;
	}

	private void closeAfterFailure(RuntimeException failure) {
		try { close(); } catch (RuntimeException closeFailure) { failure.addSuppressed(closeFailure); }
	}

	private static List<TomBlockModule> order(Collection<? extends TomBlockModule> modules) {
		if (modules == null) throw new IllegalArgumentException("modules cannot be null");
		Map<ContentKey<TomBlockModule>, TomBlockModule> byId = new LinkedHashMap<>();
		for (TomBlockModule module : modules) {
			if (module == null) throw new IllegalArgumentException("modules cannot contain null");
			if (byId.putIfAbsent(module.id(), module) != null) throw new IllegalArgumentException("Duplicate module: " + module.id());
		}
		List<TomBlockModule> result = new ArrayList<>();
		Set<ContentKey<TomBlockModule>> visiting = new HashSet<>();
		Set<ContentKey<TomBlockModule>> visited = new HashSet<>();
		for (TomBlockModule module : byId.values()) visit(module, byId, visiting, visited, result);
		return List.copyOf(result);
	}

	private static void visit(TomBlockModule module, Map<ContentKey<TomBlockModule>, TomBlockModule> byId,
			Set<ContentKey<TomBlockModule>> visiting, Set<ContentKey<TomBlockModule>> visited, List<TomBlockModule> result) {
		if (visited.contains(module.id())) return;
		if (!visiting.add(module.id())) throw new IllegalArgumentException("Module dependency cycle at " + module.id());
		for (ContentKey<TomBlockModule> dependencyId : module.dependencies()) {
			TomBlockModule dependency = byId.get(dependencyId);
			if (dependency == null) throw new IllegalArgumentException("Module " + module.id() + " requires missing module " + dependencyId);
			visit(dependency, byId, visiting, visited, result);
		}
		visiting.remove(module.id());
		visited.add(module.id());
		result.add(module);
	}

	@FunctionalInterface private interface ModuleAction { void run(TomBlockModule module); }
}
