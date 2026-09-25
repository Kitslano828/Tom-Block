package org.tomdang.entityai.configuration;
import java.util.*;
public record AiProfile(String navigator,List<AiGoalDefinition> goals){public AiProfile{if(navigator==null||navigator.isBlank()||goals==null||goals.isEmpty())throw new IllegalArgumentException("AI profile is incomplete");navigator=navigator.toUpperCase(Locale.ROOT);goals=List.copyOf(goals);if(goals.stream().map(AiGoalDefinition::id).distinct().count()!=goals.size())throw new IllegalArgumentException("AI goal ids must be unique");}}
