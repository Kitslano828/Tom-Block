package org.tomdang.entityai.core;
@FunctionalInterface public interface AiSensor { PerceptionSnapshot sense(AiAgent agent,long tick); }
