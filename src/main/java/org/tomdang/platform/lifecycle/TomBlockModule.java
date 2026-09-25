package org.tomdang.platform.lifecycle;

import org.tomdang.platform.identity.ContentKey;
import org.tomdang.platform.validation.ValidationReport;

import java.util.Set;

public interface TomBlockModule {
	ContentKey<TomBlockModule> id();
	default Set<ContentKey<TomBlockModule>> dependencies() { return Set.of(); }
	default void load(ModuleContext context) {}
	default void register(ModuleContext context) {}
	default void validate(ModuleContext context, ValidationReport report) {}
	default void seal(ModuleContext context) {}
	default void start(ModuleContext context) {}
	default void stop(ModuleContext context) {}
}
