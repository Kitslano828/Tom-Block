package org.tomdang.critter.journal;
import org.junit.jupiter.api.Test;import java.util.UUID;import static org.junit.jupiter.api.Assertions.*;
class CritterJournalServiceTest{
	@Test void advancesKnowledgeAndCountsRepeatHunts(){var service=new CritterJournalService(new InMemoryCritterJournalRepository());UUID player=UUID.randomUUID();service.discover(player,"FLY");service.observe(player,"FLY");service.hunt(player,"FLY");var entry=service.hunt(player,"FLY");assertEquals(CritterKnowledge.HUNTED,entry.knowledge());assertEquals(1,entry.observations());assertEquals(2,entry.successfulHunts());}
}
