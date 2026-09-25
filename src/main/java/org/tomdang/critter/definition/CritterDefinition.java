package org.tomdang.critter.definition;
import java.util.Set;
public record CritterDefinition(String id,String name,String family,CritterRarity rarity,Set<String> habitats,
		Set<Temperament> temperaments,CritterMovement movement,CritterHunting hunting,CritterRewards rewards,
		String presentation,String journalDescription,Set<String> relations){
	public CritterDefinition{if(id==null||id.isBlank()||name==null||name.isBlank()||family==null||family.isBlank()||rarity==null||movement==null||hunting==null||rewards==null||presentation==null||presentation.isBlank())throw new IllegalArgumentException("Incomplete critter definition");habitats=habitats==null?Set.of():Set.copyOf(habitats);temperaments=temperaments==null?Set.of():Set.copyOf(temperaments);relations=relations==null?Set.of():Set.copyOf(relations);journalDescription=journalDescription==null?"":journalDescription;}
}
