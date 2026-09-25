package org.tomdang.critter.configuration;
import org.junit.jupiter.api.Test;import org.tomdang.critter.definition.*;import java.io.*;import java.nio.charset.StandardCharsets;import static org.junit.jupiter.api.Assertions.*;
class CritterConfigurationLoaderTest{
	@Test void loadsCompleteDefinition(){String yaml="""
critter:
  id: TEST_FLY
  name: Test Fly
  family: LUMINA
  rarity: RARE
  habitats: [FOREST_EDGE]
  temperaments: [CURIOUS, SKITTISH]
  presentation: PLACEHOLDER
  movement: {modes: [HOVERING], wander-radius: 5, preferred-height: 1.5, speed: 0.8, rest-duration-seconds: 3, relocation-distance: 4}
  hunting: {archetypes: [OBSERVE], awareness-radius: 5, capture-window-seconds: 3, maximum-relocations: 2, outcome: CAPTURED_AND_RELEASED}
  ai:
    navigator: DIRECT_FLIGHT
    goals:
      IDLE: {type: IDLE, priority: 0}
  rewards:
    hunting-xp: 15
    materials:
      DUST: {minimum: 1, maximum: 2, chance: 0.5}
""";var value=new CritterConfigurationLoader().load(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));assertEquals("TEST_FLY",value.id());assertEquals(CritterRarity.RARE,value.rarity());assertEquals(15,value.rewards().huntingXp());assertEquals(2,value.rewards().materials().get("DUST").maximum());assertEquals("DIRECT_FLIGHT",value.ai().navigator());}
}
