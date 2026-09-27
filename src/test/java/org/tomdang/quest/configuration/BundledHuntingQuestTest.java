package org.tomdang.quest.configuration;

import org.junit.jupiter.api.Test;
import org.tomdang.quest.definition.QuestObjectiveType;
import org.tomdang.quest.definition.QuestStartPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BundledHuntingQuestTest {
    @Test void tutorialRequiresExplicitAcceptanceAndStartsWithWillsHunt() throws Exception {
        try (var input = getClass().getClassLoader().getResourceAsStream(
                "quests/southwest-island/main/introduction-to-hunting.yml")) {
            var quest = new QuestConfigurationLoader().load(input).getFirst();
            assertEquals(QuestStartPolicy.MANUAL, quest.startPolicy());
            assertEquals("ASSIST_WILL", quest.startStageId());
            var stage = quest.stages().get("ASSIST_WILL");
            assertEquals("WILLS_MOSSBACK_HUNT", stage.enterActions().getFirst().parameters().get("encounter"));
            assertEquals(QuestObjectiveType.COMPLETE_ENCOUNTER, stage.objectives().getFirst().type());
            assertEquals("LEARN_FROM_WILL", stage.nextStageId());
            var lesson = quest.stages().get("LEARN_FROM_WILL");
            assertEquals(QuestObjectiveType.COMPLETE_DIALOGUE, lesson.objectives().getFirst().type());
            assertEquals("CRITTER_HUNTER_WILL_LESSON", lesson.objectives().getFirst().target());
            assertEquals("SOLO_HUNT", lesson.nextStageId());
        }
    }

    @Test void tutorialThenRunsSoloHuntAndAwardsCritterdex() throws Exception {
        try (var input = getClass().getClassLoader().getResourceAsStream(
                "quests/southwest-island/main/introduction-to-hunting.yml")) {
            var quest = new QuestConfigurationLoader().load(input).getFirst();
            assertEquals("FIRST_SOLO_MOSSBACK_HUNT",
                    quest.stages().get("SOLO_HUNT").enterActions().getFirst().parameters().get("encounter"));
            assertEquals("GIVE_CUSTOM_ITEM", quest.rewards().getFirst().type());
            assertEquals("CRITTERDEX", quest.rewards().getFirst().parameters().get("item"));
        }
    }
}
