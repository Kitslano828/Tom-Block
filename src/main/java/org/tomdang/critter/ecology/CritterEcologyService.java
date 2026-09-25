package org.tomdang.critter.ecology;
import org.tomdang.critter.definition.*;import java.util.*;
/** Deterministic eligibility and weighting; population scheduling is intentionally a later adapter. */
public final class CritterEcologyService{
	public List<CritterDefinition> eligible(Collection<CritterDefinition> definitions,HabitatContext context){return definitions.stream().filter(value->!Collections.disjoint(value.habitats(),context.habitats())).toList();}
	public double weight(CritterDefinition value,HabitatContext context){double base=switch(value.rarity()){case COMMON->100;case UNCOMMON->30;case RARE->8;case EXCEPTIONAL->2;case MYTHIC->0.25;};double rareBonus=value.rarity().ordinal()<CritterRarity.RARE.ordinal()?0:Math.min(1.0,context.wildFortune()/100.0);return base*(1+rareBonus);}
}
