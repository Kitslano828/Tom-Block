package org.tomdang.entityai.core;
public interface AiBehavior { AiBehaviorStatus tick(AiContext context); default void stop(AiContext context,boolean interrupted){} }
