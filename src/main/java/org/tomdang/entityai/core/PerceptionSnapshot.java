package org.tomdang.entityai.core;
import java.util.*;
public record PerceptionSnapshot(long tick,List<PerceivedEntity> entities){
	public PerceptionSnapshot{entities=entities==null?List.of():List.copyOf(entities);}
	public Optional<PerceivedEntity> nearest(AiVector origin){return entities.stream().filter(PerceivedEntity::visible).min(Comparator.comparingDouble(value->value.position().distanceSquared(origin)));}
	public static PerceptionSnapshot empty(long tick){return new PerceptionSnapshot(tick,List.of());}
}
