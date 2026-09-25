package org.tomdang.encounter.runtime;
import org.tomdang.encounter.definition.EncounterDefinition;
import org.tomdang.platform.runtime.RuntimeScope;
public record EncounterRuntimeContext(EncounterDefinition definition, EncounterSession session, RuntimeScope resources) {}
