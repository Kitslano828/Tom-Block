package org.tomdang.entityai.core;
import java.util.Set;import java.util.UUID;
public interface AiAgent {
	UUID id(); String worldId(); AiVector position(); AiVector home(); Set<AiCapability> capabilities(); boolean valid();
	default String type(){return getClass().getSimpleName();}
	default boolean has(AiCapability capability){return capabilities().contains(capability);}
}
