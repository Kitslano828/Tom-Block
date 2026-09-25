package org.tomdang.quest.integration;
import org.tomdang.dialogueframework.action.*;import org.tomdang.quest.progress.QuestProgressService;
public final class StartQuestDialogueAction implements DialogueChoiceAction{
	private final QuestProgressService quests;public StartQuestDialogueAction(QuestProgressService quests){this.quests=java.util.Objects.requireNonNull(quests);}
	@Override public void execute(DialogueChoiceActionContext context){String quest=context.selectedChoice().getActionParameters().get("quest");if(quest==null||quest.isBlank())throw new IllegalArgumentException("START_QUEST requires quest parameter");quests.start(context.player().getUniqueId(),quest);}
}
