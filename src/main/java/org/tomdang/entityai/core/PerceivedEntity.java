package org.tomdang.entityai.core;
import java.util.UUID;
public record PerceivedEntity(UUID id,AiVector position,AiVector velocity,boolean visible,boolean owner,boolean hostile){
	public PerceivedEntity{if(id==null||position==null||velocity==null)throw new IllegalArgumentException("Perceived entity is incomplete");}
}
