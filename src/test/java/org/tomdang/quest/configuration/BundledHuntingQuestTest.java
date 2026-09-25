package org.tomdang.quest.configuration;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class BundledHuntingQuestTest{
	@Test void tutorialRequiresExplicitAcceptance(){try(var input=getClass().getClassLoader().getResourceAsStream("quests/southwest-island/main/introduction-to-hunting.yml")){var quest=new QuestConfigurationLoader().load(input).getFirst();assertEquals(org.tomdang.quest.definition.QuestStartPolicy.MANUAL,quest.startPolicy());assertEquals("OBSERVE",quest.startStageId());}catch(java.io.IOException exception){throw new RuntimeException(exception);}}
	@Test void tutorialStartsTheConfiguredEncounter(){try(var input=getClass().getClassLoader().getResourceAsStream("quests/southwest-island/main/introduction-to-hunting.yml")){var quest=new QuestConfigurationLoader().load(input).getFirst();var observe=quest.stages().get("OBSERVE");var action=observe.enterActions().getFirst();assertEquals("START_ENCOUNTER",action.type());assertEquals("TUTORIAL_GLIMMERFLY",action.parameters().get("encounter"));}catch(java.io.IOException exception){throw new RuntimeException(exception);}}
}
