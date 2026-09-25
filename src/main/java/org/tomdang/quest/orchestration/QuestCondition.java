package org.tomdang.quest.orchestration;
import java.util.Map;
@FunctionalInterface public interface QuestCondition { boolean test(QuestRuntimeContext context, Map<String, String> parameters); }
