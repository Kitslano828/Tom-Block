package org.tomdang.critter.definition;
import java.util.Set;import org.tomdang.entityai.configuration.*;import java.util.List;import java.util.Map;
public record CritterDefinition(String id,String name,String family,CritterRarity rarity,Set<String> habitats,
		Set<Temperament> temperaments,CritterMovement movement,CritterHunting hunting,CritterRewards rewards,
		String presentation,String journalDescription,Set<String> relations,AiProfile ai){
	public CritterDefinition{if(id==null||id.isBlank()||name==null||name.isBlank()||family==null||family.isBlank()||rarity==null||movement==null||hunting==null||rewards==null||presentation==null||presentation.isBlank()||ai==null)throw new IllegalArgumentException("Incomplete critter definition");habitats=habitats==null?Set.of():Set.copyOf(habitats);temperaments=temperaments==null?Set.of():Set.copyOf(temperaments);relations=relations==null?Set.of():Set.copyOf(relations);journalDescription=journalDescription==null?"":journalDescription;}
	public CritterDefinition(String id,String name,String family,CritterRarity rarity,Set<String> habitats,Set<Temperament> temperaments,CritterMovement movement,CritterHunting hunting,CritterRewards rewards,String presentation,String journalDescription,Set<String> relations){this(id,name,family,rarity,habitats,temperaments,movement,hunting,rewards,presentation,journalDescription,relations,new AiProfile(movement.modes().contains(MovementType.GROUND)?"NATIVE_GROUND":"DIRECT_FLIGHT",List.of(new AiGoalDefinition("IDLE","IDLE",0,Map.of()))));}
}
