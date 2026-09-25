package org.tomdang.entityai.configuration;
import java.util.Map;
public record AiGoalDefinition(String id,String type,int priority,Map<String,String> parameters){public AiGoalDefinition{if(id==null||id.isBlank()||type==null||type.isBlank()||priority<0||parameters==null)throw new IllegalArgumentException("AI goal definition is invalid");id=id.toUpperCase(java.util.Locale.ROOT);type=type.toUpperCase(java.util.Locale.ROOT);parameters=Map.copyOf(parameters);}}
