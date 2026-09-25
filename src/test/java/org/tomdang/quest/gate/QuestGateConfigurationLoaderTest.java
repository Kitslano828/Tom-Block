package org.tomdang.quest.gate;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestGateConfigurationLoaderTest {
	@Test void loadsAndNormalizesDefinitionDrivenGate() {
		String yaml = """
				gates:
				  INTRO:
				    quest: QUEST
				    world: world
				    center: {x: 10, z: 20}
				    normal: {x: 3, z: 4}
				    half-width: 8
				    minimum-y: 60
				    maximum-y: 80
				    actor: GUIDE
				    dialogue: WARNING
				""";
		QuestGateDefinition gate = new QuestGateConfigurationLoader().load(
				new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))).all().iterator().next();
		assertEquals("QUEST", gate.questId());
		assertEquals(0.6, gate.normalX(), 0.00001);
		assertEquals(0.8, gate.normalZ(), 0.00001);
	}
}
