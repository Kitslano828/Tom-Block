package org.tomdang.entityai.core;
public interface AiGoal {
	String id(); int priority(); boolean canStart(AiContext context); AiBehavior start(AiContext context);
	default boolean canContinue(AiContext context,AiBehavior behavior){return true;}
}
