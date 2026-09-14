package org.tomdang.actorframework.resolver;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomdang.actorframework.instance.ActorInstanceRegistry;
import org.tomdang.actorframework.registry.ActorRegistry;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActorResolverTest {

	private NamespacedKey actorDefinitionIDKey;
	private ActorResolver actorResolver;

	@BeforeEach
	void setUp() {
		actorDefinitionIDKey = mock(NamespacedKey.class);
		actorResolver = new ActorResolver(
				mock(NamespacedKey.class),
				actorDefinitionIDKey,
				mock(NamespacedKey.class),
				mock(NamespacedKey.class),
				mock(NamespacedKey.class),
				mock(ActorRegistry.class),
				mock(ActorInstanceRegistry.class)
		);
	}

	@Test
	void isManagedActorEntityRejectsNullEntity() {
		assertThrows(IllegalArgumentException.class, () -> actorResolver.isManagedActorEntity(null));
	}

	@Test
	void isManagedActorEntityReturnsTrueWhenDefinitionMetadataExists() {
		Entity entity = mock(Entity.class);
		PersistentDataContainer persistentDataContainer = mock(PersistentDataContainer.class);
		when(entity.getPersistentDataContainer()).thenReturn(persistentDataContainer);
		when(persistentDataContainer.has(actorDefinitionIDKey, PersistentDataType.STRING)).thenReturn(true);

		assertTrue(actorResolver.isManagedActorEntity(entity));
	}

	@Test
	void isManagedActorEntityReturnsFalseWhenDefinitionMetadataIsAbsent() {
		Entity entity = mock(Entity.class);
		PersistentDataContainer persistentDataContainer = mock(PersistentDataContainer.class);
		when(entity.getPersistentDataContainer()).thenReturn(persistentDataContainer);
		when(persistentDataContainer.has(actorDefinitionIDKey, PersistentDataType.STRING)).thenReturn(false);

		assertFalse(actorResolver.isManagedActorEntity(entity));
	}
}
