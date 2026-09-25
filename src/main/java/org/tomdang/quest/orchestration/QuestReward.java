package org.tomdang.quest.orchestration;
import java.util.Map;
@FunctionalInterface public interface QuestReward { void grant(QuestRuntimeContext context, Map<String, String> parameters); }
