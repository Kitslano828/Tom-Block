package org.tomdang.encounter.runtime;
import org.tomdang.encounter.definition.EncounterDefinition;
import org.tomdang.platform.runtime.RuntimeScope;
import org.tomdang.activity.ActivityInstance;
public record EncounterRuntimeContext(EncounterDefinition definition, EncounterSession session, RuntimeScope resources) {
	public ActivityInstance activity() {
		return new ActivityInstance(session.instanceId(), session.ownerId(), session.participants(), definition.activityPolicy());
	}
}
