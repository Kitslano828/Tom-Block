package org.tomdang.quest.orchestration;
import java.util.Map;
@FunctionalInterface public interface QuestAction { void execute(QuestRuntimeContext context, Map<String, String> parameters); }
