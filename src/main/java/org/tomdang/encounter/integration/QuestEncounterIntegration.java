package org.tomdang.encounter.integration;
import org.tomdang.encounter.runtime.EncounterRuntimeService;
import org.tomdang.quest.orchestration.*;
import java.util.Set;
public final class QuestEncounterIntegration {
	public QuestEncounterIntegration(EncounterRuntimeService encounters,QuestHandlerRegistry<QuestAction> actions,QuestHandlerRegistry<QuestCondition> conditions){
		actions.register("START_ENCOUNTER",(context,parameters)->encounters.start(required(parameters,"encounter"),context.playerId(),Set.of(context.playerId())));
		conditions.register("NO_ACTIVE_ENCOUNTER",(context,parameters)->encounters.findFor(context.playerId()).isEmpty());
	}
	private static String required(java.util.Map<String,String> values,String key){String value=values.get(key);if(value==null||value.isBlank())throw new IllegalArgumentException("Missing encounter action parameter "+key);return value;}
}
