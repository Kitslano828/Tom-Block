package org.tomdang.platform.lifecycle;

import org.junit.jupiter.api.Test;
import org.tomdang.platform.identity.ContentKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModuleRuntimeTest {
	@Test void ordersDependenciesAndRunsEveryPhaseBeforeTheNext() {
		List<String> calls = new ArrayList<>();
		TestModule base = new TestModule("base", Set.of(), calls);
		TestModule feature = new TestModule("feature", Set.of(base.id()), calls);
		ModuleRuntime runtime = new ModuleRuntime(List.of(feature, base));

		runtime.start();
		runtime.close();

		assertEquals(List.of(
				"base:load", "feature:load", "base:register", "feature:register",
				"base:validate", "feature:validate", "base:seal", "feature:seal",
				"base:start", "feature:start", "feature:stop", "base:stop"), calls);
	}

	@Test void rejectsMissingDependenciesAndCycles() {
		List<String> calls = new ArrayList<>();
		ContentKey<TomBlockModule> missing = ContentKey.of("test", "missing");
		assertThrows(IllegalArgumentException.class,
				() -> new ModuleRuntime(List.of(new TestModule("feature", Set.of(missing), calls))));
		TestModule[] cycle = new TestModule[2];
		cycle[0] = new TestModule("one", Set.of(ContentKey.of("test", "two")), calls);
		cycle[1] = new TestModule("two", Set.of(ContentKey.of("test", "one")), calls);
		assertThrows(IllegalArgumentException.class, () -> new ModuleRuntime(List.of(cycle)));
	}

	private record TestModule(String name, Set<ContentKey<TomBlockModule>> dependencies,
			List<String> calls) implements TomBlockModule {
		@Override public ContentKey<TomBlockModule> id() { return ContentKey.of("test", name); }
		@Override public void load(ModuleContext context) { calls.add(name + ":load"); }
		@Override public void register(ModuleContext context) { calls.add(name + ":register"); }
		@Override public void validate(ModuleContext context, org.tomdang.platform.validation.ValidationReport report) { calls.add(name + ":validate"); }
		@Override public void seal(ModuleContext context) { calls.add(name + ":seal"); }
		@Override public void start(ModuleContext context) { calls.add(name + ":start"); }
		@Override public void stop(ModuleContext context) { calls.add(name + ":stop"); }
	}
}
