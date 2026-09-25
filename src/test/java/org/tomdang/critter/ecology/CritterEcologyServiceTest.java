package org.tomdang.critter.ecology;
import org.junit.jupiter.api.Test;import org.tomdang.critter.definition.*;import java.time.Duration;import java.util.*;import static org.junit.jupiter.api.Assertions.*;
class CritterEcologyServiceTest{
	@Test void filtersHabitatsAndAppliesWildFortuneOnlyToRareTiers(){var service=new CritterEcologyService();var common=definition("COMMON",CritterRarity.COMMON,"FOREST");var rare=definition("RARE",CritterRarity.RARE,"FOREST");var pond=definition("POND",CritterRarity.RARE,"POND");var context=new HabitatContext(Set.of("FOREST"),50);assertEquals(List.of(common,rare),service.eligible(List.of(common,rare,pond),context));assertEquals(100,service.weight(common,context));assertEquals(12,service.weight(rare,context));}
	private CritterDefinition definition(String id,CritterRarity rarity,String habitat){return new CritterDefinition(id,id,"TEST",rarity,Set.of(habitat),Set.of(Temperament.DOCILE),new CritterMovement(Set.of(MovementType.GROUND),1,0,1,Duration.ZERO,1),new CritterHunting(Set.of(HuntingArchetype.OBSERVE),2,Duration.ofSeconds(1),1,CaptureOutcome.OBSERVED),new CritterRewards(1,Map.of()),"PLACEHOLDER","",Set.of());}
}
