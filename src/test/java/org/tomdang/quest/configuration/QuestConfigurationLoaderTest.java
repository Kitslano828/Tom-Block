package org.tomdang.quest.configuration;

import org.junit.jupiter.api.Test;
import org.tomdang.quest.definition.QuestObjectiveType;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestConfigurationLoaderTest {
	private final QuestConfigurationLoader loader = new QuestConfigurationLoader();

	@Test
	void loadsBranchingQuestGraphAndObjectiveParameters() {
		String yaml = """
				quests:
				  TEST_QUEST:
				    display-name: Test Quest
				    description: A test.
				    category: STORY
				    repeatability: ONCE
				    prerequisites: []
				    start-stage: START
				    stages:
				      START:
				        display-name: Begin
				        objectives:
				          SPEAK:
				            type: INTERACT_WITH_ACTOR
				            target: GUIDE
				            parameters:
				              dialogue-node: GREETING
				        branches:
				          help: HELP
				          refuse: REFUSE
				      HELP:
				        display-name: Help
				        objectives:
				          CAPTURE:
				            type: CAPTURE_CRITTER
				            target: GLIMMERFLY
				      REFUSE:
				        display-name: Refuse
				        objectives:
				          LEAVE:
				            type: ENTER_REGION
				            target: VILLAGE
				""";
		var quest = loader.load(stream(yaml)).getFirst();
		assertEquals("TEST_QUEST", quest.id());
		assertEquals(3, quest.stages().size());
		assertEquals("HELP", quest.stages().get("START").branches().get("help"));
		assertEquals(QuestObjectiveType.INTERACT_WITH_ACTOR,
				quest.stages().get("START").objectives().getFirst().type());
		assertEquals("GREETING",
				quest.stages().get("START").objectives().getFirst().parameters().get("dialogue-node"));
	}

	@Test
	void rejectsUnknownAndUnreachableStages() {
		String unknown = basicQuest("next-stage: MISSING");
		assertTrue(assertThrows(IllegalArgumentException.class, () -> loader.load(stream(unknown)))
				.getMessage().contains("unknown next stage"));

		String unreachable = """
				quests:
				  Q:
				    display-name: Quest
				    category: STORY
				    start-stage: START
				    stages:
				      START:
				        display-name: Start
				        objectives:
				          ONE: {type: CUSTOM, target: ONE}
				      ORPHAN:
				        display-name: Orphan
				        objectives:
				          TWO: {type: CUSTOM, target: TWO}
				""";
		assertTrue(assertThrows(IllegalArgumentException.class, () -> loader.load(stream(unreachable)))
				.getMessage().contains("Unreachable quest stages"));
	}

	private String basicQuest(String transition) {
		return """
				quests:
				  Q:
				    display-name: Quest
				    category: STORY
				    start-stage: START
				    stages:
				      START:
				        display-name: Start
				        objectives:
				          ONE: {type: CUSTOM, target: ONE}
				        %s
				""".formatted(transition);
	}

	private ByteArrayInputStream stream(String yaml) {
		return new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
	}
}
