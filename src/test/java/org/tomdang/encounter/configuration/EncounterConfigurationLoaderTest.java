package org.tomdang.encounter.configuration;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertEquals;
class EncounterConfigurationLoaderTest {
	@Test void loadsOneReusableEncounterDefinition(){String yaml="""
			encounter:
			  id: FIREFLY_HUNT
			  behavior: FLYING_CRITTER
			  mode: PLAYER
			  timeout-seconds: 90
			  disconnect-grace-seconds: 30
			  parameters: {critter: FIREFLY}
			""";var value=new EncounterConfigurationLoader().load(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))).getFirst();assertEquals("FIREFLY_HUNT",value.id());assertEquals("FIREFLY",value.parameters().get("critter"));}
}
