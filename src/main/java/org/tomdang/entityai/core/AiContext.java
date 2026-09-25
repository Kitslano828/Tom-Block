package org.tomdang.entityai.core;
import org.tomdang.entityai.navigation.Navigator;
public record AiContext(AiAgent agent,PerceptionSnapshot perception,AiMemory memory,Navigator navigator,long tick){
	public AiContext{if(agent==null||perception==null||memory==null||navigator==null)throw new IllegalArgumentException("AI context is incomplete");}
}
