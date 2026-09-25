package org.tomdang.quest.presentation;
import java.util.*;
public final class QuestOfferRegistry{
	private final Map<String,List<QuestOfferDefinition>> byActor=new LinkedHashMap<>();
	public void register(QuestOfferDefinition offer){byActor.computeIfAbsent(key(offer.actorId()),ignored->new ArrayList<>()).add(offer);}public List<QuestOfferDefinition> forActor(String actorId){return List.copyOf(byActor.getOrDefault(key(actorId),List.of()));}public Collection<QuestOfferDefinition> all(){return byActor.values().stream().flatMap(Collection::stream).toList();}
	private String key(String value){return value.toUpperCase(Locale.ROOT);}
}
