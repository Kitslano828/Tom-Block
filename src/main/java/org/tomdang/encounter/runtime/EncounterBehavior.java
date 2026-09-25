package org.tomdang.encounter.runtime;
public interface EncounterBehavior {
	default void start(EncounterRuntimeContext context) {}
	default void resume(EncounterRuntimeContext context) {}
	default void suspend(EncounterRuntimeContext context) {}
	default void complete(EncounterRuntimeContext context) {}
	default void fail(EncounterRuntimeContext context, String reason) {}
}
